package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.SortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.TransactionSortType;
import jakarta.ejb.Stateless;

@Stateless
public class PaymentOrderStrategy implements OrderStrategy {

    private static final String DEFAULT_ORDER = TransactionSortType.ID_DESC.getSqlName();

    @Override
    public String getOrder(SortType sortType) {
        if (sortType instanceof TransactionSortType) {
            TransactionSortType transactionSortType = (TransactionSortType) sortType;
            String order;
            switch (transactionSortType) {
                case ID:
                case ID_DESC:
                    order = transactionSortType.getSqlName();
                    break;
                case AMOUNT:
                case PAYER:
                case RECEIVER:
                case DATE:
                    order = transactionSortType.getSqlName() + ", " + TransactionSortType.ID.getSqlName();
                    break;
                case AMOUNT_DESC:
                case PAYER_DESC:
                case RECEIVER_DESC:
                case DATE_DESC:
                    order = transactionSortType.getSqlName() + ", " + TransactionSortType.ID_DESC.getSqlName();
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
