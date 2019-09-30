package com.pepperkick.ems.repository;

import com.pepperkick.ems.entity.Designation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository extends CrudRepository<Designation, Integer> {
    Designation findByTitle(String title);
    Designation findById(int id);
    List<Designation> findAll();
    List<Designation> findAllByOrderByLevelAsc();
    List<Designation> findByLevel(float level);
}
