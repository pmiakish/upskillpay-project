package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.ejb.Singleton;

@Singleton
public class OrderStrategyFactory {

    @Produces @Named("accountOrder")
    public OrderStrategy getAccountOrderStrategy() {
        return new AccountOrderStrategy();
    }

    @Produces @Named("cardOrder")
    public OrderStrategy getCardOrderStrategy() {
        return new CardOrderStrategy();
    }

    @Produces @Named("paymentOrder")
    public OrderStrategy getPaymentOrderStrategy() {
        return new PaymentOrderStrategy();
    }

    @Produces @Named("personOrder")
    public OrderStrategy getPersonOrderStrategy() {
        return new PersonOrderStrategy();
    }
}
