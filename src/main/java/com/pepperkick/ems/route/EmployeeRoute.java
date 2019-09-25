package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/employee")
public class EmployeeRoute {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private  final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public List<Employee> get() {
        Iterable<Employee> employee = employeeRepository.findAll();
        List<Employee> employees = new ArrayList<>();

        for (Employee e : employee) {
            employees.add(e);
        }

        return employees;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity post(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String job = (String) payload.get("jobTitle");
        Integer managerId = Integer.valueOf("" + payload.get("managerId"));

        if (name.compareTo("") == 0) {
            return new ResponseEntity<String>("Name cannot be empty", HttpStatus.NOT_FOUND);
        }

        Designation designation = designationRepository.findByTitle(job);
        if (designation == null){
            return new ResponseEntity<String>("Designation not found", HttpStatus.NOT_FOUND);
        }

        Optional<Employee> manager = employeeRepository.findById(managerId);
        if (!manager.isPresent()) {
            return new ResponseEntity<String>("Manager not found", HttpStatus.NOT_FOUND);
        } else if (manager.get().getDesignation().getLevel() >= designation.getLevel()) {
            return new ResponseEntity<String>("Manager cannot be designated lower or equal level to subordinate", HttpStatus.NOT_FOUND);
        }

        Employee newEmployee = new Employee();
        newEmployee.setName(name);
        newEmployee.setManager(manager.get());
        newEmployee.setDesignation(designation);
        employeeRepository.save(newEmployee);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value= "/{id}", method= RequestMethod.GET, produces = "application/json")
    public ResponseEntity get(@PathVariable int id) {
        if (id < 0)
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);

        Optional<Employee> employee = employeeRepository.findById(id);

        if (employee.isPresent())
            return new ResponseEntity<Object>(employee, HttpStatus.OK);

        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value= "/{id}", method = RequestMethod.DELETE, produces = "application/text")
    public ResponseEntity delete(@PathVariable int id) {
        if (id < 0)
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);

        Optional<Employee> optional = employeeRepository.findById(id);
        Employee employee;

        if (optional.isPresent()) {
            employee = optional.get();
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        if (employee.getDesignation().getLevel() == 1) {
            if (!employee.getSubordinates().isEmpty())
                return new ResponseEntity(HttpStatus.METHOD_NOT_ALLOWED);
        }

        if (!employee.getSubordinates().isEmpty()) {
            Employee manager = employee.getManager();
            employee.getSubordinates().forEach(object -> {
                object.setManager(manager);
                employeeRepository.save(object);
            });
        }

        employeeRepository.delete(employee);

        return new ResponseEntity(HttpStatus.OK);
    }
}
