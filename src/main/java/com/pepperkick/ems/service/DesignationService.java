package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class DesignationService {
    @Autowired
    private DesignationRepository designationRepository;

    public float GetNewDesignationLevel(Designation higherDesignation) {
        SortedSet<Float> levels = new TreeSet<>();
        List<Designation> designations = designationRepository.findAll();

        for (Designation designation : designations) {
            levels.add(designation.getLevel());
        }

        int index = 0;
        for (Float level : levels) {
            if (level == higherDesignation.getLevel()) {
                break;
            }
            index++;
        }

        Float[] floats = new Float[levels.size()];

        int i = 0;
        for (Float fl : levels) {
            floats[i++] = fl;
        }

        float high = 0, low = floats[index];
        if (index == floats.length - 1) {
            return (low + 1);
        } else {
            high = floats[index + 1];
            return ((low + high) / 2);
        }
    }
}
