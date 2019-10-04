package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.entity.Employee;
import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.util.MessageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Employee.class)
public class EmployeePostBody {
    @ApiModelProperty(name = "name", example = "Iron Man", value = "Name of the Employee", required = true, position = 1)
    private String name;

    @ApiModelProperty(name = "jobTitle", example = "Manager", value = "Designation title of the Employee", required = true, position = 2)
    private String jobTitle;

    @ApiModelProperty(name = "managerId", example = "1", value = "Manager ID of the Employee", required = false, position = 3)
    private int managerId = -1;

    public void validate(MessageHelper messageHelper) throws BadRequestException {
        if (this.name == null || this.name.compareTo("") == 0)
            throw new BadRequestException(
                messageHelper.getMessage("error.route.employee.empty.param.name")
            );

        if (this.jobTitle == null || this.jobTitle.compareTo("") == 0)
            throw new BadRequestException(
                messageHelper.getMessage("error.route.employee.empty.param.designation")
            );
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
