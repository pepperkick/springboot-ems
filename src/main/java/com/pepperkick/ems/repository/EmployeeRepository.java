package com.pepperkick.ems.repository;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Integer> {
    Employee findById(int id);
    List<Employee> findAll();
    List<Employee> findEmployeeByDesignation(Designation designation);
    void deleteById(int id);
}
