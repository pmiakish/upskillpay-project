package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exception.CustomSQLCode;
import com.epam.upskillproject.exception.PaymentParamException;
import com.epam.upskillproject.model.dao.queryhandler.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandler.constructors.AccountQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.AccountSortType;
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
    private static final String INVALID_PARAM_SQLSTATE = "22023";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
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
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveAccount(conn, rs);
    }

    @Override
    public Optional<Account> getSingleAccountByIdAndOwner(BigInteger accountId, BigInteger ownerId) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.singleByIdAndOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, accountId, ownerId);
        return retrieveAccount(conn, rs);
    }

    @Override
    public List<Account> getAllAccounts(AccountSortType sortType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.all();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrieveAccountList(conn, rs);
    }

    @Override
    public List<Account> getAccountsPage(int limit, int offset, AccountSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.page();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, limit, offset);
        return retrieveAccountList(conn, rs);
    }

    @Override
    public List<Account> getAccountsByOwner(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveAccountList(conn, rs);
    }

    @Override
    public List<Account> getAccountsByOwnerPage(BigInteger id, int limit, int offset, AccountSortType sortType)
            throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byOwnerPage();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, id, limit, offset);
        return retrieveAccountList(conn, rs);
    }

    @Override
    public int countAccounts() throws SQLException {
        Connection conn = dataSource.getConnection();
        String query = queryConstructor.countAll();
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrieveAccountsNumber(conn, rs);
    }

    @Override
    public int countAccountsByOwner(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.countByOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveAccountsNumber(conn, rs);
    }

    @Override
    public boolean updateAccountStatus(BigInteger id, StatusType statusType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.updateStatus();
        int result = queryExecutor.executeUpdate(conn, rawQuery, statusType, id);
        conn.close();
        Optional<StatusType> updatedStatusType = getAccountStatus(id);
        return (result != 0 && updatedStatusType.isPresent() && updatedStatusType.get().equals(statusType));
    }

    @Override
    public Optional<StatusType> getAccountStatus(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.status();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        StatusType statusType = null;
        if (rs != null && rs.next()) {
            String dbValue = null;
            try {
                dbValue = rs.getString(1).toUpperCase();
                statusType = StatusType.valueOf(dbValue);
            } catch (IllegalArgumentException e) {
                logger.log(Level.INFO, "Cannot get status type: incompatible value retrieved: " + dbValue, e);
                return Optional.empty();
            } finally {
                rs.getStatement().close();
            }
        }
        conn.close();
        return (statusType != null) ? Optional.of(statusType) : Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getBalance(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.balance();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        BigDecimal balance = null;
        if (rs != null && rs.next()) {
            balance = rs.getBigDecimal(1);
            rs.getStatement().close();
        }
        conn.close();
        return (balance != null) ? Optional.of(balance) : Optional.empty();
    }

    /**
     * Adds a specified amount to an account balance. The method is intended for use as a part of a transaction.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an account id (a positive BigInteger value)
     * @param amount a positive BigInteger value
     * @return true in case of success, otherwise false
     * @throws SQLException
     * @throws PaymentParamException if amount has negative value or equals zero
     */
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

    /**
     * Subtract a specified amount from an account balance. The method is intended for use as a part of a transaction.
     * Possibility of subtracting an amount exceeding a balance value depends on database constraints.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an account id (positive BigInteger value)
     * @param amount a positive BigInteger value
     * @return true in case of success, otherwise false
     * @throws SQLException
     * @throws PaymentParamException if amount has negative value or equals zero
     */
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
    public synchronized Account addAccount(Account accountDto) throws SQLException {
        if (accountDto == null) {
            return null;
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.add();
        Date now = new Date(System.currentTimeMillis());
        queryExecutor.executeUpdate(conn, rawQuery,
                accountDto.getOwnerId(),
                (accountDto.getBalance() != null) ? accountDto.getBalance() : BigDecimal.ZERO,
                (accountDto.getStatus() != null) ? accountDto.getStatus() : StatusType.ACTIVE,
                (accountDto.getRegDate() != null) ? accountDto.getRegDate() : now);
        conn.close();
        return getAccountsByOwner(accountDto.getOwnerId()).stream().max(Comparator.comparing(Account::getId)).orElse(null);
    }

    /**
     * Removes an account which has a specified id. The method is intended for use as a part of a transaction after
     * removing all the cards associated with the account to avoid database constraints.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an account id (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
    @Override
    public synchronized boolean deleteSingleAccountById(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delSingleById();
        return (queryExecutor.executeUpdate(conn, rawQuery, id) != 0);
    }

    /**
     * Removes an account which has a specified account id and owner id. The method is intended for use as a part of
     * a transaction after removing all the cards associated with the account to avoid database constraints.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param accountId an account id (a positive BigInteger)
     * @param ownerId a person's id which owns an account (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
    @Override
    public synchronized boolean deleteSingleAccountByIdAndOwner(Connection conn, BigInteger accountId,
                                                                BigInteger ownerId) throws SQLException {
        String rawQuery = queryConstructor.delSingleByIdAndOwner();
        return (queryExecutor.executeUpdate(conn, rawQuery, accountId, ownerId) != 0);
    }

    /**
     * Removes all the accounts which has a specified owner id. The method is intended for use as a part of
     * a transaction after removing all the cards associated with the owner to avoid database constraints.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an owner's id (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
    @Override
    public synchronized boolean deleteAccountsByOwner(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByOwner();
        return (queryExecutor.executeUpdate(conn, rawQuery, id) != 0);
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
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (method: %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create account instance (invalid field values were obtained from database)",
                    INVALID_PARAM_SQLSTATE, CustomSQLCode.INVALID_DB_PARAMETER.getCode(), e);
        }
    }

    private Optional<Account> retrieveAccount(Connection conn, ResultSet rs) throws SQLException {
        Account account = null;
        if (rs != null && rs.next()) {
            account = buildInstance(rs);
            rs.getStatement().close();
        }
        conn.close();
        return (account != null) ? Optional.of(account) : Optional.empty();
    }

    private List<Account> retrieveAccountList(Connection conn, ResultSet rs) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                accounts.add(buildInstance(rs));
            }
            rs.getStatement().close();
        }
        conn.close();
        return accounts;
    }

    private int retrieveAccountsNumber(Connection conn, ResultSet rs) throws SQLException {
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.getStatement().close();
        }
        conn.close();
        return amount;
    }

}