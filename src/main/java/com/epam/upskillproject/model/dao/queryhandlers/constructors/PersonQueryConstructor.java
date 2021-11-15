package com.epam.upskillproject.model.dao.queryhandlers.constructors;

import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.PermissionType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
public class PersonQueryConstructor {

    private final String SINGLE_PROP_PATTERN = "query.%s.getSingleBy%s";
    private final String ALL_PROP_PATTERN = "query.%s.getAll";
    private final String PAGE_PROP_PATTERN = "query.%s.getPage";
    private final String COUNT_PROP_PATTERN = "query.%s.countAll";
    private final String UPDATE_PROP_PATTERN = "query.%s.update";
    private final String STATUS_PROP = "query.person.getStatus";
    private final String ADD_PROP = "query.person.add";
    private final String DEL_SINGLE_BY_ID_PROP = "query.person.delete";
    private final String PERSON_PERM_STR = "person";
    private final String ADMIN_PERM_STR = "admin";
    private final String CUSTOMER_PERM_STR = "customer";
    private final String ID_PARAM_NAME = "Id";
    private final String EMAIL_PARAM_NAME = "Email";

    @Inject
    PropertiesKeeper propertiesKeeper;

    public String singleByEmail(PermissionType permission) {
        String propertyName = String.format(SINGLE_PROP_PATTERN, permissionToString(permission), EMAIL_PARAM_NAME);
        return propertiesKeeper.getString(propertyName);
    }


    public String singleByEmail() {
        String propertyName = String.format(SINGLE_PROP_PATTERN, PERSON_PERM_STR, EMAIL_PARAM_NAME);
        return propertiesKeeper.getString(propertyName);
    }

    public String singleById(PermissionType permission) {
        String propertyName = String.format(SINGLE_PROP_PATTERN, permissionToString(permission), ID_PARAM_NAME);
        return propertiesKeeper.getString(propertyName);
    }

    public String singleById() {
        String propertyName = String.format(SINGLE_PROP_PATTERN, PERSON_PERM_STR, ID_PARAM_NAME);
        return propertiesKeeper.getString(propertyName);
    }

    public String all(PermissionType permission) {
        String propertyName = String.format(ALL_PROP_PATTERN, permissionToString(permission));
        return propertiesKeeper.getString(propertyName);
    }

    public String all() {
        String propertyName = String.format(ALL_PROP_PATTERN, PERSON_PERM_STR);
        return propertiesKeeper.getString(propertyName);
    }

    public String page(PermissionType permission) {
        String propertyName = String.format(PAGE_PROP_PATTERN, permissionToString(permission));
        return propertiesKeeper.getString(propertyName);
    }

    public String page() {
        String propertyName = String.format(PAGE_PROP_PATTERN, PERSON_PERM_STR);
        return propertiesKeeper.getString(propertyName);
    }

    public String count(PermissionType permission) {
        String propertyName = String.format(COUNT_PROP_PATTERN, permissionToString(permission));
        return propertiesKeeper.getString(propertyName);
    }

    public String count() {
        String propertyName = String.format(COUNT_PROP_PATTERN, PERSON_PERM_STR);
        return propertiesKeeper.getString(propertyName);
    }

    public String update(PermissionType permission) {
        String propertyName = String.format(UPDATE_PROP_PATTERN, permissionToString(permission));
        return propertiesKeeper.getString(propertyName);
    }

    public String update() {
        String propertyName = String.format(UPDATE_PROP_PATTERN, PERSON_PERM_STR);
        return propertiesKeeper.getString(propertyName);
    }

    public String status() {
        return propertiesKeeper.getString(STATUS_PROP);
    }

    public String add() {
        return propertiesKeeper.getString(ADD_PROP);
    }

    public String delSingleById() {
        return propertiesKeeper.getString(DEL_SINGLE_BY_ID_PROP);
    }

    private String permissionToString(PermissionType permission) {
        String permissionStr;
        switch (permission) {
            case SUPERADMIN:
            case ADMIN:
                permissionStr = ADMIN_PERM_STR;
                break;
            case CUSTOMER:
                permissionStr = CUSTOMER_PERM_STR;
                break;
            default:
                permissionStr = PERSON_PERM_STR;
                break;
        }
        return permissionStr;
    }

}
