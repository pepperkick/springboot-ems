package com.pepperkick.ems.entity;

import com.pepperkick.ems.object.Designation;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @Nullable
    @OneToOne
    @JoinColumn(name = "PARENT")
    private Employee parent;

    @Column(name = "DESIGNATION")
    private Designation designation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getParent() {
        return parent;
    }

    public void setParent(Employee parent) {
        this.parent = parent;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }
}
