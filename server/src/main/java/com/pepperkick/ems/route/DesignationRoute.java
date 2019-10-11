package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.DesignationPostBody;
import com.pepperkick.ems.service.DesignationService;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ResponseHelper;
import com.pepperkick.ems.util.ValidatorHelper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api/v1/designations")
public class DesignationRoute {
    private final EmployeeRepository employeeRepository;

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
        this.employeeRepository = employeeRepository;
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

        // If designation list is empty then return 404 error
        if (designations.size() == 0)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.notfound.list"),
                    HttpStatus.NOT_FOUND
            );

        // Return the designation list
        return new ResponseEntity<>(designations, HttpStatus.OK);
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ApiOperation(value = "Add a new designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created new designation"),
            @ApiResponse(code = 400, message = "Invalid post body or parameter")
    })
    public ResponseEntity post(@ApiParam(value = "Information of new designation") @RequestBody DesignationPostBody body) {
        try {
            // Validate POST body details
            body.validate(messageHelper);
        } catch (BadRequestException e) {
            // Return 400 if there are validation error
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // If POST body has no higher designation
        if (body.getHigher() == -1) {
            // Find all designations
            List<Designation> designations = designationRepository.findAll();

            // If designation list is not empty then return 400 error
            // Higher designation cannot be empty if there are existing designations
            if (designations.size() != 0)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.empty.param.higher"),
                    HttpStatus.BAD_REQUEST
                );
        }

        // Find designations with title equal to POST body name
        Designation nameDesignation = designationRepository.findByTitle(body.getName());

        // If a designation with the given title is found then return 400
        // Two designations cannot have same title
        if (nameDesignation != null) {
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.designation.restriction.same.name", body.getName()),
                HttpStatus.BAD_REQUEST
            );
        }

        // Create new designation
        Designation mewDesignation = new Designation();
        mewDesignation.setTitle(body.getName());

        // If no previous designation is present, set designation level to 1
        if (body.getHigher() == -1)
            mewDesignation.setLevel(1);
        // Else set a new designation level
        else {
            // Get designation with higher designation ID
            Designation higherDesignation = designationRepository.findById(body.getHigher());

            // If higher designation is not found then return 400
            if(higherDesignation == null)
                return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.designation.notfound.higher", body.getHigher()),
                        HttpStatus.BAD_REQUEST
                );
            // Else check if POST body equals is true
            else {
                // If equals is true then set new designation level equal to higher designation level
                if (body.isEquals())
                    mewDesignation.setLevel(higherDesignation.getLevel());
                // Else get a new level between higher designation level and next higher designation level
                else {
                    mewDesignation.setLevel(designationService.getNewDesignationLevel(higherDesignation));
                }
            }
        }

        // Save the new designation
        try {
            mewDesignation = designationRepository.save(mewDesignation);
        } catch (DataIntegrityViolationException e) {
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.db.constraint"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Return the new designation
        return new ResponseEntity<>(mewDesignation, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Delete a designation", response = Designation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted designation"),
            @ApiResponse(code = 404, message = "Designation not found"),
    })
    public ResponseEntity deleteById(@ApiParam(name = "id", value = "Designation's ID", example = "1") @PathVariable int id) {
        try {
            // Validate URL param ID
            validatorHelper.validateIdWithError(id, "error.route.designation.invalid.id");
        } catch (BadRequestException e) {
            // Return 400 if there are validation error
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Find designation by ID
        Designation designation = designationRepository.findById(id);

        // Return 404 if designation not found
        if (designation == null)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.notfound"),
                    HttpStatus.NOT_FOUND
            );

        // Find all employees with this designation
        List<Employee> employees = employeeRepository.findEmployeeByDesignation(designation);

        // If employee list is not empty then return 400
        // Cannot delete designation while employees have this designation assigned to it
        if (employees.size() != 0)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.restriction.cannot_have_employee_assigned"),
                    HttpStatus.BAD_REQUEST
            );

        // Delete the designation
        designationRepository.delete(designation);

        // Return the deleted designation
        return new ResponseEntity<>(designation, HttpStatus.OK);
    }
}
