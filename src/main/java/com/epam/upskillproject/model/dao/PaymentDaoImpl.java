package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.PaymentQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Transaction;
import com.epam.upskillproject.model.service.sort.TransactionSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class PaymentDaoImpl implements PaymentDao {

    private static final Logger logger = LogManager.getLogger(PaymentDaoImpl.class.getName());

    private static final String ID_COLUMN_NAME = "ID";
    private static final String AMOUNT_COLUMN_NAME = "AMOUNT";
    private static final String PAYER_COLUMN_NAME = "PAYER";
    private static final String RECEIVER_COLUMN_NAME = "RECEIVER";
    private static final String DATE_COLUMN_NAME = "DATE";

    private final PaymentQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;
    private final OrderStrategy orderStrategy;

    @Inject
    public PaymentDaoImpl(PaymentQueryConstructor queryConstructor, QueryExecutor queryExecutor,
                          @Named("paymentOrder") OrderStrategy orderStrategy) {
        this.queryExecutor = queryExecutor;
        this.queryConstructor = queryConstructor;
        this.orderStrategy = orderStrategy;
    }

    @Override
    public Optional<Transaction> getSinglePaymentById(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        Transaction payment = null;
        if (rs != null && rs.next()) {
            payment = buildInstance(rs);
            rs.close();
        }
        return (payment != null) ? Optional.of(payment) : Optional.empty();
    }

    @Override
    public List<Transaction> getAllPayments(TransactionSortType sortType) throws SQLException {
        String rawQuery = queryConstructor.all();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)));
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public List<Transaction> getPaymentsPage(int limit, int offset, TransactionSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)), limit, offset);
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public List<Transaction> getPaymentsByPayer(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.byPayer();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public List<Transaction> getPaymentsByPayerPage(BigInteger id, int limit, int offset) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.byPayerPage();
        ResultSet rs = queryExecutor.execute(rawQuery, id, limit, offset);
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public List<Transaction> getPaymentsByReceiver(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.byReceiver();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public List<Transaction> getPaymentsByReceiverPage(BigInteger id, int limit, int offset) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.byReceiverPage();
        ResultSet rs = queryExecutor.execute(rawQuery, id, limit, offset);
        List<Transaction> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.close();
        }
        return payments;
    }

    @Override
    public int countPayments() throws SQLException {
        String query = queryConstructor.countAll();
        ResultSet rs = queryExecutor.execute(query);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public int countPaymentsByPayer(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.countByPayer();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public int countPaymentsByReceiver(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.countByReceiver();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public BigDecimal getTotalReceiverIncomeByPayer(BigInteger payerId, BigInteger receiverId, int days)
            throws SQLException {
        if (days < 0) {
            return BigDecimal.ZERO;
        }
        String rawQuery = queryConstructor.totalReceiverIncomeByPayer();
        Timestamp past = Timestamp.valueOf(LocalDateTime.now().minusDays(days));
        BigDecimal amount = BigDecimal.ZERO;
        ResultSet rs = queryExecutor.execute(rawQuery, payerId, receiverId, past);
        if (rs != null && rs.next() && rs.getBigDecimal(1) != null) {
            amount = rs.getBigDecimal(1);
            rs.close();
        }
        return amount;
    }

    private Transaction buildInstance(ResultSet rs) throws SQLException {
        try {
            return new Transaction(
                    new BigInteger(rs.getString(ID_COLUMN_NAME)),
                    rs.getBigDecimal(AMOUNT_COLUMN_NAME),
                    new BigInteger(rs.getString(PAYER_COLUMN_NAME)),
                    new BigInteger(rs.getString(RECEIVER_COLUMN_NAME)),
                    rs.getTimestamp(DATE_COLUMN_NAME).toLocalDateTime()
            );
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create payment instance (invalid field values were obtained from database)", e);
        }
    }
}
