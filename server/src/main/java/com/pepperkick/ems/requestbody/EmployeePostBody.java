package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.ValidationError;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ValidatorHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Details to add new employee")
public class EmployeePostBody {
    @ApiModelProperty(name = "name", example = "Iron Man", value = "Name of the Employee", required = true, position = 1)
    private String name;

    @ApiModelProperty(name = "jobTitle", example = "Manager", value = "Designation title of the Employee", required = true, position = 2)
    private String jobTitle;

    @ApiModelProperty(name = "managerId", example = "1", value = "Manager ID of the Employee", required = false, position = 3)
    private int managerId = -1;

    public void validate(MessageHelper messageHelper) throws BadRequestException {
        EmployeeBody.validate(messageHelper, name, jobTitle);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

}
