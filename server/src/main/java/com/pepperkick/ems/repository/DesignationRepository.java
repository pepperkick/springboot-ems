package com.pepperkick.ems.repository;

import com.pepperkick.ems.entity.Designation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository extends CrudRepository<Designation, Integer> {
    // Find a designation by ID
    Designation findById(int id);

    // Find a designation by it's title
    Designation findByTitle(String title);

    // Find all designations
    List<Designation> findAll();

    // Find all designations sorted by level in ascending order
    List<Designation> findAllByOrderByLevelAsc();

    // Find all designations of certain level
    List<Designation> findByLevel(float level);
}
