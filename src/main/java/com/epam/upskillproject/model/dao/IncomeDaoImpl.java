package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.IncomeQueryConstructor;
import jakarta.inject.Inject;
import jakarta.ejb.Singleton;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.sql.*;

@Singleton
public class IncomeDaoImpl implements IncomeDao {

    private static final Logger logger = LogManager.getLogger(IncomeDaoImpl.class.getName());

    private final IncomeQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;

    @Inject
    public IncomeDaoImpl(IncomeQueryConstructor queryConstructor, QueryExecutor queryExecutor) {
        this.queryConstructor = queryConstructor;
        this.queryExecutor = queryExecutor;
    }

    // should use the method as a transaction part
    @Override
    public synchronized boolean increaseBalance(Connection conn, BigDecimal amount) throws SQLException,
            PaymentParamException {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.log(Level.WARN, String.format("Incorrect amount value passed to %s (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), amount));
            throw new PaymentParamException("Incorrect payment parameters (amount equals or less than zero)");
        }
        String rawQuery = queryConstructor.increase();
        int result = queryExecutor.executeUpdate(conn, rawQuery, amount);
        return (result != 0);
    }

    @Override
    public synchronized boolean decreaseBalance(Connection conn, BigDecimal amount) throws SQLException,
            PaymentParamException {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.log(Level.WARN, String.format("Incorrect amount value passed to %s (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), amount));
            throw new PaymentParamException("Incorrect payment parameters (amount equals or less than zero)");
        }
        String rawQuery = queryConstructor.decrease();
        int result = queryExecutor.executeUpdate(conn, rawQuery, amount);
        return (result != 0);
    }

    @Override
    public BigDecimal getBalance() throws SQLException {
        String query = queryConstructor.getBalance();
        ResultSet rs = queryExecutor.execute(query);
        BigDecimal balance = BigDecimal.ZERO;
        if (rs != null && rs.next()) {
            balance = rs.getBigDecimal(1);
            rs.close();
        }
        return balance;
    }
}
