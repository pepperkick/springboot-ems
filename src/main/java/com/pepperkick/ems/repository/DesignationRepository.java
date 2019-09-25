package com.pepperkick.ems.repository;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.entity.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignationRepository extends CrudRepository<Designation, Integer> {
    Designation findByTitle(String title);
}
