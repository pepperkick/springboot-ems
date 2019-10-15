package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.EmployeeRequestPostBody;
import com.pepperkick.ems.requestbody.EmployeeRequestPutBody;
import com.pepperkick.ems.service.EmployeeService;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ValidatorHelper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/employees")
public class EmployeeRoute {
    private static final String TAG_INVALID_ID = "error.route.employee.invalid.id";
    private final EmployeeRepository employeeRepository;
    private final MessageHelper messageHelper;
    private final ValidatorHelper validatorHelper;
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeRoute(EmployeeRepository employeeRepository, MessageHelper messageHelper, EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
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
        validatorHelper.validateIdWithError(id, TAG_INVALID_ID);

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
        // Validate URL param ID
        validatorHelper.validateIdWithError(id, TAG_INVALID_ID);
        // Validate PUT body details
        body.validate(messageHelper);

        // Find employee by URL param ID
        Employee employee = employeeService.put(id, body);

        // If PUT body replace is true
        if (body.isReplace()) {
            // Return new employee
            return new ResponseEntity<>(employee, HttpStatus.CREATED);
        } else {
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
        validatorHelper.validateIdWithError(id, TAG_INVALID_ID);

        // Delete employee
        employeeService.deleteById(id);

        // Return status
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
