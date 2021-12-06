package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exception.CustomSQLCode;
import com.epam.upskillproject.model.dao.queryhandler.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandler.constructors.PaymentQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Payment;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PaymentSortType;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
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
    private static final String INVALID_PARAM_SQLSTATE = "22023";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
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
    public Optional<Payment> getSinglePaymentById(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        Payment payment = null;
        if (rs != null && rs.next()) {
            payment = buildInstance(rs);
            rs.getStatement().close();
        }
        conn.close();
        return (payment != null) ? Optional.of(payment) : Optional.empty();
    }

    @Override
    public List<Payment> getAllPayments(PaymentSortType sortType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.all();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn,query);
        return retrievePayments(conn, rs);
    }

    @Override
    public List<Payment> getPaymentsPage(int limit, int offset, PaymentSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.page();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, limit, offset);
        return retrievePayments(conn, rs);
    }

    @Override
    public List<Payment> getPaymentsByPayer(BigInteger id, PaymentSortType sortType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byPayer();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, id);
        return retrievePayments(conn, rs);
    }

    @Override
    public List<Payment> getPaymentsByPayerPage(BigInteger id, int limit, int offset, PaymentSortType sortType)
            throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byPayerPage();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, id, limit, offset);
        return retrievePayments(conn, rs);
    }

    @Override
    public List<Payment> getPaymentsByReceiver(BigInteger id, PaymentSortType sortType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byReceiver();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, id);
        return retrievePayments(conn, rs);
    }

    @Override
    public List<Payment> getPaymentsByReceiverPage(BigInteger id, int limit, int offset,
                                                   PaymentSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byReceiverPage();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, id, limit, offset);
        return retrievePayments(conn, rs);
    }

    @Override
    public int countPayments() throws SQLException {
        Connection conn = dataSource.getConnection();
        String query = queryConstructor.countAll();
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrievePaymentsNumber(conn, rs);
    }

    @Override
    public int countPaymentsByPayer(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.countByPayer();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrievePaymentsNumber(conn, rs);
    }

    @Override
    public int countPaymentsByReceiver(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.countByReceiver();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrievePaymentsNumber(conn, rs);
    }

    @Override
    public BigDecimal getTotalReceiverIncomeByPayer(BigInteger payerId, BigInteger receiverId, int days)
            throws SQLException {
        if (days < 0) {
            return BigDecimal.ZERO;
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.totalReceiverIncomeByPayer();
        Timestamp past = Timestamp.valueOf(LocalDateTime.now().minusDays(days));
        BigDecimal amount = BigDecimal.ZERO;
        ResultSet rs = queryExecutor.execute(conn, rawQuery, payerId, receiverId, past);
        if (rs != null && rs.next() && rs.getBigDecimal(1) != null) {
            amount = rs.getBigDecimal(1);
            rs.getStatement().close();
        }
        conn.close();
        return amount;
    }

    private Payment buildInstance(ResultSet rs) throws SQLException {
        try {
            return new Payment(
                    new BigInteger(rs.getString(ID_COLUMN_NAME)),
                    rs.getBigDecimal(AMOUNT_COLUMN_NAME),
                    new BigInteger(rs.getString(PAYER_COLUMN_NAME)),
                    new BigInteger(rs.getString(RECEIVER_COLUMN_NAME)),
                    rs.getTimestamp(DATE_COLUMN_NAME).toLocalDateTime()
            );
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (method: %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create payment instance (invalid field values were obtained from database)",
                    INVALID_PARAM_SQLSTATE, CustomSQLCode.INVALID_DB_PARAMETER.getCode(), e);
        }
    }

    private List<Payment> retrievePayments(Connection conn, ResultSet rs) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                payments.add(buildInstance(rs));
            }
            rs.getStatement().close();
        }
        conn.close();
        return payments;
    }

    private int retrievePaymentsNumber(Connection conn, ResultSet rs) throws SQLException {
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.getStatement().close();
        }
        conn.close();
        return amount;
    }
}
