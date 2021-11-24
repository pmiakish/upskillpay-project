package com.epam.upskillproject.model.dao.queryhandlers.constructors;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
public class AccountQueryConstructor {

    private final String SINGLE_BY_ID_PROP = "query.account.getSingleById";
    private final String SINGLE_BY_ID_AND_OWNER_PROP = "query.account.getSingleByIdAndOwner";
    private final String ALL_PROP = "query.account.getAll";
    private final String PAGE_PROP = "query.account.getPage";
    private final String BY_OWNER_PROP = "query.account.getByOwner";
    private final String BY_OWNER_PAGE_PROP = "query.account.getByOwnerPage";
    private final String COUNT_ALL_PROP = "query.account.countAll";
    private final String COUNT_BY_OWNER_PROP = "query.account.countByOwner";
    private final String UPDATE_STATUS_PROP = "query.account.updateStatus";
    private final String ADD_PROP = "query.account.add";
    private final String STATUS_PROP = "query.account.getStatus";
    private final String BALANCE_PROP = "query.account.balance";
    private final String INCREASE_PROP = "query.account.increase";
    private final String DECREASE_PROP = "query.account.decrease";
    private final String DEL_SINGLE_BY_ID_PROP = "query.account.deleteByID";
    private final String DEL_BY_OWNER_PROP = "query.account.deleteByOwner";
    private final String DEL_SINGLE_BY_ID_AND_OWNER_PROP = "query.account.deleteByIdAndOwner";

    @Inject
    PropertiesKeeper propertiesKeeper;

    public String singleById() {
        return propertiesKeeper.getString(SINGLE_BY_ID_PROP);
    }

    public String singleByIdAndOwner() {
        return propertiesKeeper.getString(SINGLE_BY_ID_AND_OWNER_PROP);
    }

    public String all() {
        return propertiesKeeper.getString(ALL_PROP);
    }

    public String page() {
        return propertiesKeeper.getString(PAGE_PROP);
    }

    public String byOwner() {
        return propertiesKeeper.getString(BY_OWNER_PROP);
    }

    public String byOwnerPage() {
        return propertiesKeeper.getString(BY_OWNER_PAGE_PROP);
    }

    public String countAll() {
        return propertiesKeeper.getString(COUNT_ALL_PROP);
    }

    public String countByOwner() {
        return propertiesKeeper.getString(COUNT_BY_OWNER_PROP);
    }

    public String updateStatus() {
        return propertiesKeeper.getString(UPDATE_STATUS_PROP);
    }

    public String status() {
        return propertiesKeeper.getString(STATUS_PROP);
    }

    public String balance() {
        return propertiesKeeper.getString(BALANCE_PROP);
    }

    public String increase() {
        return propertiesKeeper.getString(INCREASE_PROP);
    }

    public String decrease() {
        return propertiesKeeper.getString(DECREASE_PROP);
    }

    public String add() {
        return propertiesKeeper.getString(ADD_PROP);
    }

    public String delSingleById() {
        return propertiesKeeper.getString(DEL_SINGLE_BY_ID_PROP);
    }

    public String delByOwner() {
        return propertiesKeeper.getString(DEL_BY_OWNER_PROP);
    }

    public String delSingleByIdAndOwner() {
        return propertiesKeeper.getString(DEL_SINGLE_BY_ID_AND_OWNER_PROP);
    }
}
