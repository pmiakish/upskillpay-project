package com.epam.upskillproject.exception;

public enum TransactionExceptionType {

    CONNECTION("Cannot connect to database. "),
    BAD_PARAM("Transaction cannot be implemented (incorrect parameters passed). "),
    ROLLBACK("Cannot rollback transaction. "),
    PERFORM("Transaction was rolled back because of error during execution. "),
    LOW_BALANCE("Transaction was rolled back because of lack of funds. "),
    FORBIDDEN_STATUS("Receiver's status does not allow to perform transaction. ");

    private final String message;

    TransactionExceptionType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
