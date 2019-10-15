package com.pepperkick.ems.util;

import com.pepperkick.ems.exception.BadRequestException;
import com.pepperkick.ems.exception.ValidationError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorHelper {
    private final MessageHelper messageHelper;

    public ValidatorHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    public void validateIdWithError(int id, String tag) {
        try {
            validateId(id);
        } catch (ValidationError e) {
            throw new BadRequestException(
                messageHelper.getMessage(tag, id)
            );
        }
    }

    private static void validateId(int id) {
        if (id < 0)
            throw new ValidationError("id", "negative");
    }

    public static void validateName(String name) {
        if (name == null)
            throw new ValidationError("name", "null");

        if (name.compareTo("") == 0)
            throw new ValidationError("name", "empty");

        if (name.length() > 30)
            throw new ValidationError("name", "tooLong");

        if (name.length() < 2)
            throw new ValidationError("name", "tooShort");

        Pattern regex = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(name);

        if (matcher.find())
            throw new ValidationError("name", "invalid");
    }
}
