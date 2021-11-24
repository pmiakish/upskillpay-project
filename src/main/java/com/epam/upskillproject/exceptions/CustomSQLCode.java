package com.epam.upskillproject.exceptions;

public enum CustomSQLCode {
    POOL_EXHAUSTED(2311211),
    INVALID_STATEMENT_PARAMETER(2311212),
    INVALID_DB_PARAMETER(2311213);

    private int code;

    CustomSQLCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
