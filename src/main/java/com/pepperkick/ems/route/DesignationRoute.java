package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/designation")
public class DesignationRoute {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public List<Designation> get() {
        return designationRepository.findAllByOrderByLevelAsc();
    }

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
            return new ResponseEntity<>("Name cannot be empty", HttpStatus.NOT_ACCEPTABLE);

        if (higher == -1) {
            List<Designation> designations = designationRepository.findAll();
            if (designations.size() != 0)
                return new ResponseEntity<>("Designation list is not empty", HttpStatus.NOT_ACCEPTABLE);
        }

        Designation nameDesignation = designationRepository.findByTitle(name);
        if (nameDesignation != null) {
            return new ResponseEntity<>("Designation with same name already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        Designation mewDesignation = new Designation();
        mewDesignation.setTitle(name);

        if (higher == -1)
            mewDesignation.setLevel(1);
        else {
            Designation higherDesignation = designationRepository.findById(higher);

            if(higherDesignation == null)
                return new ResponseEntity<>("Higher designation not found", HttpStatus.NOT_FOUND);
            else {
                if (equals)
                    mewDesignation.setLevel(higherDesignation.getLevel());
                else {
                    SortedSet<Float> levels = new TreeSet<>();
                    List<Designation> designations = designationRepository.findAll();

                    for (Designation designation : designations) {
                        levels.add(designation.getLevel());
                    }

                    int index = 0;
                    for (Float level : levels) {
                        if (level == higherDesignation.getLevel()) {
                            break;
                        }
                        index++;
                    }

                    Float[] floats = new Float[levels.size()];

                    int i = 0;
                    for (Float fl : levels) {
                        floats[i++] = fl;
                    }

                    float high = 0, low = floats[index];
                    if (index == floats.length - 1) {
                        mewDesignation.setLevel(low + 1);
                    } else {
                        high = floats[index + 1];
                        mewDesignation.setLevel((low + high) / 2);
                    }
                }
            }
        }

        designationRepository.save(mewDesignation);

        return new ResponseEntity<>(mewDesignation, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable int id) {
        if (id < 0)
            return new ResponseEntity<>("ID cannot be negative", HttpStatus.NOT_ACCEPTABLE);

        Designation designation = designationRepository.findById(id);
        if (designation == null)
            return new ResponseEntity<>("Designation not found", HttpStatus.NOT_FOUND);

        List<Employee> employees = employeeRepository.findEmployeeByDesignation(designation);
        if (employees.size() != 0)
            return new ResponseEntity<>("Employees are present for this designation", HttpStatus.NOT_ACCEPTABLE);

        designationRepository.delete(designation);

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
