package com.pepperkick.ems.batch.processor;

import com.pepperkick.ems.batch.dto.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee, String> {
    private Logger logger = LoggerFactory.getLogger(EmployeeProcessor.class);

    @Override
    public String process(Employee employee) {
        logger.info("Processing employee " + employee.getName() + " (" + employee.getId() + ")");
        return employee.toString();
    }
}
