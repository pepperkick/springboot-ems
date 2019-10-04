package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.util.ResponseHelper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/employees")
public class EmployeeRoute {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DesignationRepository designationRepository;
    private Designation mainDesignation = null;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

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
            return ResponseHelper.CreateErrorResponseEntity(
                    "No employees found",
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
    public ResponseEntity post(@NotNull @RequestBody Map<String, Object> payload) {
        int managerId = -1;

        if (payload.get("name") == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Employee's name cannot be empty",
                    HttpStatus.BAD_REQUEST
            );

        if (payload.get("jobTitle") == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Employee's job title cannot be empty",
                    HttpStatus.BAD_REQUEST
            );

        String name = (String) payload.get("name");
        String jobTitle = (String) payload.get("jobTitle");

        if (payload.get("managerId") != null)
            managerId = Integer.parseInt("" + payload.get("managerId"));

        if (name.compareTo("") == 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Employee's name cannot be empty",
                    HttpStatus.BAD_REQUEST
            );

        Designation designation = designationRepository.findByTitle(jobTitle);
        if (designation == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Could not find any designation with the given job title, please make sure the job title matches a designation title",
                    HttpStatus.BAD_REQUEST
            );
        else if (designation.getLevel() == 1) {
            if (mainDesignation == null) {
                return ResponseHelper.CreateErrorResponseEntity(
                        "Unable to verify if a director is present at the moment, please try again later",
                        HttpStatus.BAD_REQUEST
                );
            }

            List<Employee> employees = employeeRepository.findEmployeeByDesignation(mainDesignation);

            if (employees.size() != 0)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Only one director can be present",
                        HttpStatus.BAD_REQUEST
                );

            if (managerId != -1)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Director cannot have a manager",
                        HttpStatus.BAD_REQUEST
                );
        }

        if (managerId == -1)
            if (designation.compareTo(mainDesignation) != 0)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Employee's job title needs to be 'director' to not have a manager",
                        HttpStatus.BAD_REQUEST
                );

        Employee newEmployee = new Employee();
        newEmployee.setName(name);
        newEmployee.setDesignation(designation);

        if (managerId != - 1) {
            Employee manager = employeeRepository.findById(managerId);
            if (manager == null)
                return ResponseHelper.CreateErrorResponseEntity(
                        "No employee found with the supplied manager ID",
                        HttpStatus.BAD_REQUEST
                );
            else if (manager.getDesignation().getLevel() >= designation.getLevel())
                return ResponseHelper.CreateErrorResponseEntity(
                        "Employee's designation cannot be higher or equal to it's manager's designation",
                        HttpStatus.BAD_REQUEST
                );

            newEmployee.setManager(manager);
        }

        newEmployee = employeeRepository.save(newEmployee);

        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get information of specific employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the employee information"),
            @ApiResponse(code = 404, message = "Employee not found"),
    })
    @RequestMapping(value= "/{id}", method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity get(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        if (id < 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "ID cannot be negative",
                    HttpStatus.BAD_REQUEST
            );

        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "No employee found with supplied employee ID",
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
    public ResponseEntity put(
            @ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id,
            @RequestBody Map<String, Object> payload
    ) {
        if (id < 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "ID cannot be negative",
                    HttpStatus.BAD_REQUEST
            );

        String name = null;
        String jobTitle = null;
        int managerId = -1;
        boolean replace = false;

        if (payload.get("name") != null)
            name = (String) payload.get("name");
        if (payload.get("jobTitle") != null)
            jobTitle = (String) payload.get("jobTitle");
        if (payload.get("managerId") != null)
            managerId = Integer.parseInt("" + payload.get("managerId"));
        if (payload.get("replace") !=  null)
            replace = Boolean.parseBoolean("" + payload.get("replace"));

        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "No employee found with the supplied employee ID",
                    HttpStatus.NOT_FOUND
            );

        if (replace) {
            if (name == null)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Employee's name cannot be empty",
                        HttpStatus.BAD_REQUEST
                );

            if (jobTitle == null)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Employee's job title cannot be empty",
                        HttpStatus.BAD_REQUEST
                );

            Designation designation = designationRepository.findByTitle(jobTitle);
            Employee oldEmployee = employee;

            if (designation.compareTo(mainDesignation) == 0 && managerId != -1)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Director cannot have a manager",
                        HttpStatus.BAD_REQUEST
                );

            Employee manager = employeeRepository.findById(managerId);
            if (designation.compareTo(mainDesignation) != 0 && manager == null)
                return ResponseHelper.CreateErrorResponseEntity(
                        "No employee found with the supplied manager ID",
                        HttpStatus.BAD_REQUEST
                );

            if (manager != null && manager.getDesignation().getLevel() >= designation.getLevel())
                return ResponseHelper.CreateErrorResponseEntity(
                        "Employee's designation cannot be higher or equal to it's manager's designation",
                         HttpStatus.BAD_REQUEST
                );

            employee = new Employee();
            employee.setName(name);
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
            if (name != null) employee.setName(name);

            if (jobTitle != null) {
                Designation designation = designationRepository.findByTitle(jobTitle);

                if (designation == null)
                    return ResponseHelper.CreateErrorResponseEntity(
                            "Could not find any designation with the supplied title",
                            HttpStatus.BAD_REQUEST
                    );

                if (designation.compareTo(mainDesignation) == 0)
                    return ResponseHelper.CreateErrorResponseEntity(
                            "Cannot change designation of director",
                            HttpStatus.BAD_REQUEST
                    );

                if (employee.getSubordinates().size() > 0) {
                    Designation highest = employee.getDesignation();

                    for (Employee sub : employee.getSubordinates()) {
                        if (sub.getDesignation().getLevel() > highest.getLevel())
                            highest = sub.getDesignation();
                    }

                    if (designation.getLevel() >= highest.getLevel())
                        return ResponseHelper.CreateErrorResponseEntity(
                                "Employee designation cannot be lower or equal to it's subordinates",
                                HttpStatus.BAD_REQUEST
                        );
                }

                employee.setDesignation(designation);
            }

            if (managerId != -1) {
                Employee manager = employeeRepository.findById(managerId);

                if (manager == null)
                    return ResponseHelper.CreateErrorResponseEntity(
                            "No employee found with supplied manager ID",
                            HttpStatus.BAD_REQUEST
                    );
                if (manager.getDesignation().getLevel() >= employee.getDesignation().getLevel())
                    return ResponseHelper.CreateErrorResponseEntity(
                            "Employee designation cannot be lower or equal to it's subordinates",
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
    public ResponseEntity delete(@ApiParam(name = "id", example = "1", value = "Employee's ID", required = true) @PathVariable int id) {
        if (id < 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "ID cannot be negative",
                    HttpStatus.BAD_REQUEST
            );

        Employee employee = employeeRepository.findById(id);;

        if (employee == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "No employee found with the supplied employee ID",
                    HttpStatus.NOT_FOUND
            );

        if (employee.getDesignation().getLevel() == 1) {
            if (!employee.getSubordinates().isEmpty())
                return ResponseHelper.CreateErrorResponseEntity(
                        "Cannot delete director when subordinates list is not empty",
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
