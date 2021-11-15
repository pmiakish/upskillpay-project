package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.PaymentParamException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public interface IncomeDao {
    boolean increaseBalance(Connection conn, BigDecimal amount) throws SQLException, PaymentParamException;
    boolean decreaseBalance(Connection conn, BigDecimal amount) throws SQLException, PaymentParamException;
    BigDecimal getBalance() throws SQLException;
}
