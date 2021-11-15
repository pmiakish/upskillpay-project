package com.epam.upskillproject.model.service.sort;

public enum AccountSortType implements SortType {

    ID("ID"),
    ID_DESC("ID DESC"),
    OWNER("OWNER"),
    OWNER_DESC("OWNER DESC"),
    BALANCE("BALANCE"),
    BALANCE_DESC("BALANCE DESC"),
    STATUS("STATUS"),
    STATUS_DESC("STATUS DESC"),
    REGDATE("REGDATE"),
    REGDATE_DESC("REGDATE DESC");

    private final String sqlName;

    AccountSortType(String sqlName) {
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
