package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.SortType;
import jakarta.ejb.Stateless;

@Stateless
public class AccountOrderStrategy implements OrderStrategy {

    private static final String DEFAULT_ORDER = AccountSortType.OWNER.getSqlName()  + ", " + AccountSortType.ID.getSqlName();

    @Override
    public String getOrder(SortType sortType) {
        if (sortType instanceof AccountSortType) {
            AccountSortType accountSortType = (AccountSortType) sortType;
            String order;
            switch (accountSortType) {
                case ID:
                case ID_DESC:
                    order = accountSortType.getSqlName();
                    break;
                case OWNER:
                    order = accountSortType.getSqlName() + ", " + AccountSortType.ID.getSqlName();
                    break;
                case OWNER_DESC:
                    order = accountSortType.getSqlName() + ", " + AccountSortType.ID_DESC.getSqlName();
                    break;
                case BALANCE:
                case STATUS:
                case REGDATE:
                    order = accountSortType.getSqlName() + ", " + AccountSortType.OWNER.getSqlName() + ", " +
                            AccountSortType.ID.getSqlName();
                    break;
                case BALANCE_DESC:
                case STATUS_DESC:
                case REGDATE_DESC:
                    order = accountSortType.getSqlName() + ", " + AccountSortType.OWNER_DESC.getSqlName() + ", " +
                            AccountSortType.ID_DESC.getSqlName();
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
