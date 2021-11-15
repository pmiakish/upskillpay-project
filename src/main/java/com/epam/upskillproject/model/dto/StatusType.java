package com.epam.upskillproject.model.dto;

public enum StatusType {
    ACTIVE(1),
    BLOCKED(2);

    private int id;

    StatusType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return name();
    }
}
