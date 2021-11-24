package com.epam.upskillproject.model.dao.queryhandlers.constructors;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
public class CardQueryConstructor {

    private final String SINGLE_BY_ID_PROP = "query.card.getSingleById";
    private final String SINGLE_BY_ID_AND_OWNER_PROP = "query.card.getSingleByIdAndOwner";
    private final String ALL_PROP = "query.card.getAll";
    private final String PAGE_PROP = "query.card.getPage";
    private final String BY_OWNER_PROP = "query.card.getByOwner";
    private final String BY_ACCOUNT_PROP = "query.card.getByAccount";
    private final String COUNT_ALL_PROP = "query.card.countAll";
    private final String COUNT_BY_OWNER_PROP = "query.card.countByOwner";
    private final String COUNT_BY_ACCOUNT_PROP = "query.card.countByAccount";
    private final String UPDATE_STATUS_PROP = "query.card.updateStatus";
    private final String ACCOUNT_ID_PROP = "query.card.getAccountId";
    private final String ADD_PROP = "query.card.add";
    private final String STATUS_PROP = "query.card.getStatus";
    private final String DEL_SINGLE_BY_ID_PROP = "query.card.deleteSingleById";
    private final String DEL_BY_OWNER_PROP = "query.card.deleteByOwner";
    private final String DEL_SINGLE_BY_ID_AND_OWNER_PROP = "query.card.deleteSingleByIdAndOwner";
    private final String DEL_BY_ACCOUNT_PROP = "query.card.deleteByAccount";

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

    public String byAccount() {
        return propertiesKeeper.getString(BY_ACCOUNT_PROP);
    }

    public String countAll() {
        return propertiesKeeper.getString(COUNT_ALL_PROP);
    }

    public String countByOwner() {
        return propertiesKeeper.getString(COUNT_BY_OWNER_PROP);
    }

    public String countByAccount() {
        return propertiesKeeper.getString(COUNT_BY_ACCOUNT_PROP);
    }

    public String updateStatus() {
        return propertiesKeeper.getString(UPDATE_STATUS_PROP);
    }

    public String accountId() {
        return propertiesKeeper.getString(ACCOUNT_ID_PROP);
    }

    public String add() {
        return propertiesKeeper.getString(ADD_PROP);
    }

    public String status() {
        return propertiesKeeper.getString(STATUS_PROP);
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

    public String delByAccount() {
        return propertiesKeeper.getString(DEL_BY_ACCOUNT_PROP);
    }

}
