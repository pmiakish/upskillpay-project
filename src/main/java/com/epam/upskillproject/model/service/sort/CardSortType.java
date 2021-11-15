package com.epam.upskillproject.model.service.sort;

public enum CardSortType implements SortType {

    ID("ID"),
    ID_DESC("ID DESC"),
    OWNER("OWNER"),
    OWNER_DESC("OWNER DESC"),
    ACCOUNT("ACCOUNT"),
    ACCOUNT_DESC("ACCOUNT DESC"),
    NETWORK("NETWORK"),
    NETWORK_DESC("NETWORK DESC"),
    STATUS("STATUS"),
    STATUS_DESC("STATUS DESC"),
    EXPDATE("EXPDATE"),
    EXPDATE_DESC("EXPDATE DESC");

    private final String sqlName;

    CardSortType(String sqlName) {
        this.sqlName = sqlName;
    }

    @Override
    public String getSqlName() {
        return sqlName;
    }

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }

}
