package com.epam.upskillproject.model.dao.queryhandler.sqlorder;

import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.SortType;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PaymentSortType;
import jakarta.ejb.Stateless;

@Stateless
public class PaymentOrderStrategy implements OrderStrategy {

    private static final String DEFAULT_ORDER = PaymentSortType.ID_DESC.getSqlName();

    @Override
    public String getOrder(SortType sortType) {
        if (sortType instanceof PaymentSortType) {
            PaymentSortType paymentSortType = (PaymentSortType) sortType;
            String order;
            switch (paymentSortType) {
                case ID:
                case ID_DESC:
                    order = paymentSortType.getSqlName();
                    break;
                case AMOUNT:
                case PAYER:
                case RECEIVER:
                case DATE:
                    order = paymentSortType.getSqlName() + ", " + PaymentSortType.ID.getSqlName();
                    break;
                case AMOUNT_DESC:
                case PAYER_DESC:
                case RECEIVER_DESC:
                case DATE_DESC:
                    order = paymentSortType.getSqlName() + ", " + PaymentSortType.ID_DESC.getSqlName();
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
