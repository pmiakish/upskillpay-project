package com.epam.upskillproject.model.service;

import com.epam.upskillproject.exceptions.AccountLimitException;
import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.model.dao.*;
import com.epam.upskillproject.model.dao.queryhandlers.FinancialTransactionsPerformer;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.service.sort.AccountSortType;
import jakarta.ejb.EJBException;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class CustomerService {

    private static final Logger logger = LogManager.getLogger(CustomerService.class.getName());

    private static final AccountSortType DEFAULT_ACCOUNT_SORT_TYPE = AccountSortType.ID;
    private static final BigInteger SYSTEM_INCOME_ID = BigInteger.ZERO;
    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 5;
    private static final int MAX_CARDS_PER_ACCOUNT = 3;
    private static final BigDecimal MAX_TOP_UP_AMOUNT_PER_PERIOD = new BigDecimal(100.00);
    private static final int TOP_UP_PERIOD_DAYS = 1;

    private final PersonDao personDao;
    private final AccountDao accountDao;
    private final CardDao cardDao;
    private final PaymentDao paymentDao;
    private final FinancialTransactionsPerformer financialTransactionsPerformer;
    private final CardValidator cardValidator;

    @Inject
    public CustomerService(PersonDao personDao, AccountDao accountDao, CardDao cardDao, PaymentDao paymentDao,
                           FinancialTransactionsPerformer financialTransactionsPerformer, CardValidator cardValidator) {
        this.personDao = personDao;
        this.accountDao = accountDao;
        this.cardDao = cardDao;
        this.paymentDao = paymentDao;
        this.financialTransactionsPerformer = financialTransactionsPerformer;
        this.cardValidator = cardValidator;
    }

    /**
     * Finds and returns an instance of a customer by a user principal
     * @param principal java.security.Principal (from security context)
     * @return a Person (customer's instance) if present, otherwise null
     * @throws SQLException
     */
    public Person getUserPerson(Principal principal) throws SQLException {
        if (!checkParams(principal)) {
            logger.log(Level.WARN, "Cannot get user's person (invalid principal)");
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        return person.orElse(null);
    }

    /**
     * Finds and returns an account instance by its id and a user principal (an owner)
     * @param principal java.security.Principal (from security context)
     * @return an Account if present, otherwise null
     * @throws SQLException
     */
    public Account getUserAccountById(Principal principal, BigInteger accountId) throws SQLException {
       if (!checkParams(principal, accountId)) {
           logger.log(Level.WARN, String.format("Cannot get user's account (invalid principal or account id: %s)",
                   accountId));
           return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            Optional<Account> account = accountDao.getSingleAccountByIdAndOwner(accountId, person.get().getId());
            return account.orElse(null);
        } else {
            logger.log(Level.WARN, String.format("Cannot get user's account (principal person is not found, account: %s)",
                    accountId));
            return null;
        }
    }

    /**
     * Creates a Page of user's accounts
     * @param principal java.security.Principal (from security context)
     * @param amount a number of accounts in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType AccountSortType. If a passed value is null, will be used a default sort type
     * @return a Page of user's accounts or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Account> getUserAccountsPage(Principal principal, int amount, int pageNumber, AccountSortType sortType)
            throws SQLException {
        if (!checkParams(principal, amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of user's accounts (invalid page parameters or principal)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_ACCOUNT_SORT_TYPE;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            int offset = amount * (pageNumber - 1);
            List<Account> entries = accountDao.getAccountsByOwnerPage(person.get().getId(), amount, offset, sortType);
            int total = accountDao.countAccountsByOwner(person.get().getId());
            return new Page<>(entries, pageNumber, amount, total, sortType);
        } else {
            logger.log(Level.WARN, String.format("Cannot get page of user's accounts (principal person is not found: %s)",
                    principal.getName()));
            return null;
        }
    }

    /**
     * Collects and returns a list of user's cards by account id
     * @param principal java.security.Principal (from security context)
     * @param accountId a positive BigInteger
     * @return a List of cards or an empty List if specified cards are not found
     * @throws SQLException
     */
    public List<Card> getUserCardsByAccount(Principal principal, BigInteger accountId) throws SQLException {
        if (!checkParams(principal, accountId)) {
            logger.log(Level.WARN, String.format("Cannot get user's cards (invalid principal or account id: %s)",
                    accountId));
            return new ArrayList<>();
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            return cardDao.getCardsByAccount(accountId).stream()
                    .filter(card -> card.getOwnerId().equals(person.get().getId())).collect(Collectors.toList());
        } else {
            logger.log(Level.WARN, String.format("Cannot get user's cards (principal person is not found: %s)",
                    principal.getName()));
            return new ArrayList<>();
        }
    }

    /**
     * Blocks a user's account
     * @param principal java.security.Principal (from security context)
     * @param accountId a positive BigInteger
     * @return true if an account was blocked, otherwise false
     * @throws SQLException
     */
    public boolean blockUserAccount(Principal principal, BigInteger accountId) throws SQLException {
        if (!checkParams(principal, accountId)) {
            logger.log(Level.WARN, String.format("Cannot block user's account (invalid principal or account id: %s)",
                    accountId));
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            Optional<Account> account = accountDao.getSingleAccountByIdAndOwner(accountId, person.get().getId());
            if (account.isPresent()) {
                for (Card card : getUserCardsByAccount(principal, account.get().getId())) {
                    logger.log(Level.TRACE, String.format("Try to block user's card (account: %s, card: %s)",
                            accountId, card.getId()));
                    cardDao.updateCardStatus(card.getId(), StatusType.BLOCKED);
                }
                logger.log(Level.TRACE, String.format("Try to block user's account (id: %s)", accountId));
                return accountDao.updateAccountStatus(account.get().getId(), StatusType.BLOCKED);
            }
        }
        logger.log(Level.WARN, String.format("Cannot block a user's account (person or account are not found) [%s]",
                principal.getName()));
        return false;
    }

    /**
     * Blocks a user's account
     * @param principal java.security.Principal (from security context)
     * @param cardId a positive BigInteger
     * @return true if a card was blocked, otherwise false
     * @throws SQLException
     */
    public boolean blockUserCard(Principal principal, BigInteger cardId) throws SQLException {
        if (!checkParams(principal, cardId)) {
            logger.log(Level.WARN, String.format("Cannot block user's card (invalid principal or card id: %s)",
                    cardId));
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            Optional<Card> card = cardDao.getSingleCardByIdAndOwner(cardId, person.get().getId());
            if (card.isPresent()) {
                logger.log(Level.TRACE, String.format("Try to block user's card (id: %s)", cardId));
                cardDao.updateCardStatus(card.get().getId(), StatusType.BLOCKED);
                return true;
            }
        }
        logger.log(Level.WARN, String.format("Cannot block a user's card (person or card are not found) [%s]",
                principal.getName()));
        return false;
    }

    /**
     * Allows to top up an account by id (within the established limit). Performs a payment from the system account to
     * a customer's account
     * @param principal java.security.Principal (from security context)
     * @param accountId a positive BigInteger (a receiver)
     * @param amount a positive BigDecimal
     * @return true if the specified top up limit will not be reached as a result of operation and payment
     * successfully finished or false in other cases
     * @throws SQLException
     * @throws AccountLimitException if expected that the specified top up limit will be reached as an operation result
     * @throws PaymentParamException if invalid payment parameters passed
     */
    public boolean topUpAccount(Principal principal, BigInteger accountId, BigDecimal amount) throws SQLException,
            AccountLimitException, PaymentParamException, TransactionException {
        if (!checkParams(principal, accountId, amount)) {
            logger.log(Level.WARN, String.format("Cannot top up account: incorrect payment parameters (principal " +
                    "name: %s, accountId: %s, amount: %s)", principal.getName(), accountId, amount));
            throw new PaymentParamException("Cannot top up account: incorrect payment parameters");
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            Optional<Account> account = accountDao.getSingleAccountByIdAndOwner(accountId, person.get().getId());
            if (account.isPresent()) {
                BigDecimal totalPerPeriod = paymentDao.getTotalReceiverIncomeByPayer(SYSTEM_INCOME_ID,
                        account.get().getId(), TOP_UP_PERIOD_DAYS);
                if (totalPerPeriod.add(amount).compareTo(MAX_TOP_UP_AMOUNT_PER_PERIOD) > 0) {
                    logger.log(Level.INFO, String.format("Cannot top up account: entered amount exceeds allowable " +
                            "limit [limit: %s, accrued: %s] (principal name: %s, accountId: %s, amount: %s)",
                            MAX_TOP_UP_AMOUNT_PER_PERIOD, totalPerPeriod, principal.getName(), accountId, amount));
                    throw new AccountLimitException("Entered amount exceeds allowable limit");
                } else {
                    logger.log(Level.TRACE, String.format("Try to top up user's account (principal name: %s, " +
                                    "accountId: %s, amount: %s)", principal.getName(), accountId, amount));
                    financialTransactionsPerformer.makePayment(amount, SYSTEM_INCOME_ID, account.get().getId());
                    return true;
                }
            }
        }
        logger.log(Level.WARN, String.format("Cannot top up account: person or account are not found (principal " +
                "name: %s, accountId: %s, amount: %s)", principal.getName(), accountId, amount));
        return false;
    }

    /**
     * Adds a new user's account
     * @param principal java.security.Principal (from security context)
     * @return true if a new account was added, otherwise false
     * @throws SQLException
     * @throws AccountLimitException if the maximum number of customer's accounts exceeded
     */
    public boolean addUserAccount(Principal principal) throws SQLException, AccountLimitException {
        if (!checkParams(principal)) {
            logger.log(Level.WARN, "Cannot add user's account (invalid principal)");
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            if (accountDao.countAccountsByOwner(person.get().getId()) < MAX_ACCOUNTS_PER_CUSTOMER) {
                logger.log(Level.TRACE, String.format("Try to create a new user's account (user id: %s)",
                        person.get().getId()));
                return (accountDao.addAccount(person.get().getId(), BigDecimal.ZERO, StatusType.ACTIVE) != null);
            } else {
                logger.log(Level.INFO, String.format("Cannot create a new user's account (user id: %s): the maximum " +
                        "number of customer's accounts (%s) exceeded", person.get().getId(), MAX_ACCOUNTS_PER_CUSTOMER));
                throw new AccountLimitException("The maximum number of customer's accounts exceeded");
            }
        } else {
            logger.log(Level.WARN, String.format("Cannot add a new user's account (principal person is not found: %s)",
                    principal.getName()));
            return false;
        }
    }

    /**
     * Removes a user's account. When removing transfers entire amount from the account to the system income
     * @param principal java.security.Principal (from security context)
     * @param accountId a positive BigInteger
     * @return true if a user's account was deleted or false in other cases
     * @throws SQLException
     */
    public boolean deleteUserAccount(Principal principal, BigInteger accountId) throws SQLException,
            TransactionException {
        if (!checkParams(principal, accountId)) {
            logger.log(Level.WARN, String.format("Cannot delete a user's account: incorrect parameters (principal " +
                    "name: %s, account id: %s)", principal.getName(), accountId));
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent() && accountDao.getSingleAccountByIdAndOwner(accountId, person.get().getId()).isPresent()) {
            logger.log(Level.TRACE, String.format("Try to delete user's account (principal name: %s, account id: %s)",
                    principal.getName(), accountId));
            return financialTransactionsPerformer.deleteAccount(accountId);
        }
        logger.log(Level.WARN, String.format("Cannot delete account: person or account are not found (principal " +
                "name: %s, account id: %s)", principal.getName(), accountId));
        return false;
    }

    /**
     * Adds a new user's card
     * @param principal java.security.Principal (from security context)
     * @param accountId a positive BigInteger
     * @param cardNetworkType not null CardNetworkType
     * @return true if a new card was added, otherwise false
     * @throws SQLException
     * @throws AccountLimitException if the maximum account cards number exceeded
     */
    public String addUserCard(Principal principal, BigInteger accountId, CardNetworkType cardNetworkType) throws
            SQLException, AccountLimitException, TransactionException {
        if (!checkParams(principal, accountId, cardNetworkType)) {
            logger.log(Level.WARN, "Cannot add user's card (incorrect parameters)");
            return "";
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent() && accountDao.getSingleAccountByIdAndOwner(accountId, person.get().getId()).isPresent()) {
            String cvc = "";
            if (cardDao.countCardsByAccount(accountId) < MAX_CARDS_PER_ACCOUNT) {
                logger.log(Level.TRACE, String.format("Try to create a new user's card (account id: %s)", accountId));
                cvc = financialTransactionsPerformer.issueCard(person.get().getId(), accountId, cardNetworkType);
            } else {
                logger.log(Level.INFO, String.format("Cannot create a new user's card (user id: %s): the maximum " +
                        "number of account cards (%s) exceeded", person.get().getId(), MAX_CARDS_PER_ACCOUNT));
                throw new AccountLimitException("The maximum number of customer's cards exceeded");
            }
            return cvc;
        } else {
            logger.log(Level.WARN, String.format("Cannot add card to account: person or account are not found " +
                    "(principal name: %s, account id: %s)", principal.getName(), accountId));
            return "";
        }
    }

    /**
     * Removes a user's card
     * @param principal java.security.Principal (from security context)
     * @param cardId a positive BigInteger
     * @return true if a user's card was deleted or false in other cases
     * @throws SQLException
     */
    public boolean deleteUserCard(Principal principal, BigInteger cardId) throws SQLException {
        if (!checkParams(principal, cardId)) {
            logger.log(Level.WARN, String.format("Cannot delete a user's card: incorrect parameters (principal " +
                    "name: %s, card id: %s)", principal.getName(), cardId));
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent() && cardDao.getSingleCardByIdAndOwner(cardId, person.get().getId()).isPresent()) {
            logger.log(Level.TRACE, String.format("Try to delete user's card (principal name: %s, card id: %s)",
                    principal.getName(), cardId));
            return cardDao.deleteCardByIdAndOwner(cardId, person.get().getId());
        }
        logger.log(Level.WARN, String.format("Cannot delete card: person or card are not found (principal " +
                "name: %s, card id: %s)", principal.getName(), cardId));
        return false;
    }


    /**
     * Performs a payment by using valid card to receiver's account
     * @param principal java.security.Principal (from security context)
     * @param payerCardId a positive BigInteger
     * @param cvc cvc-string (must contain three digits)
     * @param receiverAccountId a positive BigInteger
     * @param amount a positive BigDecimal
     * @throws SQLException
     * @throws PaymentParamException if invalid payment parameters passed
     * @throws TransactionException exception might be thrown by FinancialTransactionsPerformer instance
     */
    public void performPayment(Principal principal, BigInteger payerCardId, String cvc, BigInteger receiverAccountId,
                               BigDecimal amount) throws SQLException, PaymentParamException, TransactionException {
        if (!checkParams(principal, payerCardId, receiverAccountId, amount)) {
            logger.log(Level.WARN, String.format("Cannot perform payment: incorrect parameters (principal " +
                    "name: %s, payer's card id: %s, receiver's account id: %s, amount: %s)", principal.getName(),
                    payerCardId, receiverAccountId, amount));
            throw new PaymentParamException("Cannot perform payment: incorrect parameters passed");
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(principal.getName());
        if (person.isPresent()) {
            Optional<Card> card = cardDao.getSingleCardByIdAndOwner(payerCardId, person.get().getId());
            if (card.isPresent() && cardValidator.validate(card.get(), cvc)) {
                logger.log(Level.TRACE, String.format("Try to perform payment (principal name: %s, payer's card " +
                        "id: %s, receiver's account id: %s, amount: %s)", principal.getName(), payerCardId,
                        receiverAccountId, amount));
                financialTransactionsPerformer.makePayment(amount, card.get().getAccountId(), receiverAccountId);
            } else {
                logger.log(Level.WARN, String.format("Cannot perform payment: card is not valid (principal " +
                        "name: %s, payer's card id: %s)", principal.getName(), payerCardId));
                throw new PaymentParamException("Cannot perform payment: the card is not valid");
            }
        }
    }

    private boolean checkParams(Object... params) {
        if (params == null) {
            return false;
        }
        for (Object p : params) {
            if (p == null ||
                    (p instanceof String && ((String) p).trim().length() == 0) ||
                    (p instanceof Integer && ((Integer) p) < 0) ||
                    (p instanceof BigInteger && ((BigInteger) p).compareTo(BigInteger.ZERO) < 0) ||
                    (p instanceof BigDecimal && ((BigDecimal) p).compareTo(BigDecimal.ZERO) < 0)
            ) {
                return false;
            }
        }
        return true;
    }
}
