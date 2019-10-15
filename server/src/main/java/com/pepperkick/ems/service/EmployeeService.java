package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.EmployeeRequestPostBody;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final MessageHelper messageHelper;
    private Designation mainDesignation;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DesignationRepository designationRepository, DesignationRepository designationRepository1, MessageHelper messageHelper) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository1;
        this.messageHelper = messageHelper;

        // Find main designation (Director) for later use
        // Designation with level 1 is assumed main designation
        List<Designation> designations = designationRepository.findByLevel(1);
        if (designations.size() == 1) mainDesignation = designations.get(0);
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
        if (employee == null)
            // Throw badRequest error if badRequest flag is true
            if (badRequest)
                throw new BadRequestException(messageHelper.getMessage(tag, id));
            // Throw notFound error
            else
                throw new NotFoundException(messageHelper.getMessage(tag, id));

        return employee;
    }

    public Employee create(EmployeeRequestPostBody body) {
        return create(body.getName(), body.getJobTitle(), body.getManagerId());
    }

    public Employee create(String name, String jobTitle, int managerId) {
        // Find designation by jobTitle
        Designation designation = designationRepository.findByTitle(jobTitle);

        if (designation == null)
            throw new BadRequestException(messageHelper.getMessage("error.route.employee.notfound.designation", jobTitle));

        if (designation.equalsTo(mainDesignation)) {
            List<Employee> employees = employeeRepository.findEmployeeByDesignation(mainDesignation);

            // If employee list with main designation is not empty then return 400
            // Cannot have more than one director
            if (employees.size() != 0)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.single"));

            // If managerId is present then return 400
            // Employee with main designation (Director) cannot have a manager
            if (managerId != -1)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"));
        } else {
            if (managerId == -1)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.can_only_have_no_manager"));
        }

        Employee manager = null;
        if (managerId != -1)
            // Find employee with managerId
            manager = findById(managerId, true, "error.route.employee.notfound.manager");

        return create(name, designation, manager);
    }

    public Employee create(String name, Designation designation, Employee manager) {
        // Check if manager is present
        if (manager != null) {
            // Check if manager designation is lower than new employee's designation
            if (manager.getDesignation().compareTo(designation) >= 0) {
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation",  designation.getTitle(), manager.getDesignation().getTitle()));
            }
        }

        // Create new employee
        Employee employee = new Employee();
        employee.setName(name);
        employee.setDesignation(designation);
        employee.setManager(manager);

        return employeeRepository.save(employee);
    }

    public void deleteById(int id) {
        // Get employee ith the given ID
        Employee employee = findById(id);

        // IF employee's designation is equal to main designation (Director) then return 400
        // Cannot delete employee with main designation (Director
        if (employee.getDesignation().equalsTo(mainDesignation)) {
            if (!employee.getSubordinates().isEmpty())
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.restriction.director.subordinates_not_empty"));
        }

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

    public Designation getMainDesignation() { return mainDesignation; }

    void setMainDesignation(Designation designation) { this.mainDesignation = designation; }
}
