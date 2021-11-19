package com.epam.upskillproject.model.dao.queryhandlers;

import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.exceptions.TransactionExceptionType;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dao.AccountDao;
import com.epam.upskillproject.model.dao.CardDao;
import com.epam.upskillproject.model.dao.IncomeDao;
import com.epam.upskillproject.model.dao.PersonDao;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.CardNetworkType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
public class FinancialTransactionsPerformer {

    private static final Logger logger = LogManager.getLogger(FinancialTransactionsPerformer.class.getName());

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigInteger SYSTEM_INCOME_ID = BigInteger.ZERO;
    private static final String COMMISSION_RATE_PROP = "system.payments.commissionRate";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
    private BigDecimal commissionRate;
    private final IncomeDao incomeDao;
    private final AccountDao accountDao;
    private final CardDao cardDao;
    private final PersonDao personDao;
    private final PaymentTransactionsRecorder paymentTransactionsRecorder;
    private final PropertiesKeeper propertiesKeeper;

    @Inject
    public FinancialTransactionsPerformer(IncomeDao incomeDao, AccountDao accountDao, CardDao cardDao,
                                          PersonDao personDao, PaymentTransactionsRecorder paymentTransactionsRecorder,
                                          PropertiesKeeper propertiesKeeper) {
        this.incomeDao = incomeDao;
        this.accountDao = accountDao;
        this.cardDao = cardDao;
        this.personDao = personDao;
        this.paymentTransactionsRecorder = paymentTransactionsRecorder;
        this.propertiesKeeper = propertiesKeeper;
    }

