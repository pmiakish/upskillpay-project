package com.epam.upskillproject.model.dto;

import java.math.BigDecimal;

public enum CardNetworkType {

    VISA_CLASSIC(1, "VISA Classic","10.00"),
    VISA_GOLD(2, "VISA Gold", "13.50"),
    VISA_PLATINUM(3, "VISA Platinum", "19.50"),
    MC_STANDARD(4, "MASTERCARD Standard", "10.00"),
    MC_GOLD(5, "MASTERCARD Gold", "15.00"),
    MC_WORLD(6, "MASTERCARD World", "20.00");

    private int id;
    private String name;
    private String cost;

    CardNetworkType(int id, String name, String cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCost() {
        return new BigDecimal(cost);
    }
}
