package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dto.Transaction;
import com.epam.upskillproject.model.service.sort.TransactionSortType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Optional<Transaction> getSinglePaymentById(BigInteger id) throws SQLException;
    List<Transaction> getAllPayments(TransactionSortType sortType) throws SQLException;
    List<Transaction> getPaymentsPage(int limit, int offset, TransactionSortType sortType) throws SQLException;
    List<Transaction> getPaymentsByPayer(BigInteger id, TransactionSortType sortType) throws SQLException;
    List<Transaction> getPaymentsByPayerPage(BigInteger id, int limit, int offset, TransactionSortType sortType) throws SQLException;
    List<Transaction> getPaymentsByReceiver(BigInteger id, TransactionSortType sortType) throws SQLException;
    List<Transaction> getPaymentsByReceiverPage(BigInteger id, int limit, int offset, TransactionSortType sortType) throws SQLException;
    int countPayments() throws SQLException;
    int countPaymentsByPayer(BigInteger id) throws SQLException;
    int countPaymentsByReceiver(BigInteger id) throws SQLException;
    BigDecimal getTotalReceiverIncomeByPayer(BigInteger payerId, BigInteger receiverId, int days) throws SQLException;
}
