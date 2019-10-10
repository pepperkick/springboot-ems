package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.util.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final MessageHelper messageHelper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DesignationRepository designationRepository, MessageHelper messageHelper) {
        this.employeeRepository = employeeRepository;
        this.messageHelper = messageHelper;
    }

    public Employee findById(int id) throws NotFoundException, BadRequestException {
        return findById(id, false);
    }

    public Employee findById(int id, boolean notfound) throws NotFoundException, BadRequestException {
        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            if (notfound)
                throw new NotFoundException(messageHelper.getMessage("error.route.employee.notfound", id));
            else
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.notfound", id));

        return employee;
    }

}
