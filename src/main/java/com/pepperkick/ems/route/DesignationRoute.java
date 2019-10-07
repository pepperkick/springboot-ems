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

    private final DesignationRepository designationRepository;
    private final DesignationService designationService;
    private final MessageHelper messageHelper;
    private final ValidatorHelper validatorHelper;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    public DesignationRoute(DesignationService designationService, DesignationRepository designationRepository, MessageHelper messageHelper) {
        this.designationService = designationService;
        this.designationRepository = designationRepository;
        this.messageHelper = messageHelper;
        this.validatorHelper = new ValidatorHelper(messageHelper);
    }

    @ApiOperation(value = "View the list of designations", response = Designation.class)
    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity get() {
        List<Designation> designations = designationRepository.findAllByOrderByLevelAsc();

        if (designations.size() == 0)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.notfound.list"),
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
    public ResponseEntity post(@ApiParam(value = "Information of new designation") @RequestBody DesignationPostBody body) {
        try {
            body.validate(messageHelper);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (body.getHigher() == -1) {
            List<Designation> designations = designationRepository.findAll();
            if (designations.size() != 0)
                return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.empty.param.higher"),
                    HttpStatus.BAD_REQUEST
                );
        }

        Designation nameDesignation = designationRepository.findByTitle(body.getName());
        if (nameDesignation != null) {
            return ResponseHelper.createErrorResponseEntity(
                messageHelper.getMessage("error.route.designation.restriction.same.name", body.getName()),
                HttpStatus.BAD_REQUEST
            );
        }

        Designation mewDesignation = new Designation();
        mewDesignation.setTitle(body.getName());

        if (body.getHigher() == -1)
            mewDesignation.setLevel(1);
        else {
            Designation higherDesignation = designationRepository.findById(body.getHigher());

            if(higherDesignation == null)
                return ResponseHelper.createErrorResponseEntity(
                        messageHelper.getMessage("error.route.designation.notfound.higher", body.getHigher()),
                        HttpStatus.BAD_REQUEST
                );
            else {
                if (body.isEquals())
                    mewDesignation.setLevel(higherDesignation.getLevel());
                else {
                    mewDesignation.setLevel(designationService.getNewDesignationLevel(higherDesignation));
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
    public ResponseEntity deleteById(@ApiParam(name = "id", value = "Designation's ID", example = "1") @PathVariable int id) {
        try {
            validatorHelper.validateId(id);
        } catch (BadRequestException e) {
            return ResponseHelper.createErrorResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Designation designation = designationRepository.findById(id);
        if (designation == null)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.notfound"),
                    HttpStatus.NOT_FOUND
            );

        List<Employee> employees = employeeRepository.findEmployeeByDesignation(designation);
        if (employees.size() != 0)
            return ResponseHelper.createErrorResponseEntity(
                    messageHelper.getMessage("error.route.designation.restriction.cannot_have_employee_assigned"),
                    HttpStatus.BAD_REQUEST
            );

        designationRepository.delete(designation);

        return new ResponseEntity<>(designation, HttpStatus.OK);
    }
}
