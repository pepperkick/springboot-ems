package com.pepperkick.ems.service;

import com.pepperkick.ems.Application;
import com.pepperkick.ems.config.H2Configuration;
import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.repository.DesignationRepository;
import com.pepperkick.ems.repository.EmployeeRepository;
import com.pepperkick.ems.util.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, H2Configuration.class })
public class DesignationServiceTests {
    private DesignationRepository designationRepository;
    private DesignationService designationService;
    private EmployeeRepository employeeRepository;

    @Autowired
    private MessageHelper messageHelper;

    @BeforeTest
    public void init() {
        designationRepository = mock(DesignationRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        designationService = new DesignationService(designationRepository, employeeRepository, messageHelper);

        Designation dummyDesignation = new Designation();
        dummyDesignation.setId(1);
        dummyDesignation.setLevel(2.0f);
        dummyDesignation.setTitle("Manager");

        Designation higherDesignation = new Designation();
        higherDesignation.setId(2);
        higherDesignation.setLevel(1.0f);
        higherDesignation.setTitle("Director");

        List<Designation> designations = new ArrayList<>();
        designations.add(dummyDesignation);
        designations.add(higherDesignation);

        when(designationRepository.findById(1)).thenReturn(higherDesignation);
        when(designationRepository.findById(2)).thenReturn(dummyDesignation);
        when(designationRepository.findAll()).thenReturn(designations);

        designations.sort(Collections.reverseOrder());
        when(designationRepository.findAllByOrderByLevelAsc()).thenReturn(designations);
    }

    @Test
    public void getNewDesignationLevel() {
        Designation designation = designationRepository.findById(1);
        float level = designationService.getNewDesignationLevel(designation);
        assert level == 1.5;
    }
}
