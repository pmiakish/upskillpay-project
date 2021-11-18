package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDao {
    Optional<Account> getSingleAccountById(BigInteger id) throws SQLException;
    Optional<Account> getSingleAccountByIdAndOwner(BigInteger accountId, BigInteger ownerId) throws SQLException;
    List<Account> getAllAccounts(AccountSortType sortType) throws SQLException;
    List<Account> getAccountsPage(int limit, int offset, AccountSortType sortType) throws SQLException;
    List<Account> getAccountsByOwner(BigInteger id) throws SQLException;
    List<Account> getAccountsByOwnerPage(BigInteger id, int limit, int offset, AccountSortType sortType) throws SQLException;
    int countAccounts() throws SQLException;
    int countAccountsByOwner(BigInteger id) throws SQLException;
    boolean updateAccountStatus(BigInteger id, StatusType statusType) throws SQLException;
    Optional<StatusType> getAccountStatus(BigInteger id) throws SQLException;
    Optional<BigDecimal> getBalance(BigInteger id) throws SQLException;
    boolean increaseBalance(Connection conn, BigInteger id, BigDecimal amount) throws SQLException, PaymentParamException;
    boolean decreaseBalance(Connection conn, BigInteger id, BigDecimal amount) throws SQLException, PaymentParamException;
    Account addAccount(BigInteger ownerId, BigDecimal initialBalance, StatusType statusType) throws SQLException;
    boolean deleteSingleAccountById(Connection conn, BigInteger id) throws SQLException;
    boolean deleteSingleAccountByIdAndOwner(Connection conn, BigInteger accountId, BigInteger ownerId) throws SQLException;
    boolean deleteAccountsByOwner(Connection conn, BigInteger id) throws SQLException;
}