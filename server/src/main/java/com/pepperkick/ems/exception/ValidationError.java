package com.pepperkick.ems.exception;

public class ValidationError extends Exception {
    public String tag;
    public String code;

    public ValidationError(String tag, String code) {
        super("Validation error while checking " + tag + " due to error " + code);
        this.tag = tag;
        this.code = code;
    }
}
