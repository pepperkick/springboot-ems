package com.pepperkick.ems.batch.mapper;

import com.pepperkick.ems.batch.dto.Employee;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();

        employee.setId(rs.getInt("ID"));
        employee.setName(rs.getString("NAME"));
        employee.setDesignationId(rs.getInt("DESIGNATION"));
        employee.setManagerId(rs.getInt("MANAGER"));

        return employee;
    }
}