    public synchronized void makePayment(BigDecimal amount, BigInteger payerId, BigInteger receiverId)
            throws TransactionException {
        if (checkPaymentParams(amount, payerId, receiverId)) {
            amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
            BigDecimal commissionAmount = (payerId.equals(BigInteger.ZERO) || receiverId.equals(BigInteger.ZERO)) ?
                    BigDecimal.ZERO : amount.multiply(commissionRate).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
            Connection conn = startTransaction();
            logger.log(Level.TRACE, String.format("Payment transaction started (amount: %s, payer's account: %s, " +
                    "receiver's account: %s)", amount, payerId, receiverId));
            if (conn != null) {
                try {
                    Optional<StatusType> receiverStatus = accountDao.getAccountStatus(receiverId);
                    if (!receiverId.equals(SYSTEM_INCOME_ID) &&
                            (receiverStatus.isEmpty() || receiverStatus.get().equals(StatusType.BLOCKED))
                    ) {
                        logger.log(Level.INFO, String.format("Payment transaction: receiver's account status is " +
                                "'BLOCKED' or empty (id: %s)", receiverId));
                        endTransaction(conn, false, new IllegalStateException("Receiver's status does not " +
                                "allow to perform a payment"));
                    }
                    debit(conn, payerId, amount.add(commissionAmount));
                    put(conn, receiverId, amount);
                    paymentTransactionsRecorder.commit(conn, amount, payerId, receiverId);
                    if (!commissionAmount.equals(BigDecimal.ZERO)) {
                        incomeDao.increaseBalance(conn, commissionAmount);
                        paymentTransactionsRecorder.commit(conn, commissionAmount, payerId, SYSTEM_INCOME_ID);
                    }
                    logger.log(Level.TRACE, String.format("Payment transaction successfully finished (amount: %s, " +
                            "payer's account: %s, receiver's account: %s)", amount, payerId, receiverId));
                    endTransaction(conn, true, null);
                } catch (SQLException e) {
                    logger.log(Level.WARN, String.format("Cannot perform payment transaction: an exception was thrown" +
                            " during execution (amount: %s, payer's account: %s, receiver's account: %s)",
                            amount, payerId, receiverId), e);
                    endTransaction(conn, false, e);
                }
            } else {
                logger.log(Level.WARN, String.format("Cannot perform payment transaction: passed connection is not " +
                        "valid (amount: %s, payer's account: %s, receiver's account: %s)", amount, payerId, receiverId));
                throw new TransactionException(TransactionExceptionType.CONNECTION,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.log(Level.INFO, String.format("Payment cannot be implemented: bad parameters " +
                    "(amount: %s, payer's account: %s, receiver's account: %s)", amount, payerId, receiverId));
            throw new TransactionException(TransactionExceptionType.BAD_PARAM, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public synchronized String issueCard(BigInteger ownerId, BigInteger accountId, CardNetworkType cardNetworkType)
            throws TransactionException {
        String cvc = "";
        if (ownerId != null && accountId != null && cardNetworkType != null) {
            Connection conn = startTransaction();
            logger.log(Level.TRACE, String.format("Card issue transaction started (owner: %s, account: %s, " +
                    "card network: %s)", ownerId, accountId, cardNetworkType));
            if (conn != null) {
                try {
                    Optional<Person> owner = personDao.getSinglePersonById(ownerId);
                    Optional<Account> account = accountDao.getSingleAccountById(accountId);
                    if (owner.isEmpty() || owner.get().getStatus().equals(StatusType.BLOCKED) ||
                            account.isEmpty() || account.get().getStatus().equals(StatusType.BLOCKED)) {
                        logger.log(Level.INFO, String.format("Card issue transaction cannot be implemented: customer " +
                                "and customer's account may not be blocked or empty (owner: %s, account: %s, " +
                                "card network: %s)", ownerId, accountId, cardNetworkType));
                        endTransaction(conn, false, new IllegalStateException("Account and account owner may " +
                                "not be blocked for card issue"));
                    }
                    cvc = cardDao.addCard(conn, ownerId, accountId, cardNetworkType);
                    if (cvc != null) {
                        BigDecimal amount = cardNetworkType.getCost();
                        debit(conn, accountId, amount);
                        put(conn, SYSTEM_INCOME_ID, amount);
                        paymentTransactionsRecorder.commit(conn, amount, accountId, SYSTEM_INCOME_ID);
                        logger.log(Level.TRACE, String.format("Card issue transaction successfully finished " +
                                "(owner: %s, account: %s, card network: %s)", ownerId, accountId, cardNetworkType));
                        endTransaction(conn, true, null);
                    } else {
                        logger.log(Level.INFO, String.format("Card issue transaction failed - null result obtained " +
                                        "from CardDao (owner: %s, account: %s, card network: %s)", ownerId, accountId,
                                cardNetworkType));
                        endTransaction(conn, false, null);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARN, String.format("Cannot perform card issue transaction: an exception was " +
                                    "thrown during execution (owner: %s, account: %s, card network: %s)",
                            ownerId, accountId, cardNetworkType), e);
                    endTransaction(conn, false, e);
                }
            } else {
                logger.log(Level.WARN, String.format("Cannot perform card issue transaction: passed connection is not" +
                        " valid (owner: %s, account: %s, card network: %s)", ownerId, accountId, cardNetworkType));
                throw new TransactionException(TransactionExceptionType.CONNECTION,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return cvc;
    }

    public synchronized void deletePerson(BigInteger id) throws TransactionException {
        if (id != null) {
            Connection conn = startTransaction();
            logger.log(Level.TRACE, String.format("Person delete transaction started (id: %s)", id));
            if (conn != null) {
                try {
                    if (personDao.getSinglePersonById(id).isEmpty()) {
                        logger.log(Level.INFO, String.format("Person delete transaction cannot be implemented: cannot" +
                                " find person (id: %s)", id));
                        endTransaction(conn, false, new IllegalArgumentException("Person with specified id is " +
                                "not exists"));
                    }
                    List<Account> accounts = accountDao.getAccountsByOwner(id);
                    for (Account acc : accounts) {
                        cardDao.deleteCardsByAccount(conn, acc.getId());
                        if (acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                            accountDao.updateAccountStatus(acc.getId(), StatusType.ACTIVE);
                            makePayment(acc.getBalance(), acc.getId(), SYSTEM_INCOME_ID);
                        }
                        accountDao.deleteSingleAccountById(conn, acc.getId());
                    }
                    personDao.deletePersonById(conn, id);
                    logger.log(Level.TRACE, String.format("Person delete transaction successfully finished (id: %s)",
                            id));
                    endTransaction(conn, true, null);
                } catch (SQLException e) {
                    logger.log(Level.WARN, String.format("Cannot perform person delete transaction: an exception was " +
                            "thrown during execution (id: %s)", id), e);
                    endTransaction(conn, false, e);
                }
            } else {
                logger.log(Level.WARN, String.format("Cannot perform person delete transaction: passed connection is " +
                        "not valid (id: %s)", id));
                throw new TransactionException(TransactionExceptionType.CONNECTION,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.log(Level.INFO, "Person delete transaction cannot be implemented: bad person id " +
                    "parameter (id is null)");
            throw new TransactionException(TransactionExceptionType.BAD_PARAM, HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect person's id");
        }
    }

    public synchronized boolean deleteAccount(BigInteger id) throws TransactionException {
        if (id != null) {
            Connection conn = startTransaction();
            logger.log(Level.TRACE, String.format("Account delete transaction started (id: %s)", id));
            if (conn != null) {
                try {
                    if (accountDao.getSingleAccountById(id).isEmpty()) {
                        logger.log(Level.INFO, String.format("Account delete transaction cannot be implemented: " +
                                "cannot find account (id: %s)", id));
                        endTransaction(conn, false, new IllegalArgumentException("Account with specified id " +
                                "is not exists"));
                    }
                    cardDao.deleteCardsByAccount(conn, id);
                    Optional<BigDecimal> balance = accountDao.getBalance(id);
                    if (balance.isPresent() && balance.get().compareTo(BigDecimal.ZERO) > 0) {
                        makePayment(balance.get(), id, SYSTEM_INCOME_ID);
                    }
                    accountDao.deleteSingleAccountById(conn, id);
                    logger.log(Level.TRACE, String.format("Account delete transaction successfully finished (id: %s)",
                            id));
                    endTransaction(conn, true, null);
                    return accountDao.getSingleAccountById(id).isEmpty();
                } catch (SQLException e) {
                    logger.log(Level.WARN, String.format("Cannot perform account delete transaction: an exception was" +
                            " thrown during execution (id: %s)", id), e);
                    endTransaction(conn, false, e);
                }
            } else {
                logger.log(Level.WARN, String.format("Cannot perform account delete transaction: passed connection is" +
                        " not valid (id: %s)", id));
                throw new TransactionException(TransactionExceptionType.CONNECTION,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.log(Level.INFO, "Account delete transaction cannot be implemented: bad account id " +
                    "parameter (id is null)");
            throw new TransactionException(TransactionExceptionType.BAD_PARAM, HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect account id");
        }
        return false;
    }

    private Connection startTransaction()  {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return conn;
        } catch (SQLException e) {
            logger.log(Level.WARN, "Cannot create connection for transaction", e);
            return null;
        }
    }

    private void endTransaction(Connection conn, boolean success, Throwable throwable) throws TransactionException {
        if (conn == null) {
            logger.log(Level.WARN, "Cannot end transaction properly because connection is null");
            throw new TransactionException(TransactionExceptionType.CONNECTION,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        // Commit section
        if (success) {
            try {
                conn.commit();
            } catch (SQLException commitEx) {
                success = false;
                logger.log(Level.WARN, "Cannot end transaction properly because of throwing exception " +
                        "during commit", commitEx);
            }
        }
        // Rollback and close-connection section
        try {
            if (!success) {
                conn.rollback();
                logger.log(Level.WARN, "Rollback transaction");
                throw (throwable != null) ?
                        new TransactionException(TransactionExceptionType.PERFORM,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, throwable.getMessage()) :
                        new TransactionException(TransactionExceptionType.PERFORM,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (SQLException rollbackEx) {
            logger.log(Level.ERROR, "Cannot rollback transaction properly because of throwing exception", rollbackEx);
            throw new TransactionException(TransactionExceptionType.ROLLBACK,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, rollbackEx.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException closeEx) {
                logger.log(Level.ERROR, "Cannot close connection properly because of throwing exception", closeEx);
            }
        }
    }

    private synchronized void debit(Connection conn, BigInteger payerId, BigDecimal amount) throws SQLException {
        if (payerId.equals(SYSTEM_INCOME_ID)) {
            incomeDao.decreaseBalance(conn, amount);
        } else {
            accountDao.decreaseBalance(conn, payerId, amount);
        }
    }

    private synchronized void put(Connection conn, BigInteger receiverId, BigDecimal amount) throws SQLException {
        if (receiverId.equals(SYSTEM_INCOME_ID)) {
            incomeDao.increaseBalance(conn, amount);
        } else {
            accountDao.increaseBalance(conn, receiverId, amount);
        }
    }

    private boolean checkPaymentParams(BigDecimal amount, BigInteger payerId, BigInteger receiverId) {
        if (
                amount.compareTo(BigDecimal.ZERO) > 0 &&
                payerId != null && receiverId != null &&
                payerId.compareTo(BigInteger.ZERO) >= 0 &&
                receiverId.compareTo(BigInteger.ZERO) >= 0 &&
                !payerId.equals(receiverId)
        ) {
            try {
                if (
                        (!payerId.equals(SYSTEM_INCOME_ID) && accountDao.getSingleAccountById(payerId).isEmpty()) ||
                        (!receiverId.equals(SYSTEM_INCOME_ID) && accountDao.getSingleAccountById(receiverId).isEmpty())
                ) {
                    logger.log(Level.INFO, "Incorrect payment parameters passed: payer's or receiver's " +
                            "account not found");
                    return false;
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, "Cannot check payment parameters: an exception was thrown during " +
                        "payer's and receiver's accounts checking", e);
                return false;
            }
            return true;
        }
        logger.log(Level.INFO, "Incorrect payment parameters passed");
        return false;
    }

    @PostConstruct
    public void init() throws ServletException {
        commissionRate = propertiesKeeper
                .getBigDecimal(COMMISSION_RATE_PROP)
                .setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

}
