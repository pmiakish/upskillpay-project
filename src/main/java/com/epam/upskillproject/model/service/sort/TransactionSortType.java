package com.epam.upskillproject.model.service.sort;

public enum TransactionSortType implements SortType {

    ID("ID"),
    ID_DESC("ID DESC"),
    AMOUNT("AMOUNT"),
    AMOUNT_DESC("AMOUNT DESC"),
    PAYER("PAYER"),
    PAYER_DESC("PAYER DESC"),
    RECEIVER("RECEIVER"),
    RECEIVER_DESC("RECEIVER DESC"),
    DATE("DATE"),
    DATE_DESC("DATE DESC");

    private final String sqlName;

    TransactionSortType(String sqlName) {
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
