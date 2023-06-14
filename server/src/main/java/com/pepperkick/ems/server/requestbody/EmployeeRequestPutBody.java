package com.pepperkick.ems.server.requestbody;

import com.pepperkick.ems.server.exception.BadRequestException;
import com.pepperkick.ems.server.util.MessageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Details to update or replace employee")
public class EmployeeRequestPutBody {
    @ApiModelProperty(name = "name", value = "Name of the employee",example = "Captain America", position = 1)
    private String name;

    @ApiModelProperty(name = "jobTitle", value = "Designation of the employee", example = "Manager", position = 2)
    private String jobTitle;

    @ApiModelProperty(name = "managerId", value = "Manager ID of the employee", example = "1", position = 3)
    private int managerId = -1;

    @ApiModelProperty(name = "replace", value = "Should the employee be replaced with a new employee with the given information", example = "true", position = 4)
    private boolean replace = false;

    public void validate(MessageHelper messageHelper) {
        if (replace) {
            EmployeeRequestBody.validate(messageHelper, name, jobTitle);
        } else {
            EmployeeRequestBody.validateName(messageHelper, name);
            if ( this.name == null && this.jobTitle == null && this.managerId == -1)
                throw new BadRequestException(
                    messageHelper.getMessage("error.route.employee.update.empty.body")
                );
        }
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

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }
}
