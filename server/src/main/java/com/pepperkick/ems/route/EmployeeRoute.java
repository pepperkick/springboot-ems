package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.requestbody.EmployeeRequestPostBody;
import com.pepperkick.ems.requestbody.EmployeeRequestPutBody;
import com.pepperkick.ems.service.EmployeeService;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ResponseHelper;
import com.pepperkick.ems.util.ValidatorHelper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/employees")
public class EmployeeRoute {
    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final MessageHelper messageHelper;
    private final ValidatorHelper validatorHelper;
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeRoute(EmployeeRepository employeeRepository, DesignationRepository designationRepository, MessageHelper messageHelper, EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
        this.messageHelper = messageHelper;
        this.validatorHelper = new ValidatorHelper(messageHelper);
        this.employeeService = employeeService;
    }

    @GetMapping(produces = "application/json")
    @ApiOperation(value = "View the list of employees", response = Employee.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list"),
        @ApiResponse(code = 404, message = "No employees found"),
    })
    public ResponseEntity get() {
        // Get all employees
        List<Employee> employees = employeeRepository.findAll();

        // Sort the list according to designation and name
        employees.sort(Employee::compareTo);

        // Return employee list
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Add a new employee", response = Employee.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created new employee", response = Employee.class),
        @ApiResponse(code = 400, message = "Invalid post body or parameter")
    })
    public ResponseEntity post(@ApiParam(value = "Information of new employee") @NotNull @RequestBody EmployeeRequestPostBody body) {
        // Validate body
        body.validate(messageHelper);

        // Create new employee
        Employee employee = employeeService.create(body);

        // Return the new employee
        return new ResponseEntity<>(employee, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Get information of specific employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the employee information"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    public ResponseEntity getById(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        // Validate given ID
        validatorHelper.validateIdWithError(id, "error.route.employee.invalid.id");

        // Get employee ith the given ID
        Employee employee; employee = employeeService.findById(id);

        // Return employee
        return new ResponseEntity<Object>(employee, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", produces = "application/json", consumes = "application/json")
    @ApiOperation(value = "Update or replace an employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated the employee"),
            @ApiResponse(code = 201, message = "Successfully created a new employee and replaced the old employee"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    public ResponseEntity putById(
            @ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id,
            @ApiParam(value = "Information of employee to update") @RequestBody EmployeeRequestPutBody body
    ) {
        try {
            // Validate URL param ID
            validatorHelper.validateIdWithError(id, "error.route.employee.invalid.id");
            // Validate PUT body details
            body.validate(messageHelper);
        } catch (BadRequestException e) {
            // Return 400 if there are validation error
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Find employee by URL param ID
        Employee employee = employeeRepository.findById(id);

        // IF employee not found then return 404
        if (employee == null)
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.notfound", id),
                HttpStatus.NOT_FOUND
            );

        // If PUT body replace is true
        if (body.isReplace()) {
            // Find designation by title equal to PUT body jobTitle
            Designation designation = designationRepository.findByTitle(body.getJobTitle());
            Employee oldEmployee = employee;

            // If designation not found then return 400
            if (designation == null)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.notfound.designation", body.getJobTitle()),
                    HttpStatus.BAD_REQUEST
                );

            // If designation is equal to main designation (Director) and PUT body managerID is present then return 400
            // Director cannot have a manager
            if (designation.equalsTo(employeeService.getMainDesignation()) && body.getManagerId() != -1)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"),
                    HttpStatus.BAD_REQUEST
                );

            // Find employee with PUT body managerId
            Employee manager = employeeRepository.findById(body.getManagerId());

            // If designation is not equal to main designation (Director) and manager not found then set manager is current employee's manager
            // Any designation other then main designation (Director) must have a manager
            if (!designation.equalsTo(employeeService.getMainDesignation()) && manager == null)
                manager = employee.getManager();

            // If manager is found and manager's designation level is less than new employee's designation level then return 400
            // Manager's designation level cannot be lower than it's subordinates
            if (manager != null && manager.getDesignation().getLevel() >= designation.getLevel())
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation", designation.getTitle(), manager.getDesignation().getTitle()),
                     HttpStatus.BAD_REQUEST
                );

            // If employee's subordinates list is not empty
            if (employee.getSubordinates().size() > 0) {
                // Get current employee's designation
                Designation highest = employee.getSubordinates().first().getDesignation();

                // Check designation of each subordinate
                for (Employee sub : employee.getSubordinates()) {
                    if (sub.getDesignation().getLevel() < highest.getLevel())
                        highest = sub.getDesignation();
                }

                // If current designation level is lower then highest subordinate designation level then return 400
                // Employee designation cannot be lower than it's subordinates
                if (designation.getLevel() >= highest.getLevel())
                    return ResponseHelper.createErrorResponseEntity(
                            messageHelper.getMessage("error.route.employee.restriction.subordinate.cannot_have_higher_designation", designation.getTitle()),
                            HttpStatus.BAD_REQUEST
                    );
            }

            // Create new employee
            employee = new Employee();
            employee.setName(body.getName());
            employee.setDesignation(designation);
            employee.setManager(manager);

            // Save new employee
            employee = employeeRepository.save(employee);

            // Change manager of old employee's subordinates
            for (Employee sub : oldEmployee.getSubordinates()) {
                sub.setManager(employee);
                employeeRepository.save(sub);
            }

            // Delete old employee
            employeeRepository.delete(oldEmployee);
            employee = employeeRepository.findById((int) employee.getId());

            // Return new employee
            return new ResponseEntity<>(employee, HttpStatus.CREATED);
        } else {
            // If PUT body name is not empty then set it
            if (body.getName() != null) employee.setName(body.getName());

            // IF PUT body jobTitle is not empty
            if (body.getJobTitle() != null) {
                // Find designation with title equal to PUT body jobTitle
                Designation designation = designationRepository.findByTitle(body.getJobTitle());

                // If designation is not found then 400
                if (designation == null)
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.notfound.designation", body.getJobTitle()),
                        HttpStatus.BAD_REQUEST
                    );

                // If designation is equal to main designation (Director) then return 400
                // Cannot change designation of a employee with main designation (Director)
                if (designation.equalsTo(employeeService.getMainDesignation()))
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.restriction.director.cannot_change_designation"),
                        HttpStatus.BAD_REQUEST
                    );

                // If employee's subordinates list is not empty
                if (employee.getSubordinates().size() > 0) {
                    // Get current employee's designation
                    Designation highest = employee.getDesignation();

                    // Check designation of each subordinate
                    for (Employee sub : employee.getSubordinates()) {
                        if (sub.getDesignation().getLevel() > highest.getLevel())
                            highest = sub.getDesignation();
                    }

                    // If current designation level is lower then highest subordinate designation level then return 400
                    // Employee designation cannot be lower than it's subordinates
                    if (designation.getLevel() >= highest.getLevel())
                        return ResponseHelper.createErrorResponseEntity(
                            messageHelper.getMessage("error.route.employee.restriction.subordinate.cannot_have_higher_designation", designation.getTitle()),
                            HttpStatus.BAD_REQUEST
                        );
                }

                employee.setDesignation(designation);
            }

            // If PUT body managerId is present
            if (body.getManagerId() != -1) {
                // Find employee with ID equal to PUT body managerID
                Employee manager = employeeRepository.findById(body.getManagerId());

                // If manager not found then return 400
                if (manager == null)
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.notfound.manager", body.getManagerId()),
                        HttpStatus.BAD_REQUEST
                    );

                // If manager's designation level is less than current employee designation level then return 400
                // Manager's designation level cannot be lower than it's subordinates
                if (manager.getDesignation().getLevel() >= employee.getDesignation().getLevel())
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.restriction.subordinate.cannot_have_higher_designation", body.getJobTitle()) ,
                        HttpStatus.BAD_REQUEST
                    );

                employee.setManager(manager);
            }

            // Update the employee
            employeeRepository.save(employee);

            // Return the employee
            return new ResponseEntity<>(employee, HttpStatus.OK);
        }
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Delete an employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted the employee"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    public ResponseEntity deleteById(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        // Validate given ID
        validatorHelper.validateIdWithError(id, "error.route.employee.invalid.id");

        // Delete employee
        employeeService.deleteById(id);

        // Return status
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
