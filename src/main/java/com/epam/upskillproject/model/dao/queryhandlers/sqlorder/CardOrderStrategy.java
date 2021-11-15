package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import com.epam.upskillproject.model.service.sort.CardSortType;
import com.epam.upskillproject.model.service.sort.SortType;
import jakarta.ejb.Stateless;

@Stateless
public class CardOrderStrategy implements OrderStrategy {

    private static final String DEFAULT_ORDER = CardSortType.OWNER.getSqlName()  + ", " + CardSortType.ID.getSqlName();

    @Override
    public String getOrder(SortType sortType) {
        if (sortType instanceof CardSortType) {
            CardSortType cardSortType = (CardSortType) sortType;
            String order;
            switch (cardSortType) {
                case ID:
                case ID_DESC:
                    order = cardSortType.getSqlName();
                    break;
                case OWNER:
                case ACCOUNT:
                    order = cardSortType.getSqlName() + ", " + CardSortType.ID.getSqlName();
                    break;
                case OWNER_DESC:
                case ACCOUNT_DESC:
                    order = cardSortType.getSqlName() + ", " + CardSortType.ID_DESC.getSqlName();
                    break;
                case NETWORK:
                case STATUS:
                case EXPDATE:
                    order = cardSortType.getSqlName() + ", " + CardSortType.OWNER.getSqlName()  + ", " +
                            CardSortType.ID.getSqlName();
                    break;
                case NETWORK_DESC:
                case STATUS_DESC:
                case EXPDATE_DESC:
                    order = cardSortType.getSqlName() + ", " + CardSortType.OWNER_DESC.getSqlName()  + ", " +
                            CardSortType.ID_DESC.getSqlName();
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
