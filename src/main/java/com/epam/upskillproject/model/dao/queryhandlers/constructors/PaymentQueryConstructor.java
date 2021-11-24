package com.epam.upskillproject.model.dao.queryhandlers.constructors;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
public class PaymentQueryConstructor {

    private final String SINGLE_BY_ID_PROP = "query.payment.getSingleById";
    private final String ALL_PROP = "query.payment.getAll";
    private final String BY_PAYER_PROP = "query.payment.getByPayer";
    private final String BY_RECEIVER_PROP = "query.payment.getByReceiver";
    private final String PAGE_PROP = "query.payment.getPage";
    private final String BY_PAYER_PAGE_PROP = "query.payment.getByPayerPage";
    private final String BY_RECEIVER_PAGE_PROP = "query.payment.getByReceiverPage";
    private final String COUNT_ALL_PROP = "query.payment.countAll";
    private final String COUNT_BY_PAYER_PROP = "query.payment.countByPayer";
    private final String COUNT_BY_RECEIVER_PROP = "query.payment.countByReceiver";
    private final String RECEIVER_INCOME_BY_PAYER_PROP = "query.payment.getTotalReceiverIncomeByPayer";

    @Inject
    PropertiesKeeper propertiesKeeper;

    public String singleById() {
        return propertiesKeeper.getString(SINGLE_BY_ID_PROP);
    }

    public String all() {
        return propertiesKeeper.getString(ALL_PROP);
    }

    public String byPayer() {
        return propertiesKeeper.getString(BY_PAYER_PROP);
    }

    public String byReceiver() {
        return propertiesKeeper.getString(BY_RECEIVER_PROP);
    }

    public String page() {
        return propertiesKeeper.getString(PAGE_PROP);
    }

    public String byPayerPage() {
        return propertiesKeeper.getString(BY_PAYER_PAGE_PROP);
    }

    public String byReceiverPage() {
        return propertiesKeeper.getString(BY_RECEIVER_PAGE_PROP);
    }

    public String countAll() {
        return propertiesKeeper.getString(COUNT_ALL_PROP);
    }

    public String countByPayer() {
        return propertiesKeeper.getString(COUNT_BY_PAYER_PROP);
    }

    public String countByReceiver() {
        return propertiesKeeper.getString(COUNT_BY_RECEIVER_PROP);
    }

    public String totalReceiverIncomeByPayer() {
        return propertiesKeeper.getString(RECEIVER_INCOME_BY_PAYER_PROP);
    }

}
