package com.epam.upskillproject.model.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Account {
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigInteger id;
    private final BigInteger ownerId;
    private final BigDecimal balance;
    private final StatusType status;
    private final LocalDate regDate;
    private final int hash;

    public Account(BigInteger id, BigInteger ownerId, BigDecimal balance, StatusType status, LocalDate regDate) {
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        this.status = status;
        this.regDate = regDate;
        this.hash = hashCode();
    }

    public Account(BigInteger ownerId, BigDecimal balance, StatusType status) {
        this.id = null;
        this.ownerId = ownerId;
        this.balance = balance.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        this.status = status;
        this.regDate = null;
        this.hash = hashCode();
    }

    public BigInteger getId() {
        return id;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public StatusType getStatus() {
        return status;
    }

    public LocalDate getRegDate() {
        return regDate;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(ownerId, account.ownerId) &&
                Objects.equals(balance, account.balance) &&
                status == account.status &&
                Objects.equals(regDate, account.regDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, balance, status, regDate);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", balance=" + balance +
                ", status=" + status +
                ", regDate=" + regDate +
                '}';
    }

}
