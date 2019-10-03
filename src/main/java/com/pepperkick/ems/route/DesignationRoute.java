package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.service.DesignationService;
import com.pepperkick.ems.util.ResponseHelper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/designations")
public class DesignationRoute {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DesignationService designationService;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @ApiOperation(value = "View the list of designations", response = Designation.class)
    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity get() {
        List<Designation> designations = designationRepository.findAllByOrderByLevelAsc();

        if (designations.size() == 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "No designations found",
                    HttpStatus.NOT_FOUND
            );

        return new ResponseEntity<>(designations, HttpStatus.OK);
    }

    @ApiOperation(value = "Add a new designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created new designation"),
            @ApiResponse(code = 400, message = "Invalid post body or parameter")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity post(@RequestBody Map<String, Object> payload) {
        String name = null;
        int higher = -1;
        boolean equals = false;

        if (payload.get("name") != null)
            name = (String) payload.get("name");
        if (payload.get("higher") != null)
            higher = Integer.parseInt("" + payload.get("higher"));
        if (payload.get("equals") != null)
            equals = Boolean.parseBoolean("" + payload.get("equals"));

        if (name == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Designation's name cannot be empty",
                    HttpStatus.BAD_REQUEST
            );

        if (higher == -1) {
            List<Designation> designations = designationRepository.findAll();
            if (designations.size() != 0)
                return ResponseHelper.CreateErrorResponseEntity(
                        "Higher designation ID is required as the designation list is not empty",
                        HttpStatus.BAD_REQUEST
                );
        }

        Designation nameDesignation = designationRepository.findByTitle(name);
        if (nameDesignation != null) {
            return ResponseHelper.CreateErrorResponseEntity(
                    "Designation with same name already exists",
                    HttpStatus.BAD_REQUEST
            );
        }

        Designation mewDesignation = new Designation();
        mewDesignation.setTitle(name);

        if (higher == -1)
            mewDesignation.setLevel(1);
        else {
            Designation higherDesignation = designationRepository.findById(higher);

            if(higherDesignation == null)
                return ResponseHelper.CreateErrorResponseEntity(
                        "No designation found with high designation ID",
                        HttpStatus.BAD_REQUEST
                );
            else {
                if (equals)
                    mewDesignation.setLevel(higherDesignation.getLevel());
                else {
                    mewDesignation.setLevel(designationService.GetNewDesignationLevel(higherDesignation));
                }
            }
        }

        designationRepository.save(mewDesignation);

        return new ResponseEntity<>(mewDesignation, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted designation"),
            @ApiResponse(code = 404, message = "Designation not found"),
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@ApiParam(name = "id", value = "Designation's ID", example = "1") @PathVariable int id) {
        if (id < 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "ID cannot be negative",
                    HttpStatus.BAD_REQUEST
            );

        Designation designation = designationRepository.findById(id);
        if (designation == null)
            return ResponseHelper.CreateErrorResponseEntity(
                    "No designation found with the supplied ID",
                    HttpStatus.NOT_FOUND
            );

        List<Employee> employees = employeeRepository.findEmployeeByDesignation(designation);
        if (employees.size() != 0)
            return ResponseHelper.CreateErrorResponseEntity(
                    "Cannot remove a designation while employees are assigned to it",
                    HttpStatus.BAD_REQUEST
            );

        designationRepository.delete(designation);

        return new ResponseEntity<>(designation, HttpStatus.OK);
    }
}
