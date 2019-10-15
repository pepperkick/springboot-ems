package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.DesignationRequestPostBody;
import com.pepperkick.ems.service.DesignationService;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ValidatorHelper;
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
    private final DesignationRepository designationRepository;
    private final DesignationService designationService;
    private final MessageHelper messageHelper;
    private final ValidatorHelper validatorHelper;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @Autowired
    public DesignationRoute(DesignationService designationService, DesignationRepository designationRepository, MessageHelper messageHelper, EmployeeRepository employeeRepository) {
        this.designationService = designationService;
        this.designationRepository = designationRepository;
        this.messageHelper = messageHelper;
        this.validatorHelper = new ValidatorHelper(messageHelper);
    }

    @GetMapping(produces = "application/json")
    @ApiOperation(value = "View the list of designations", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the list"),
            @ApiResponse(code = 404, message = "No designations found"),
    })
    public ResponseEntity get() {
        // Get all designations ordered by level
        List<Designation> designations = designationRepository.findAllByOrderByLevelAsc();

        // Return the designation list
        return new ResponseEntity<>(designations, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Get information of specific designation", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the designation information"),
            @ApiResponse(code = 404, message = "Designation not found"),
    })
    public ResponseEntity getById(@PathVariable int id) {
        // Validate ID
        validatorHelper.validateIdWithError(id, "error.route.designation.invalid.id");

        // Get all designations ordered by level
        Designation designations = designationService.findById(id);

        // Return the designation list
        return new ResponseEntity<>(designations, HttpStatus.OK);
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ApiOperation(value = "Add a new designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created new designation"),
            @ApiResponse(code = 400, message = "Invalid post body or parameter")
    })
    public ResponseEntity post(@ApiParam(value = "Information of new designation") @RequestBody DesignationRequestPostBody body) {
        // Validate POST body details
        body.validate(messageHelper);

        // Create new designation
        Designation designation = designationService.create(body);

        // Return the new designation
        return new ResponseEntity<>(designation, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Delete a designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted designation"),
            @ApiResponse(code = 404, message = "Designation not found"),
    })
    public ResponseEntity deleteById(@ApiParam(name = "id", value = "Designation's ID", example = "1") @PathVariable int id) {
        // Validate given ID
        validatorHelper.validateIdWithError(id, "error.route.designation.invalid.id");

        // Delete designation by ID
        designationService.deleteById(id);

        // Return the status
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
