package com.pepperkick.ems.util;

import com.pepperkick.ems.entity.Employee;

import java.util.Comparator;

public class SortEmployees implements Comparator<Employee> {
    @Override
    public int compare(Employee o1, Employee o2) {
        int levelDiff = o1.getDesignation().getLevel() - o2.getDesignation().getLevel();
        if (levelDiff == 0) {
            int nameDiff = o1.getName().compareTo(o2.getName());
            if (nameDiff == 0) {
                int idDiff = o1.getId() - o2.getId();
                return Integer.compare(idDiff, 0);
            } else {
                return nameDiff > 0 ? 1 : -1;
            }
        } else {
            return levelDiff > 0 ? 1 : -1;
        }
    }
}
