package com.epam.upskillproject.model.dto;

public enum PermissionType {
    SUPERADMIN(0),
    ADMIN(1),
    CUSTOMER(2);

    private int id;

    PermissionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return name();
    }
}
