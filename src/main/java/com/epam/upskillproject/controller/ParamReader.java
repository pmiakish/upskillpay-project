package com.epam.upskillproject.controller;

import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.CardNetworkType;
import com.epam.upskillproject.model.dto.PermissionType;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.TransactionSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class ParamReader {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String PAGE_NUMBER_PROP = "pagination.default.pageNumber";
    private static final String PAGE_SIZE_PROP = "pagination.default.pageSize";
    private static final String PAGE_PARAM = "page";
    private static final String ENTRIES_PARAM = "entries";

    private final PropertiesKeeper propertiesKeeper;

    @Inject
    public ParamReader(PropertiesKeeper propertiesKeeper) {
        this.propertiesKeeper = propertiesKeeper;
    }

    public Optional<Integer> readInteger(HttpServletRequest req, String paramName) {
        Integer paramValue = null;
        if (isCorrectString(paramName)) {
            try {
                paramValue = Integer.parseInt(Objects.requireNonNull(req.getParameter(paramName)));
            } catch (NullPointerException | NumberFormatException e) {
                return Optional.empty();
            }
        }
        return (paramValue != null) ? Optional.of(paramValue) : Optional.empty();
    }

    public Optional<Boolean> readBoolean(HttpServletRequest req, String paramName) {
        if (isCorrectString(paramName)) {
            try {
                return Optional.of(Boolean.valueOf(Objects.requireNonNull(req.getParameter(paramName))));
            } catch (NullPointerException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> readString(HttpServletRequest req, String paramName) {
        String paramValue = null;
        if (isCorrectString(paramName)) {
            paramValue = req.getParameter(paramName);
        }
        return (paramValue == null || paramValue.length() == 0) ? Optional.empty() : Optional.of(paramValue);
    }

    public Optional<BigInteger> readBigInteger(HttpServletRequest req, String paramName) {
        Optional<String> paramStrValue = readString(req, paramName);
        BigInteger paramValue = null;
        if (paramStrValue.isPresent()) {
            try {
                paramValue = new BigInteger(paramStrValue.get());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return (paramValue != null) ? Optional.of(paramValue) : Optional.empty();
    }

    public Optional<BigDecimal> readBigDecimal(HttpServletRequest req, String paramName) {
        Optional<String> paramStrValue = readString(req, paramName);
        BigDecimal paramValue = null;
        if (paramStrValue.isPresent()) {
            try {
                paramValue = new BigDecimal(paramStrValue.get()).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return (paramValue != null) ? Optional.of(paramValue) : Optional.empty();
    }

    public Optional<LocalDate> readLocalDate(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        LocalDate localDate;
        if (paramValue.isPresent()) {
            try {
                localDate = LocalDate.parse(paramValue.get());
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(localDate);
    }

    public Optional<PersonSortType> readPersonSort(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        PersonSortType sortType;
        if (paramValue.isPresent()) {
            try {
                sortType = PersonSortType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(sortType);
    }

    public Optional<AccountSortType> readAccountSort(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        AccountSortType sortType;
        if (paramValue.isPresent()) {
            try {
                sortType = AccountSortType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(sortType);
    }

    public Optional<TransactionSortType> readPaymentSort(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        TransactionSortType sortType;
        if (paramValue.isPresent()) {
            try {
                sortType = TransactionSortType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(sortType);
    }

    public Optional<CardSortType> readCardSort(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        CardSortType sortType;
        if (paramValue.isPresent()) {
            try {
                sortType = CardSortType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(sortType);
    }

    public Optional<StatusType> readStatusType(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        StatusType statusType;
        if (paramValue.isPresent()) {
            try {
                statusType = StatusType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(statusType);
    }

    public Optional<PermissionType> readPermissionType(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        PermissionType permissionType;
        if (paramValue.isPresent()) {
            try {
                permissionType = PermissionType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(permissionType);
    }

    public Optional<CardNetworkType> readCardNetworkType(HttpServletRequest req, String paramName) {
        Optional<String> paramValue = readString(req, paramName);
        CardNetworkType cardNetworkType;
        if (paramValue.isPresent()) {
            try {
                cardNetworkType = CardNetworkType.valueOf(paramValue.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(cardNetworkType);
    }

    public int readPageNumber(HttpServletRequest req) {
        Optional<Integer> pageNumberParam = readInteger(req, PAGE_PARAM);
        return (pageNumberParam.isPresent() && pageNumberParam.get() > 0) ? pageNumberParam.get() :
                propertiesKeeper.getIntOrDefault(PAGE_NUMBER_PROP, DEFAULT_PAGE_NUMBER);
    }

    public int readPageSize(HttpServletRequest req) {
        Optional<Integer> pageSizeParam = readInteger(req, ENTRIES_PARAM);
        return (pageSizeParam.isPresent() && pageSizeParam.get() > 0) ? pageSizeParam.get() :
                propertiesKeeper.getIntOrDefault(PAGE_SIZE_PROP, DEFAULT_PAGE_SIZE);
    }

    private boolean isCorrectString(String str) {
        return (str != null && str.length() > 0);
    }
}
