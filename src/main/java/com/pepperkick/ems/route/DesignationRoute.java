package com.pepperkick.ems.route;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/designation")
public class DesignationRoute {
    @Autowired
    private DesignationRepository designationRepository;

    private final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    @RequestMapping(method= RequestMethod.GET, produces = "application/json")
    public List<Designation> get() {
        return designationRepository.findAllByOrderByLevelAsc();
    }
}
