package com.epam.upskillproject.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class CommandNotFoundException extends IllegalArgumentException {

    public CommandNotFoundException() {
    }

    public CommandNotFoundException(String message) {
        super(message);
    }

    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandNotFoundException(Throwable cause) {
        super(cause);
    }


}
