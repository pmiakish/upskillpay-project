package com.epam.upskillproject.util;

import com.epam.upskillproject.model.dto.StatusType;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Stateless
public class ParamsValidator {
    private static final String EMAIL_PATTERN = "^\\w+@\\w+\\.\\w+$";
    private static final String NAME_PATTERN = "^[\\p{Lu}][-\\p{L} ]+$";
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int MAX_NAME_LENGTH = 30;

    public boolean validateEmail(String email) {
        return (email != null && email.matches(EMAIL_PATTERN));
    }

    public boolean validatePassword(String password) {
        return (password != null && password.length() > 0 && password.length() <= MAX_PASSWORD_LENGTH);
    }

    public boolean validateName(String... names) {
        if (names != null && names.length > 0) {
            for (String name : names) {
                if (name == null || name.length() == 0 || name.length() > MAX_NAME_LENGTH || !name.matches(NAME_PATTERN)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean validatePersonAddParams(String email, String password, String firstName, String lastName) {
        return validateEmail(email) &&
                (password == null || validatePassword(password)) &&
                validateName(firstName, lastName);

    }

    public boolean validatePersonUpdateParams(BigInteger id, String email, String password, String firstName,
                                              String lastName, StatusType statusType, LocalDate regDate) {
        return validateId(id) &&
                validateEmail(email) &&
                (password == null || validatePassword(password)) &&
                validateName(firstName, lastName) &&
                statusType != null &&
                regDate != null;
    }

    public boolean validatePositiveInt(int number) {
        return (number > 0);
    }

    public boolean validatePageParams(int amount, int pageNumber) {
        return (validatePositiveInt(amount) && validatePositiveInt(pageNumber));
    }

    public boolean validateId(BigInteger id) {
        return (id != null && id.compareTo(BigInteger.ZERO) > 0);
    }

    public boolean validatePaymentId(BigInteger id) {
        return (id != null && id.compareTo(BigInteger.ZERO) >= 0);
    }

    public boolean validatePaymentAmount(BigDecimal amount) {
        return (amount != null && amount.compareTo(BigDecimal.ZERO) > 0);
    }
}
