package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.requestbody.DesignationRequestPostBody;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.crypto.Des;

import java.util.List;

@Service
public class DesignationService {
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final MessageHelper messageHelper;

    @Autowired
    public DesignationService(DesignationRepository designationRepository, EmployeeRepository employeeRepository, MessageHelper messageHelper) {
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
        this.messageHelper = messageHelper;

        // Check if designation table is empty, if yes then fill with initial data
        List<Designation> designations = designationRepository.findAll();
        if (designations.size() == 0) {
            String[] titles = { "Director", "Manager", "Lead", "Developer", "DevOps", "QA", "Intern" };
            float[] levels = { 1, 2, 3, 4, 4, 4, 5 };

            for (int i = 0; i < titles.length; i++) {
                Designation designation = new Designation();
                designation.setTitle(titles[i]);
                designation.setLevel(levels[i]);
                designationRepository.save(designation);
            }
        }
    }

    // Get designation level that is between two designations
    public float getNewDesignationLevel(Designation higherDesignation) {
        boolean flag = false;
        Designation highest = higherDesignation;

        // Get list of all designations ordered by level
        List<Designation> designations = designationRepository.findAllByOrderByLevelAsc();

        // Get index of highest designation in the list
        int index = designations.indexOf(higherDesignation);

        // Loop through the list to find the next highest designation by level
        while (!flag) {
            try {
                Designation temp = designations.get(index++);

                if (temp.getLevel() > highest.getLevel()) {
                    highest = temp;
                    flag = true;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        // Return average of two designation levels if higher designation is found otherwise send highest designation level plus 1
        return flag ? (highest.getLevel() + higherDesignation.getLevel()) / 2 : higherDesignation.getLevel() + 1;
    }

    public Designation findById(int id) {
        return findById(id, false);
    }

    public Designation findById(int id, boolean badRequest) {
        return findById(id, badRequest, "error.route.designation.notfound");
    }

    public Designation findById(int id, boolean badRequest, String tag) {
        Designation designation = designationRepository.findById(id);

        // If employee is null
        if (designation == null)
            // Throw badRequest error if badRequest flag is true
            if (badRequest)
                throw new BadRequestException(messageHelper.getMessage(tag, id));
            // Throw notFound error
            else
                throw new NotFoundException(messageHelper.getMessage(tag, id));

        return designation;
    }

    public Designation create(DesignationRequestPostBody body) {
        return create(body.getName(), body.getHigher(), body.isEquals());
    }

    public Designation create(String name, int higher, boolean equal) {
        // If POST body has no higher designation
        if (higher == -1) {
            // Find all designations
            List<Designation> designations = designationRepository.findAll();

            // If designation list is not empty then return 400 error
            // Higher designation cannot be empty if there are existing designations
            if (designations.size() != 0)
                throw new BadRequestException(messageHelper.getMessage("error.route.designation.empty.param.higher"));
        }

        // Find designations with title equal to POST body name
        Designation nameDesignation = designationRepository.findByTitle(name);

        // If a designation with the given title is found then return 400
        // Two designations cannot have same title
        if (nameDesignation != null) {
            throw new BadRequestException(messageHelper.getMessage("error.route.designation.restriction.same.name", name));
        }

        // Create new designation
        Designation mewDesignation = new Designation();
        mewDesignation.setTitle(name);

        // If no previous designation is present, set designation level to 1
        if (higher == -1)
            mewDesignation.setLevel(1);
        // Else set a new designation level
        else {
            // Get designation with higher designation ID
            Designation higherDesignation = findById(higher, true, "error.route.designation.notfound.higher");

            // If equals is true then set new designation level equal to higher designation level
            if (equal)
                mewDesignation.setLevel(higherDesignation.getLevel());
            // Else get a new level between higher designation level and next higher designation level
            else
                mewDesignation.setLevel(getNewDesignationLevel(higherDesignation));
        }

        // Save the new designation
        try {
            mewDesignation = designationRepository.save(mewDesignation);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(messageHelper.getMessage("error.route.designation.db.constraint"));
        }

        return mewDesignation;
    }

    public void deleteById(int id) {
        // Get designation ith the given ID
        Designation designation = findById(id);

        // Find all employees with this designation
        List<Employee> employees = employeeRepository.findEmployeeByDesignation(designation);

        // If employee list is not empty then return 400
        // Cannot delete designation while employees have this designation assigned to it
        if (employees.size() != 0)
            throw new BadRequestException(messageHelper.getMessage("error.route.designation.restriction.cannot_have_employee_assigned"));

        // Delete the designation
        designationRepository.delete(designation);

    }
}
