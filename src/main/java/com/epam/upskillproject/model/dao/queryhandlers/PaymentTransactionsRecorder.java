package com.epam.upskillproject.model.dao.queryhandlers;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.*;

@Singleton
public class PaymentTransactionsRecorder {
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final String COMMIT_TRANSACTION_PROP = "query.transact.commit";

    private final PropertiesKeeper propertiesKeeper;
    private final QueryExecutor queryExecutor;

    @Inject
    public PaymentTransactionsRecorder(PropertiesKeeper propertiesKeeper, QueryExecutor queryExecutor) {
        this.propertiesKeeper = propertiesKeeper;
        this.queryExecutor = queryExecutor;
    }

    public void commit(Connection conn, BigDecimal amount, BigInteger payerId, BigInteger receiverId)
            throws SQLException {
        if (amount != null) {
            amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
            String query = propertiesKeeper.getString(COMMIT_TRANSACTION_PROP);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            queryExecutor.executeUpdate(conn, query, amount, payerId, receiverId, now);
        } else {
            throw new SQLException("Cannot commit payment info: bad parameter (amount is null)");
        }
    }
}
