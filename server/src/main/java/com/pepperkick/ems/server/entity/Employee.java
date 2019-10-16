package com.pepperkick.ems.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "EMPLOYEE")
@ApiModel
public class Employee implements Comparable<Employee>, Comparator<Employee> {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "Employee's ID", position = 1)
    private Integer id;

    @Column(name = "NAME", length = 40)
    @NotNull
    @ApiModelProperty(notes = "Employee's Name", position = 2, required = true)
    private String name;

    @Transient
    @ApiModelProperty(notes = "Employee's Job Title", example = "Director", position = 3, required = true)
    private String jobTitle;

    @OneToOne
    @JoinColumn(name = "MANAGER")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @ApiModelProperty(notes = "Employee's Manager", position = 4)
    private Employee manager;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(Employee.class)
    @ApiModelProperty(notes = "Employee's Colleagues", position = 5)
    private SortedSet<Employee> colleagues;

    @OneToMany(mappedBy = "manager")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(value = {"manager", "colleagues", "subordinates"})
    @SortComparator(Employee.class)
    @ApiModelProperty(notes = "Employee's Subordinates", position = 6)
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

    public SortedSet<Employee> getSubordinates() {
        return subordinates;
    }

    private SortedSet<Employee> getSubordinates(Employee e) {
        SortedSet<Employee> temp = new TreeSet<>(subordinates);
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

        return new TreeSet<>();
    }

    @Override
    public int compareTo(Employee o) {
        return compare(this, o);
    }

    @Override
    public int compare(Employee o1, Employee o2) {
        float levelDiff = o1.getDesignation().getLevel() - o2.getDesignation().getLevel();
        if (levelDiff == 0) {
            int nameDiff = o1.getName().compareTo(o2.getName());
            if (nameDiff == 0) {
                return o1.getId() - o2.getId();
            } else {
                return nameDiff > 0 ? 1 : -1;
            }
        } else {
            return levelDiff > 0 ? 1 : -1;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Employee)) {
            return false;
        }

        Employee employee = (Employee) obj;

        return this.getId().equals(employee.getId());
    }
}
