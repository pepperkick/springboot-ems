package com.pepperkick.ems.server.repository;

import com.pepperkick.ems.server.entity.Designation;
import com.pepperkick.ems.server.entity.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
    // Find employee of specific ID
    Employee findById(int id);

    // Find all employees
    List<Employee> findAll();

    // Find all employees of specific designations
    List<Employee> findEmployeeByDesignation(Designation designation);
}
