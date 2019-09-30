package com.pepperkick.ems.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.SortedSet;

@Entity
@Table(name = "EMPLOYEE")
public class Employee implements Comparable<Employee>, Comparator<Employee> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Transient
    private String jobTitle;

    @Nullable
    @OneToOne
    @JoinColumn(name = "MANAGER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    private Employee manager;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(Employee.class)
    private SortedSet<Employee> colleagues;

    @OneToMany
    @JoinColumn(name = "MANAGER")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(Employee.class)
    private SortedSet<Employee> subordinates;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "DESIGNATION")
    private Designation designation;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public String getJobTitle() {
        return designation.getTitle();
    }

    public void setJobTitle(String jobTitle) {
//        this.jobTitle = jobTitle;
    }

    public SortedSet<Employee> getSubordinates() {
        return subordinates;
    }

    public SortedSet<Employee> getSubordinates(Employee e) {
        SortedSet<Employee> temp = subordinates;
        temp.remove(e);
        return temp;
    }

    @Nullable
    public Employee getManager() {
        return manager;
    }

    public void setManager(@Nullable Employee manager) {
        this.manager = manager;
    }

    public SortedSet<Employee> getColleagues() {
        if (manager != null)
            return manager.getSubordinates(this);

        return null;
    }

    @Override
    public int compareTo(Employee o) {
        return this.compare(this, o);
    }

    @Override
    public int compare(Employee o1, Employee o2) {
        float levelDiff = o1.getDesignation().getLevel() - o2.getDesignation().getLevel();
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
