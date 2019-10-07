package com.pepperkick.ems.service;

import com.pepperkick.ems.entity.Designation;
import com.pepperkick.ems.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesignationService {
    private final DesignationRepository designationRepository;

    @Autowired
    public DesignationService(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;

        List<Designation> designations = designationRepository.findAll();
        if (designations.size() == 0) {
            String[] titles = { "Director", "Manager", "Lead", "Developer", "DevOps", "QA", "Intern" };
            float[] levels = { 1, 2, 3, 4, 4, 4, 5 };

            for (int i = 0; i < titles.length; i++) {
                Designation designation = new Designation();
                designation.setTitle(titles[i]);
                designation.setLevel(levels[i]);
                designationRepository.save(designation);
            }
        }
    }

    public float getNewDesignationLevel(Designation higherDesignation) {
        boolean flag = false;
        Designation highest = higherDesignation;
        List<Designation> designations = designationRepository.findAllByOrderByLevelAsc();

        int index = designations.indexOf(higherDesignation);

        while (!flag) {
            try {
                Designation temp = designations.get(index++);

                if (temp.getLevel() > highest.getLevel()) {
                    highest = temp;
                    flag = true;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        return flag ? (highest.getLevel() + higherDesignation.getLevel()) / 2 : higherDesignation.getLevel() + 1;
    }
}
