package com.pepperkick.ems.exception;

public class ValidationError extends RuntimeException {
    public final String tag;
    public final String code;

    public ValidationError(String tag, String code) {
        super("Validation error while checking " + tag + " due to error " + code);
        this.tag = tag;
        this.code = code;
    }
}
