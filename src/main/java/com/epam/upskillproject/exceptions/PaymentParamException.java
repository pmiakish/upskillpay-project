package com.epam.upskillproject.exceptions;

public class PaymentParamException extends IllegalArgumentException {

    public PaymentParamException() {
    }

    public PaymentParamException(String message) {
        super(message);
    }

    public PaymentParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentParamException(Throwable cause) {
        super(cause);
    }


}
