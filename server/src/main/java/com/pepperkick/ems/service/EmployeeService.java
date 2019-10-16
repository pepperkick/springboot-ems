package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.EmployeeRequestPostBody;
import com.pepperkick.ems.requestbody.EmployeeRequestPutBody;
import com.pepperkick.ems.util.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private static final String TAG_SUBORDINATE_CANNOT_HAVE_HIGHER_DESIGNATION = "error.route.employee.restriction.subordinate.cannot_have_higher_designation";
    private final EmployeeRepository employeeRepository;
    private final DesignationService designationService;
    private final MessageHelper messageHelper;
    private Designation mainDesignation;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, @Lazy DesignationService designationService, MessageHelper messageHelper) {
        this.employeeRepository = employeeRepository;
        this.designationService = designationService;
        this.messageHelper = messageHelper;

        // Find main designation (Director) for later use
        // Designation with level 1 is assumed main designation
        mainDesignation = designationService.getMainDesignation();
    }

    public Employee findById(int id) {
        return findById(id, false);
    }

    public Employee findById(int id, boolean badRequest) {
        return findById(id, badRequest, "error.route.employee.notfound");
    }

    public Employee findById(int id, boolean badRequest, String tag) {
        // Find employee from repository by given ID
        Employee employee = employeeRepository.findById(id);

        // If employee is null
        if (employee == null) {
            // Throw badRequest error if badRequest flag is true
            if (badRequest) {
                throw new BadRequestException(messageHelper.getMessage(tag, id));
                // Throw notFound error
            } else{
                throw new NotFoundException(messageHelper.getMessage(tag, id));
            }
        }

        return employee;
    }

    public Employee create(EmployeeRequestPostBody body) {
        return create(body.getName(), body.getJobTitle(), body.getManagerId(), true);
    }

    public Employee create(String name, String jobTitle, int managerId, boolean save) {
        if (mainDesignation == null)
            throw new BadRequestException(messageHelper.getMessage("error.route.employee.notfound.main_designation"));

        // Find designation by jobTitle
        Designation designation = designationService.findByTitle(jobTitle, true, "error.route.employee.notfound.designation");

        // If designation equals to main designation
        if (designation.equals(mainDesignation)) {
            if (save) {
                List<Employee> employees = employeeRepository.findEmployeeByDesignation(mainDesignation);

                // If employee list with main designation is not empty then return 400
                // Cannot have more than one director
                if (!employees.isEmpty())
                    throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.single"));
            }

            // If managerId is present then return 400
            // Employee with main designation (Director) cannot have a manager
            if (managerId != -1)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"));
        } else {
            // If managerId is not present then return 400
            // Only director cannot have manager
            if (managerId == -1)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.can_only_have_no_manager"));
        }

        Employee manager = null;
        if (managerId != -1) {
            // Find employee with managerId
            manager = findById(managerId, true, "error.route.employee.notfound.manager");

            if (manager.getDesignation().compareByLevel(designation) >= 0)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation", jobTitle, manager.getDesignation().getTitle()));
        }

        return create(name, designation, manager, save);
    }

    public Employee create(String name, Designation designation, Employee manager, boolean save) {
        // Check if manager is present and if  designation is lower than new employee's designation
        if (manager != null && manager.getDesignation().compareByLevel(designation) >= 0)
            throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation",  designation.getTitle(), manager.getDesignation().getTitle()));

        // Create new employee
        Employee employee = new Employee();
        employee.setName(name);
        employee.setDesignation(designation);
        employee.setManager(manager);

        return save ? employeeRepository.save(employee) : employee;
    }

    public Employee put(int id, EmployeeRequestPutBody body) {
        Employee employee = findById(id);

        if (body.isReplace())
            return replace(employee, body.getName(), body.getJobTitle(), body.getManagerId());
        else
            return update(employee, body.getName(), body.getJobTitle(), body.getManagerId());
    }

    public Employee update(Employee employee, String title, String jobTitle, int managerId) {
        // Update employee name
        employee.setName(title != null ? title : employee.getName());

        if (jobTitle != null) {
            // Find designation by jobTitle
            Designation designation = designationService.findByTitle(jobTitle, true, "error.route.employee.notfound.designation");

            // If employee's designation is equal to main designation (Director) and designation is updated then return 400
            // Cannot change designation of a employee with main designation (Director)
            if (employee.getDesignation().equals(mainDesignation)) {
                if (!designation.equals(mainDesignation))
                    throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.cannot_change_designation"));

                if (managerId != -1)
                    throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"));
            }

            // Check if designation is lower than any designation of subordinate
            if (isDesignationHigherOrLowerThanSubordinateDesignation(designation, employee, true))
                throw new BadRequestException(messageHelper.getMessage(TAG_SUBORDINATE_CANNOT_HAVE_HIGHER_DESIGNATION, jobTitle));

            // Update employee designation
            employee.setDesignation(designation);
        }

        if (managerId != -1) {
            // Find employee with ID equal to PUT body managerID
            Employee manager = findById(managerId, true, "error.route.employee.notfound.manager");

            // If manager's designation level is less than current employee designation level then return 400
            // Manager's designation level cannot be lower than it's subordinates
            if (manager.getDesignation().compareByLevel(employee.getDesignation()) >= 0)
                throw new BadRequestException(messageHelper.getMessage(TAG_SUBORDINATE_CANNOT_HAVE_HIGHER_DESIGNATION, jobTitle));

            // Update employee manager
            employee.setManager(manager);
        }

        // Save updated employee
        return employeeRepository.save(employee);
    }

    public Employee replace(Employee employee, String name, String jobTitle, int managerId) {
        // Check if managerId is present
        if (managerId == -1 && employee.getManager() != null)
            // Set managerId to current employee's manager
            managerId = employee.getManager().getId();

        // Create new employee
        Employee newEmployee = create(name, jobTitle, managerId, false);

        // Check if designation is lower than any designation of subordinate
        if (isDesignationHigherOrLowerThanSubordinateDesignation(newEmployee.getDesignation(), employee, false))
            throw new BadRequestException(messageHelper.getMessage(TAG_SUBORDINATE_CANNOT_HAVE_HIGHER_DESIGNATION, jobTitle));

        // Replace employee
        newEmployee = employeeRepository.save(newEmployee);
        changeManagerOfSubordinates(employee, newEmployee);
        employeeRepository.delete(employee);

        return newEmployee;
    }

    public void deleteById(int id) {
        // Get employee ith the given ID
        Employee employee = findById(id);

        // IF employee's designation is equal to main designation (Director) then return 400
        // Cannot delete employee with main designation (Director
        if (employee.getDesignation().equals(mainDesignation) && !employee.getSubordinates().isEmpty())
            throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.subordinates_not_empty"));

        // If employee subordinates list is not empty
        if (!employee.getSubordinates().isEmpty()) {
            // Get employee with ID equal to current employee's manager ID
            Employee manager = employee.getManager();

            // Change manager of each subordinate of current employee with current employee's manager
            employee.getSubordinates().forEach(object -> {
                object.setManager(manager);
                employeeRepository.save(object);
            });
        }

        // Delete employee
        employeeRepository.delete(employee);
    }

    public boolean isDesignationHigherOrLowerThanSubordinateDesignation(Designation designation, Employee employee, boolean isHigher) {
        // If employee's subordinates list is not empty
        if (!employee.getSubordinates().isEmpty()) {
            // Get current employee's designation
            Designation highest = getHighestOrLowestSubordinateDesignation(employee, isHigher);

            // If current designation level is lower then highest subordinate designation level then return 400
            // Employee designation cannot be lower than it's subordinates
            return isHigher ? designation.compareByLevel(highest) <= 0 : designation.compareByLevel(highest) >= 0 ;
        }

        return false;
    }

    public Designation getHighestOrLowestSubordinateDesignation(Employee employee, boolean isHighest) {
        // Get current employee's designation
        Designation compare = employee.getSubordinates().first().getDesignation();

        // Check designation of each subordinate
        for (Employee sub : employee.getSubordinates()) {
            if (isHighest && sub.getDesignation().compareByLevel(compare) > 0)
                compare = sub.getDesignation();
            if (!isHighest && sub.getDesignation().compareByLevel(compare) < 0)
                compare = sub.getDesignation();
        }

        return compare;
    }

    public void changeManagerOfSubordinates(Employee employee, Employee manager) {
        // Change manager of old employee's subordinates
        for (Employee subordinate : employee.getSubordinates()) {
            subordinate.setManager(manager);
            employeeRepository.save(subordinate);
        }
    }

    void setMainDesignation(Designation designation) { this.mainDesignation = designation; }
}
