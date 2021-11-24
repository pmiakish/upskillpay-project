package com.epam.upskillproject.exceptions;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class AccountLimitException extends IllegalStateException {

    public AccountLimitException() {
    }

    public AccountLimitException(String message) {
        super(message);
    }

    public AccountLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountLimitException(Throwable cause) {
        super(cause);
    }


}
