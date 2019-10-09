package com.pepperkick.ems.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

@Entity
@Table(name = "DESIGNATION")
public class Designation implements Comparable<Designation> {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Designation's ID", example = "1", position = 1)
    private Integer id;

    @Column(name = "TITLE", unique = true)
    @ApiModelProperty(value = "Designation's Title", example = "Director", position = 2)
    private String title;

    @Column(name = "LEVEL")
    @ApiModelProperty(value = "Designation's Level", example = "1.0", position = 3)
    private float level;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public boolean equalsTo(Designation o) { return compareTo(o) == 0; }

    @Override
    public int compareTo(Designation o) {
        return this.getId() - o.getId();
    }
}
