package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/employee")
public class EmployeeRoute {
    @Autowired
    private EmployeeRepository employeeRepository;
    private  final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public List<Employee> get() {
        Iterable<Employee> employee = employeeRepository.findAll();
        employee.forEach(obj -> logger.info("Employee: " + obj.getId() + " " + obj.getName() + " " + obj.getDesignation()));
        List<Employee> employees = new ArrayList<>();

        for (Employee e : employee) {
            employees.add(e);
        }

        return employees;
    }

    @RequestMapping(value= "/{id}", method= RequestMethod.GET)
    public Employee get(@PathVariable int id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        employee.ifPresent(obj -> logger.info("Employee: " + obj.getId() + " " + obj.getName() + " " + obj.getDesignation()));

        return employee.isPresent() ? employee.get() : null;
    }
}
