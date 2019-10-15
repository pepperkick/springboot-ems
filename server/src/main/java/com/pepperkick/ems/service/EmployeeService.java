package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ResponseHelper;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final MessageHelper messageHelper;
    private Designation mainDesignation;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DesignationRepository designationRepository, MessageHelper messageHelper) {
        this.employeeRepository = employeeRepository;
        this.messageHelper = messageHelper;

        // Find main designation (Director) for later use
        // Designation with level 1 is assumed main designation
        List<Designation> designations = designationRepository.findByLevel(1);
        if (designations.size() == 1) mainDesignation = designations.get(0);
    }

    public Employee findById(int id) throws NotFoundException, BadRequestException {
        return findById(id, false);
    }

    public Employee findById(int id, boolean badRequest) throws NotFoundException, BadRequestException {
        // Find employee from repository by given ID
        Employee employee = employeeRepository.findById(id);

        // If employee is null
        if (employee == null)
            // Throw badRequest error if badRequest flag is true
            if (badRequest)
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.notfound", id));
            // Throw notFound error
            else
                throw new NotFoundException(messageHelper.getMessage("error.route.employee.notfound", id));

        return employee;
    }

    public void deleteById(int id) throws NotFoundException, BadRequestException {
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

    public Designation getMainDesignation() {
        return mainDesignation;
    }
}
