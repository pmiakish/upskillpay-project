package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.AccountQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
public class AccountDaoImpl implements AccountDao {

    private static final Logger logger = LogManager.getLogger(AccountDaoImpl.class.getName());

    private static final String ID_COLUMN_NAME = "ID";
    private static final String OWNER_COLUMN_NAME = "OWNER";
    private static final String BALANCE_COLUMN_NAME = "BALANCE";
    private static final String REGDATE_COLUMN_NAME = "REGDATE";
    private static final String STATUS_COLUMN_ALIAS = "statName";

    private final AccountQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;
    private final OrderStrategy orderStrategy;

    @Inject
    public AccountDaoImpl(AccountQueryConstructor queryConstructor, QueryExecutor queryExecutor,
                          @Named("accountOrder") OrderStrategy orderStrategy) {
        this.queryConstructor = queryConstructor;
        this.queryExecutor = queryExecutor;
        this.orderStrategy = orderStrategy;
    }

    @Override
    public Optional<Account> getSingleAccountById(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        Account account = null;
        if (rs != null && rs.next()) {
            account = buildInstance(rs);
            rs.close();
        }
        return (account != null) ? Optional.of(account) : Optional.empty();
    }

    @Override
    public Optional<Account> getSingleAccountByIdAndOwner(BigInteger accountId, BigInteger ownerId) throws SQLException {
        String rawQuery = queryConstructor.singleByIdAndOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, accountId, ownerId);
        Account account = null;
        if (rs != null && rs.next()) {
            account = buildInstance(rs);
            rs.close();
        }
        return (account != null) ? Optional.of(account) : Optional.empty();
    }

    @Override
    public List<Account> getAllAccounts(AccountSortType sortType) throws SQLException {
        String rawQuery = queryConstructor.all();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)));
        List<Account> accounts = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                accounts.add(buildInstance(rs));
            }
            rs.close();
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsPage(int limit, int offset, AccountSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)), limit, offset);
        List<Account> accounts = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                accounts.add(buildInstance(rs));
            }
            rs.close();
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByOwner(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.byOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        List<Account> accounts = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                accounts.add(buildInstance(rs));
            }
            rs.close();
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByOwnerPage(BigInteger id, int limit, int offset, AccountSortType sortType)
            throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.byOwnerPage();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)), id, limit, offset);
        List<Account> accounts = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                accounts.add(buildInstance(rs));
            }
            rs.close();
        }
        return accounts;
    }

    @Override
    public int countAccounts() throws SQLException {
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
    public int countAccountsByOwner(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.countByOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public synchronized boolean updateAccountStatus(BigInteger id, StatusType statusType) throws SQLException {
        String rawQuery = queryConstructor.updateStatus();
        int result = queryExecutor.executeUpdate(rawQuery, statusType, id);
        Optional<StatusType> updatedStatusType = getAccountStatus(id);
        return (result != 0 && updatedStatusType.isPresent() && updatedStatusType.get().equals(statusType));
    }

    @Override
    public Optional<StatusType> getAccountStatus(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.status();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        StatusType statusType = null;
        if (rs != null && rs.next()) {
            try {
                statusType = StatusType.valueOf(rs.getString(1).toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            } finally {
                rs.close();
            }
        }
        return (statusType != null) ? Optional.of(statusType) : Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getBalance(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.balance();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        BigDecimal balance = null;
        if (rs != null && rs.next()) {
            balance = rs.getBigDecimal(1);
            rs.close();
        }
        return (balance != null) ? Optional.of(balance) : Optional.empty();
    }

    // should use the method as a part of a transaction
    @Override
    public synchronized boolean increaseBalance(Connection conn, BigInteger id, BigDecimal amount) throws SQLException,
            PaymentParamException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.log(Level.WARN, String.format("Incorrect amount value passed to %s (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), amount));
            throw new PaymentParamException("Incorrect payment parameters (amount equals or less than zero)");
        }
        String rawQuery = queryConstructor.increase();
        int result = queryExecutor.executeUpdate(conn, rawQuery, amount, id);
        return (result != 0);
    }

    // should use the method as a part of a transaction
    @Override
    public synchronized boolean decreaseBalance(Connection conn, BigInteger id, BigDecimal amount) throws SQLException,
            PaymentParamException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.log(Level.WARN, String.format("Incorrect amount value passed to %s (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), amount));
            throw new PaymentParamException("Incorrect payment parameters (amount equals or less than zero)");
        }
        String rawQuery = queryConstructor.decrease();
        int result = queryExecutor.executeUpdate(conn, rawQuery, amount, id);
        return (result != 0);
    }

    @Override
    public synchronized Account addAccount(BigInteger ownerId, BigDecimal initialBalance, StatusType statusType)
            throws SQLException {
        String rawQuery = queryConstructor.add();
        Date now = new Date(System.currentTimeMillis());
        queryExecutor.executeUpdate(rawQuery, ownerId, initialBalance, statusType, now);
        return getAccountsByOwner(ownerId).stream().max(Comparator.comparing(Account::getId)).orElse(null);
    }

    // should use the method as a transaction part after removing all the cards associated with the account
    @Override
    public synchronized boolean deleteSingleAccountById(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delSingleById();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    // should use the method as a transaction part after removing all the cards associated with the account
    @Override
    public synchronized boolean deleteSingleAccountByIdAndOwner(Connection conn, BigInteger accountId, BigInteger ownerId)
            throws SQLException {
        String rawQuery = queryConstructor.delSingleByIdAndOwner();
        int result = queryExecutor.executeUpdate(conn, rawQuery, accountId, ownerId);
        return (result != 0);
    }

    // should use the method as a transaction part after removing all the cards associated with the owner
    @Override
    public synchronized boolean deleteAccountsByOwner(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByOwner();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    private Account buildInstance(ResultSet rs) throws SQLException {
        try {
            return new Account(
                    new BigInteger(rs.getString(ID_COLUMN_NAME)),
                    new BigInteger(rs.getString(OWNER_COLUMN_NAME)),
                    rs.getBigDecimal(BALANCE_COLUMN_NAME),
                    StatusType.valueOf(rs.getString(STATUS_COLUMN_ALIAS)),
                    rs.getDate(REGDATE_COLUMN_NAME).toLocalDate()
            );
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create account instance (invalid field values were obtained from database)", e);
        }
    }
}