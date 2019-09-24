package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Controller
public class EmployeeRoute {
    @Autowired
    private EmployeeRepository employeeRepository;
    private  final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @RequestMapping(value= "/employee", method=  RequestMethod.GET)
    public String get() {
        Iterable<Employee> employee = employeeRepository.findAll();
        employee.forEach(obj -> logger.info("Employee: " + obj.getId() + " " + obj.getName() + " " + obj.getDesignation()));
        return "employeeList";
    }


    @RequestMapping(value= "/employee/{id}", method=  RequestMethod.GET)
    public String get(@PathVariable int id) {
        logger.info("Requesting info for ID: " + id);
        Optional<Employee> employee = employeeRepository.findById(id);
        employee.ifPresent(obj -> logger.info("Employee: " + obj.getId() + " " + obj.getName() + " " + obj.getDesignation()));
       return "employeeList";
    }
}
