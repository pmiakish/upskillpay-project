package com.epam.upskillproject.model.dao.queryhandlers.sqlorder;

import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.SortType;

public interface OrderStrategy {
    /**
     * Produces a part of SQL-query which contains an ORDER type
     * @param sortType enum representing column name or column name with the suffix '_DESC'. If passed null, will be
     *                 returned String with default order
     * @return String with one or more order conditions
     */
    String getOrder(SortType sortType);
}
