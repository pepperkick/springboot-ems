package com.pepperkick.ems.object;

public enum Designation {
    UNKNOWN,
    DIRECTOR,
    MANAGER,
    LEAD,
    EMPLOYEE,
    INTERN;

    public static final Designation DEVELOPER = EMPLOYEE;
    public static final Designation DEV_OPS = EMPLOYEE;
    public static final Designation QA = EMPLOYEE;
}
