package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.util.MessageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class EmployeePutBody {
    @ApiModelProperty(name = "name", value = "Name of the employee",example = "Captain America", position = 1)
    private String name;

    @ApiModelProperty(name = "jobTitle", value = "Designation of the employee", example = "Manager", position = 2)
    private String jobTitle;

    @ApiModelProperty(name = "managerId", value = "Manager ID of the employee", example = "1", position = 3)
    private int managerId = -1;

    @ApiModelProperty(name = "replace", value = "Should the employee be replaced with a new employee with the given information", example = "true", position = 4)
    private boolean replace = false;

    public void validate(MessageHelper messageHelper) throws BadRequestException {
        if (replace) {
            if (this.name == null || this.name.compareTo("") == 0)
                throw new BadRequestException(
                    messageHelper.getMessage("error.route.employee.empty.param.name")
                );

            if (this.name.length() > 30)
                throw new BadRequestException(
                        messageHelper.getMessage("error.route.employee.param.name.too_long")
                );

            if (this.jobTitle == null || this.jobTitle.compareTo("") == 0)
                throw new BadRequestException(
                    messageHelper.getMessage("error.route.employee.empty.param.designation")
                );
        } else {
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
