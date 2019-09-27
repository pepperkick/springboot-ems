package com.pepperkick.ems.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pepperkick.ems.util.SortEmployees;
import org.hibernate.annotations.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.SortedSet;

@Entity
@Table(name = "EMPLOYEE")
public class Employee {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(SortEmployees.class)
    private SortedSet<Employee> colleagues;

    @OneToMany
    @JoinColumn(name = "MANAGER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(SortEmployees.class)
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
}
