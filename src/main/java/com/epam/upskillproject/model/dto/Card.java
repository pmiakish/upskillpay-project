package com.epam.upskillproject.model.dto;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

public class Card {

    private final BigInteger id;
    private final BigInteger ownerId;
    private final BigInteger accountId;
    private final CardNetworkType network;
    private final String cvc;
    private final StatusType status;
    private final LocalDate expDate;
    private final int hash;

    public Card(BigInteger id, BigInteger ownerId, BigInteger accountId, CardNetworkType network, String cvc,
                StatusType status, LocalDate expDate) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountId = accountId;
        this.network = network;
        this.cvc = cvc;
        this.status = status;
        this.expDate = expDate;
        this.hash = hashCode();
    }

    public Card(BigInteger ownerId, BigInteger accountId, CardNetworkType network, StatusType status) {
        this.id = null;
        this.ownerId = ownerId;
        this.accountId = accountId;
        this.network = network;
        this.cvc = null;
        this.status = status;
        this.expDate = null;
        this.hash = hashCode();
    }

    public BigInteger getId() {
        return id;
    }

    public BigInteger getOwnerId() {
        return ownerId;
    }

    public BigInteger getAccountId() {
        return accountId;
    }

    public CardNetworkType getNetwork() {
        return network;
    }

    public String getCvc() {
        return cvc;
    }

    public StatusType getStatus() {
        return status;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id) &&
                Objects.equals(ownerId, card.ownerId) &&
                Objects.equals(accountId, card.accountId) &&
                network == card.network &&
                Objects.equals(cvc, card.cvc) &&
                status == card.status &&
                Objects.equals(expDate, card.expDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, accountId, network, cvc, status, expDate);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", accountId=" + accountId +
                ", network=" + network +
                ", status=" + status +
                ", expDate=" + expDate +
                '}';
    }
}
