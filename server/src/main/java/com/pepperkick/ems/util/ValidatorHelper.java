package com.pepperkick.ems.util;

import com.pepperkick.ems.exception.BadRequestException;

public class ValidatorHelper {
    private final MessageHelper messageHelper;

    public ValidatorHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    public void validateId(int id) throws BadRequestException {
        if (id < 0)
            throw new BadRequestException(
                messageHelper.getMessage("error.route.employee.invalid.id", id)
            );
    }
}
