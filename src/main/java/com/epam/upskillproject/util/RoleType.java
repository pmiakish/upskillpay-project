package com.epam.upskillproject.util;

public enum RoleType {
    SUPERADMIN(0),
    ADMIN(1),
    CUSTOMER(2);

    private int id;

    RoleType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return name();
    }
}
