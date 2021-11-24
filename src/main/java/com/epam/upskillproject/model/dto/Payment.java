package com.epam.upskillproject.model.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigInteger id;
    private final BigDecimal amount;
    private final BigInteger payerId;
    private final BigInteger receiverId;
    private final LocalDateTime dateTime;

    public Payment(BigInteger id, BigDecimal amount, BigInteger payerId, BigInteger receiverId, LocalDateTime dateTime) {
        this.id = id;
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
        this.payerId = payerId;
        this.receiverId = receiverId;
        this.dateTime = dateTime;
    }

    public BigInteger getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigInteger getPayerId() {
        return payerId;
    }

    public BigInteger getReceiverId() {
        return receiverId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                Objects.equals(amount, payment.amount) &&
                Objects.equals(payerId, payment.payerId) &&
                Objects.equals(receiverId, payment.receiverId) &&
                Objects.equals(dateTime, payment.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, payerId, receiverId, dateTime);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", payerId=" + payerId +
                ", receiverId=" + receiverId +
                ", dateTime=" + dateTime +
                '}';
    }
}
