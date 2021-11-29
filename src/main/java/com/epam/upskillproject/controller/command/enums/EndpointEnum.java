package com.epam.upskillproject.controller.command.enums;

public enum EndpointEnum {
    INDEX("/"),
    LOGOUT("/logout"),
    LOGIN_ERROR("/error"),
    SIGN_UP("/signup"),
    USER_PROFILE("/profile"),
    CHANGE_LANG("/lang"),
    ACCOUNT_LIST("/accounts"),
    CARD_LIST("/cards"),
    ADMIN_LIST("/admins"),
    ADMIN_PROFILE("/admin/[0-9]+"),
    CUSTOMER_LIST("/customers"),
    CUSTOMER_PROFILE("/customer/[0-9]+"),
    CUSTOMER_ACCOUNTS("/customer/accounts/[0-9]+"),
    PAYMENT_LIST("/payments"),
    SYSTEM_INCOME("/income"),
    PAYSERVICE_ACCOUNTS("/payservice/my_accounts"),
    PAYSERVICE_SERVICE("/payservice/my_account_service/[0-9]+"),
    PAYSERVICE_INCOMING("/payservice/my_account_incoming/[0-9]+"),
    PAYSERVICE_OUTGOING("/payservice/my_account_outgoing/[0-9]+");

    private String pattern;

    EndpointEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
