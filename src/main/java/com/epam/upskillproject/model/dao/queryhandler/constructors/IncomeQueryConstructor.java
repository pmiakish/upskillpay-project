package com.epam.upskillproject.model.dao.queryhandler.constructors;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
public class IncomeQueryConstructor {

    private static final String INCOME_GET_PROP = "query.income.get";
    private static final String INCOME_INCREASE_PROP = "query.income.increase";
    private static final String INCOME_DECREASE_PROP = "query.income.decrease";

    private final PropertiesKeeper propertiesKeeper;

    @Inject
    public IncomeQueryConstructor(PropertiesKeeper propertiesKeeper) {
        this.propertiesKeeper = propertiesKeeper;
    }

    public String getBalance() {
        return propertiesKeeper.getString(INCOME_GET_PROP);
    }

    public String increase() {
        return propertiesKeeper.getString(INCOME_INCREASE_PROP);
    }

    public String decrease() {
        return propertiesKeeper.getString(INCOME_DECREASE_PROP);
    }

}
