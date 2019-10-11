package com.pepperkick.ems.requestbody;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.ValidationError;
import com.pepperkick.ems.util.MessageHelper;
import com.pepperkick.ems.util.ValidatorHelper;

public class EmployeeBody {
    public static void validate(MessageHelper messageHelper, String name, String jobTitle) throws BadRequestException {
        validateName(messageHelper, name);

        if (jobTitle == null || jobTitle.compareTo("") == 0)
            throw new BadRequestException(
                    messageHelper.getMessage("error.route.employee.empty.param.designation")
            );
    }

    public static void validateName(MessageHelper messageHelper, String name) throws BadRequestException {
        try {
            ValidatorHelper.validateName(name);
        } catch (ValidationError e) {
            switch (e.code) {
                case "null":
                case "empty":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.employee.empty.param.name")
                    );
                case "invalid":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.employee.param.name.invalid")
                    );
                case "tooLong":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.employee.param.name.too_long")
                    );
                case "tooShort":
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.employee.param.name.too_short")
                    );
            }
        }
    }
}
