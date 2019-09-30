package com.pepperkick.ems.formatter;

import com.pepperkick.ems.entity.Employee;

import java.io.Serializable;
import java.util.Map;

public class EmployeeResponse implements Serializable {
    Map<String, Object> employee;
    Employee manager;
    Employee[] colleagues;
    Employee[] subordinates;
}
