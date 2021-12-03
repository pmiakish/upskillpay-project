package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exception.PaymentParamException;
import com.epam.upskillproject.model.dao.queryhandler.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandler.constructors.IncomeQueryConstructor;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ejb.Singleton;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Singleton
public class IncomeDaoImpl implements IncomeDao {

    private static final Logger logger = LogManager.getLogger(IncomeDaoImpl.class.getName());

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
    private final IncomeQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;

    @Inject
    public IncomeDaoImpl(IncomeQueryConstructor queryConstructor, QueryExecutor queryExecutor) {
        this.queryConstructor = queryConstructor;
        this.queryExecutor = queryExecutor;
    }

    /**
     * Adds a specified amount to a system balance. The method is intended for use as a part of a transaction.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param amount a positive BigInteger value
     * @return true in case of success, otherwise false
     * @throws SQLException
     * @throws PaymentParamException if amount has negative value or equals zero
     */
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

    /**
     * Subtract a specified amount from a system balance. The method is intended for use as a part of a transaction.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param amount a positive BigInteger value
     * @return true in case of success, otherwise false
     * @throws SQLException
     * @throws PaymentParamException if amount has negative value or equals zero
     */
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
        Connection conn = dataSource.getConnection();
        String query = queryConstructor.getBalance();
        ResultSet rs = queryExecutor.execute(conn, query);
        BigDecimal balance = BigDecimal.ZERO;
        if (rs != null && rs.next()) {
            balance = rs.getBigDecimal(1);
            rs.getStatement().close();
        }
        conn.close();
        return balance;
    }
}
