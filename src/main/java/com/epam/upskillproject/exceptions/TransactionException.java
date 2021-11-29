package com.epam.upskillproject.exceptions;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class TransactionException extends RuntimeException {

    private final TransactionExceptionType type;
    private final int statusCode;

    public TransactionException(TransactionExceptionType type, int statusCode) {
        this.type = type;
        this.statusCode = statusCode;
    }

    public TransactionException(TransactionExceptionType type, int statusCode, String message) {
        super(message);
        this.type = type;
        this.statusCode = statusCode;
    }

    public TransactionException(TransactionExceptionType type, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.statusCode = statusCode;
    }

    public TransactionException(TransactionExceptionType type, int statusCode, Throwable cause) {
        super(cause);
        this.type = type;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        String superMsg = super.getMessage();
        return (superMsg != null) ? type.getMessage().concat(" | ").concat(superMsg) : type.getMessage();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public TransactionExceptionType getType() {
        return type;
    }
}
