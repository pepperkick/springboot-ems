package com.pepperkick.ems.server.requestbody;

import com.pepperkick.ems.server.exception.BadRequestException;
import com.pepperkick.ems.server.exception.ValidationError;
import com.pepperkick.ems.server.util.MessageHelper;
import com.pepperkick.ems.server.util.ValidatorHelper;

public class EmployeeRequestBody {
    private EmployeeRequestBody () {}

    public static void validate(MessageHelper messageHelper, String name, String jobTitle) {
        validateName(messageHelper, name);

        if (jobTitle == null || jobTitle.compareTo("") == 0)
            throw new BadRequestException(
                    messageHelper.getMessage("error.route.employee.empty.param.designation")
            );
    }

    public static void validateName(MessageHelper messageHelper, String name) {
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
                default:
                    throw new BadRequestException(
                            messageHelper.getMessage("error.route.unknown_error")
                    );
            }
        }
    }
}
