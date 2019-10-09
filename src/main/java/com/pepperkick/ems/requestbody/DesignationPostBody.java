package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.util.MessageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Details to add new designation")
public class DesignationPostBody {
    @ApiModelProperty(name = "name", value = "Name of the designation", example = "Senior Manager", required = true, position = 1)
    private String name;

    @ApiModelProperty(name = "higher", value = "ID of the designation that is just higher than new designation", example = "1",position = 2)
    private int higher;

    @ApiModelProperty(name = "equals", value = "Set new designation level equal to higher designation level", example = "false", position = 3)
    private boolean equals;

    public void validate(MessageHelper messageHelper) throws BadRequestException {
        if (this.name == null || this.name.compareTo("") == 0)
            throw new BadRequestException(
                messageHelper.getMessage("error.route.designation.empty.param.name")
            );

        if (this.name.length() > 30)
            throw new BadRequestException(
                    messageHelper.getMessage("error.route.designation.param.name.too_long")
            );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHigher() {
        return higher;
    }

    public void setHigher(int higher) {
        this.higher = higher;
    }

    public boolean isEquals() {
        return equals;
    }

    public void setEquals(boolean equals) {
        this.equals = equals;
    }
}
