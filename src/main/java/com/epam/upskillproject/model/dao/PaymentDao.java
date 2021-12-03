package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dto.Payment;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PaymentSortType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Optional<Payment> getSinglePaymentById(BigInteger id) throws SQLException;
    List<Payment> getAllPayments(PaymentSortType sortType) throws SQLException;
    List<Payment> getPaymentsPage(int limit, int offset, PaymentSortType sortType) throws SQLException;
    List<Payment> getPaymentsByPayer(BigInteger id, PaymentSortType sortType) throws SQLException;
    List<Payment> getPaymentsByPayerPage(BigInteger id, int limit, int offset, PaymentSortType sortType) throws SQLException;
    List<Payment> getPaymentsByReceiver(BigInteger id, PaymentSortType sortType) throws SQLException;
    List<Payment> getPaymentsByReceiverPage(BigInteger id, int limit, int offset, PaymentSortType sortType) throws SQLException;
    int countPayments() throws SQLException;
    int countPaymentsByPayer(BigInteger id) throws SQLException;
    int countPaymentsByReceiver(BigInteger id) throws SQLException;
    BigDecimal getTotalReceiverIncomeByPayer(BigInteger payerId, BigInteger receiverId, int days) throws SQLException;
}
