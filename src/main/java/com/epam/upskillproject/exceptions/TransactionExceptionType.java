package com.epam.upskillproject.exceptions;

public enum TransactionExceptionType {

    CONNECTION("Cannot connect to database"),
    BAD_PARAM("Transaction cannot be implemented (incorrect parameters passed)"),
    ROLLBACK("Cannot rollback transaction"),
    PERFORM("Transaction was rolled back because of error during execution");

    private final String message;

    TransactionExceptionType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
