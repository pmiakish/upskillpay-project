package com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort;

public enum PersonSortType implements SortType {

    ID("ID"),
    ID_DESC("ID DESC"),
    PERM("PERM"),
    PERM_DESC("PERM DESC"),
    EMAIL("EMAIL"),
    EMAIL_DESC("EMAIL DESC"),
    FIRSTNAME("FIRSTNAME"),
    FIRSTNAME_DESC("FIRSTNAME DESC"),
    LASTNAME("LASTNAME"),
    LASTNAME_DESC("LASTNAME DESC"),
    STATUS("STATUS"),
    STATUS_DESC("STATUS DESC"),
    REGDATE("REGDATE"),
    REGDATE_DESC("REGDATE DESC");

    private final String sqlName;

    PersonSortType(String sqlName) {
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
