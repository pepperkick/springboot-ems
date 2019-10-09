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

        List<Employee> employees = employeeRepository.findAll();
        if (employees.size() == 0) {
            String[] names = { "Thor", "Iron Man", "Hulk", "Captain America", "War Machine", "Vision", "Falcon", "Ant Man", "Spider Man", "Black Widow" };
            int[] designations = { 1, 2, 3, 2, 6, 5, 4, 3, 7, 4 };
            int[] managers = { -1, 1, 1, 1, 2, 2, 4, 4, 2, 3};

            for (int i = 0; i < 10; i++) {
                Employee employee = new Employee();
                employee.setName(names[i]);
                employee.setDesignation(designationRepository.findById(designations[i]));
                if (managers[i] != -1)
                    employee.setManager(employeeRepository.findById(managers[i]));
                employeeRepository.save(employee);
            }
        }
    }

    public Employee findById(int id) throws NotFoundException, BadRequestException {
        return findById(id, false);
    }

    public Employee findById(int id, boolean notfound) throws NotFoundException, BadRequestException {
        Employee employee = employeeRepository.findById(id);

        if (employee == null)
            if (notfound)
                throw new NotFoundException(messageHelper.getMessage("error.route.employee.notfound"));
            else
                throw new BadRequestException(messageHelper.getMessage("error.route.employee.notfound"));

        return employee;
    }

}
