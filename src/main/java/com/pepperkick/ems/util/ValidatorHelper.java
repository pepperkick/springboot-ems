package com.pepperkick.ems.util;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.NotFoundException;
import org.springframework.stereotype.Repository;

public class ValidatorHelper {
    public static void validateId(int id, MessageHelper messageHelper) throws BadRequestException {
        if (id < 0)
            throw new BadRequestException(
                messageHelper.getMessage("error.route.employee.invalid.id", id)
            );
    }
}
