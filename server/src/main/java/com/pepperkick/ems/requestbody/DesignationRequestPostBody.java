package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.ValidationError;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ValidatorHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Details to add new designation")
public class DesignationRequestPostBody {
    @ApiModelProperty(name = "name", value = "Name of the designation", example = "Senior Manager", required = true, position = 1)
    private String name;

    @ApiModelProperty(name = "higher", value = "ID of the designation that is just higher than new designation", example = "1",position = 2)
    private int higher;

    @ApiModelProperty(name = "equals", value = "Set new designation level equal to higher designation level", example = "false", position = 3)
    private boolean equals;

    public void validate(MessageHelper messageHelper) throws BadRequestException {
        try {
            ValidatorHelper.validateName(this.name);
        } catch (ValidationError e) {
            switch (e.code) {
                case "null":
                case "empty":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.designation.empty.param.name")
                    );
                case "invalid":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.designation.param.name.invalid")
                    );
                case "tooLong":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.designation.param.name.too_long")
                    );
                case "tooShort":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.designation.param.name.too_short")
                    );
            }
        }
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
