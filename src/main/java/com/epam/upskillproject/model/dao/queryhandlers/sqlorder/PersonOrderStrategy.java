package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import com.epam.upskillproject.model.service.sort.PersonSortType;
import com.epam.upskillproject.model.service.sort.SortType;
import jakarta.ejb.Stateless;

@Stateless
public class PersonOrderStrategy implements OrderStrategy {

    private static final String DEFAULT_ORDER = PersonSortType.LASTNAME.getSqlName()  + ", " +
            PersonSortType.FIRSTNAME.getSqlName();

    @Override
    public String getOrder(SortType sortType) {
        if (sortType instanceof PersonSortType) {
            PersonSortType personSortType = (PersonSortType) sortType;
            String order;
            switch (personSortType) {
                case ID:
                case ID_DESC:
                    order = personSortType.getSqlName();
                    break;
                case PERM:
                case EMAIL:
                case STATUS:
                case REGDATE:
                    order = personSortType.getSqlName() + ", " + PersonSortType.LASTNAME.getSqlName()  + ", " +
                            PersonSortType.FIRSTNAME.getSqlName();
                    break;
                case PERM_DESC:
                case EMAIL_DESC:
                case STATUS_DESC:
                case REGDATE_DESC:
                    order = personSortType.getSqlName() + ", " + PersonSortType.LASTNAME_DESC.getSqlName()  + ", " +
                            PersonSortType.FIRSTNAME_DESC.getSqlName();
                    break;
                case FIRSTNAME:
                    order = personSortType.getSqlName() + ", " + PersonSortType.LASTNAME.getSqlName();
                    break;
                case FIRSTNAME_DESC:
                    order = personSortType.getSqlName() + ", " + PersonSortType.LASTNAME_DESC.getSqlName();
                    break;
                case LASTNAME:
                    order = personSortType.getSqlName() + ", " + PersonSortType.FIRSTNAME.getSqlName();
                    break;
                case LASTNAME_DESC:
                    order = personSortType.getSqlName() + ", " + PersonSortType.FIRSTNAME_DESC.getSqlName();
                    break;
                default:
                    order = DEFAULT_ORDER;
                    break;
            }
            return order;
        } else {
            return DEFAULT_ORDER;
        }
    }
}
