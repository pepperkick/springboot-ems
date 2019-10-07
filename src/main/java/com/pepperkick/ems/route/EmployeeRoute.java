package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.requestbody.EmployeePostBody;
import com.pepperkick.ems.requestbody.EmployeePutBody;
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
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final MessageHelper messageHelper;
    private final ValidatorHelper validatorHelper;

    private Designation mainDesignation = null;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @Autowired
    public EmployeeRoute(EmployeeRepository employeeRepository, DesignationRepository designationRepository, MessageHelper messageHelper) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
        this.messageHelper = messageHelper;
        this.validatorHelper = new ValidatorHelper(messageHelper);
    }

    @PostConstruct
    public void init() {
        List<Designation> designations = designationRepository.findByLevel(1);
        if (designations.size() == 1) mainDesignation = designations.get(0);
    }

    @ApiOperation(value = "View the list of employees", response = Employee.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list"),
        @ApiResponse(code = 404, message = "No employees found"),
    })
    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity get() {
        List<Employee> employees = employeeRepository.findAll();

        if (employees.size() == 0)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.notfound.list"),
                    HttpStatus.NOT_FOUND
            );

        employees.sort(null);

        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @ApiOperation(value = "Add a new employee", response = Employee.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created new employee", response = Employee.class),
        @ApiResponse(code = 400, message = "Invalid post body or parameter")
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity post(@ApiParam(value = "Information of new employee") @NotNull @RequestBody EmployeePostBody body) {
        try {
            body.validate(messageHelper);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Designation designation = designationRepository.findByTitle(body.getJobTitle());
        if (designation == null)
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.notfound.designation", body.getJobTitle()),
                HttpStatus.BAD_REQUEST
            );
        else if (designation.getLevel() == 1) {
            if (mainDesignation == null) {
                return ResponseHelper.createErrorResponseEntity(
                    "Unable to verify if a director is present at the moment, please try again later",
                    HttpStatus.BAD_REQUEST
                );
            }

            List<Employee> employees = employeeRepository.findEmployeeByDesignation(mainDesignation);

            if (employees.size() != 0)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.single"),
                    HttpStatus.BAD_REQUEST
                );

            if (body.getManagerId() != -1)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"),
                    HttpStatus.BAD_REQUEST
                );
        }

        if (body.getManagerId() == -1)
            if (designation.compareTo(mainDesignation) != 0)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.can_only_have_no_manager"),
                    HttpStatus.BAD_REQUEST
                );

        Employee newEmployee = new Employee();
        newEmployee.setName(body.getName());
        newEmployee.setDesignation(designation);

        if (body.getManagerId() != - 1) {
            Employee manager = employeeRepository.findById(body.getManagerId());
            if (manager == null)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.notfound.manager", body.getManagerId()),
                    HttpStatus.BAD_REQUEST
                );
            else if (manager.getDesignation().getLevel() >= designation.getLevel())
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation",  designation.getTitle(), manager.getDesignation().getTitle()),
                    HttpStatus.BAD_REQUEST
                );

            newEmployee.setManager(manager);
        }

        try {
            newEmployee = employeeRepository.save(newEmployee);
        } catch (DataIntegrityViolationException e) {
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.db.constraint"),
                HttpStatus.BAD_REQUEST
            );
        }

        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get information of specific employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the employee information"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    @RequestMapping(value= "/{id}", method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity getById(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        try {
            validatorHelper.validateId(id);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.notfound"),
                HttpStatus.NOT_FOUND
            );

        return new ResponseEntity<Object>(employee, HttpStatus.OK);
    }

    @ApiOperation(value = "Update or replace an employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated the employee"),
            @ApiResponse(code = 201, message = "Succ    essfully created a new employee and replaced the old employee"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    @RequestMapping(value= "/{id}", method= RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public ResponseEntity putById(
            @ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id,
            @ApiParam(value = "Information of employee to update") @RequestBody EmployeePutBody body
    ) {
        try {
            validatorHelper.validateId(id);
            body.validate(messageHelper);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.notfound", id),
                HttpStatus.NOT_FOUND
            );

        if (body.isReplace()) {
            Designation designation = designationRepository.findByTitle(body.getJobTitle());
            Employee oldEmployee = employee;

            if (designation == null)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.notfound.designation", body.getJobTitle()),
                    HttpStatus.BAD_REQUEST
                );

            if (designation.compareTo(mainDesignation) == 0 && body.getManagerId() != -1)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.cannot_have_manager"),
                    HttpStatus.BAD_REQUEST
                );

            Employee manager = employeeRepository.findById(body.getManagerId());
            if (designation.compareTo(mainDesignation) != 0 && manager == null)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.notfound.manager", body.getManagerId()),
                    HttpStatus.BAD_REQUEST
                );

            if (manager != null && manager.getDesignation().getLevel() >= designation.getLevel())
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.manager.cannot_have_lower_designation", designation.getTitle(), manager.getDesignation().getTitle()),
                     HttpStatus.BAD_REQUEST
                );

            employee = new Employee();
            employee.setName(body.getName());
            employee.setDesignation(designation);
            employee.setManager(manager);

            employeeRepository.save(employee);

            for (Employee sub : oldEmployee.getSubordinates()) {
                sub.setManager(employee);
                employeeRepository.save(sub);
            }

            employeeRepository.delete(oldEmployee);
            return new ResponseEntity<>(employee, HttpStatus.CREATED);
        } else {
            if (body.getName() != null) employee.setName(body.getName());

            if (body.getJobTitle() != null) {
                Designation designation = designationRepository.findByTitle(body.getJobTitle());

                if (designation == null)
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.notfound.designation", body.getJobTitle()),
                        HttpStatus.BAD_REQUEST
                    );

                if (designation.compareTo(mainDesignation) == 0)
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.restriction.director.cannot_change_designation"),
                        HttpStatus.BAD_REQUEST
                    );

                if (employee.getSubordinates().size() > 0) {
                    Designation highest = employee.getDesignation();

                    for (Employee sub : employee.getSubordinates()) {
                        if (sub.getDesignation().getLevel() > highest.getLevel())
                            highest = sub.getDesignation();
                    }

                    if (designation.getLevel() >= highest.getLevel())
                        return ResponseHelper.createErrorResponseEntity(
                            messageHelper.getMessage("error.route.employee.restriction.subordinate.cannot_have_higher_designation", designation.getTitle()),
                            HttpStatus.BAD_REQUEST
                        );
                }

                employee.setDesignation(designation);
            }

            if (body.getManagerId() != -1) {
                Employee manager = employeeRepository.findById(body.getManagerId());

                if (manager == null)
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.notfound.manager", body.getManagerId()),
                        HttpStatus.BAD_REQUEST
                    );
                if (manager.getDesignation().getLevel() >= employee.getDesignation().getLevel())
                    return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.employee.restriction.subordinate.cannot_have_higher_designation", body.getJobTitle()) ,
                        HttpStatus.BAD_REQUEST
                    );

                employee.setManager(manager);
            }

            employeeRepository.save(employee);
            return new ResponseEntity<>(employee, HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Delete an employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted the employee"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    @RequestMapping(value= "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity deleteById(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        try {
            validatorHelper.validateId(id);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Employee employee = employeeRepository.findById(id);;

        if (employee == null)
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.employee.notfound"),
                HttpStatus.NOT_FOUND
            );

        if (employee.getDesignation().getLevel() == 1) {
            if (!employee.getSubordinates().isEmpty())
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.employee.restriction.director.subordinates_not_empty"),
                    HttpStatus.BAD_REQUEST
                );
        }

        if (!employee.getSubordinates().isEmpty()) {
            Employee manager = employee.getManager();
            employee.getSubordinates().forEach(object    -> {
                object.setManager(manager);
                employeeRepository.save(object);
            });
        }

        employeeRepository.delete(employee);

        return new ResponseEntity<>(employee, HttpStatus.OK);
    }
}