package com.epam.upskillproject.model.service;

import com.epam.upskillproject.model.dao.*;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.TransactionSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class AdminService {

    private static final Logger logger = LogManager.getLogger(AdminService.class.getName());

    private static final PersonSortType DEFAULT_PERSON_SORT_TYPE = PersonSortType.ID;
    private static final AccountSortType DEFAULT_ACCOUNT_SORT_TYPE = AccountSortType.ID;
    private static final CardSortType DEFAULT_CARD_SORT_TYPE = CardSortType.ID;
    private static final TransactionSortType DEFAULT_PAYMENT_SORT_TYPE = TransactionSortType.ID_DESC;
    private static final String EMAIL_PATTERN = "^\\w+@\\w+\\.\\w+$";

    private final PersonDao personDao;
    private final AccountDao accountDao;
    private final CardDao cardDao;
    private final PaymentDao paymentDao;

    @Inject
    public AdminService(PersonDao personDao, AccountDao accountDao, CardDao cardDao, PaymentDao paymentDao) {
        this.personDao = personDao;
        this.accountDao = accountDao;
        this.cardDao = cardDao;
        this.paymentDao = paymentDao;
    }

    /**
     * Creates a Page of customers
     * @param amount a number of customers in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType PersonSortType. If a passed value is null, will be used a default sort type
     * @return a Page of customers or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Person> getCustomers(int amount, int pageNumber, PersonSortType sortType) throws SQLException {
        if (!checkParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of customers (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_PERSON_SORT_TYPE;
        }
        PermissionType permission = PermissionType.CUSTOMER;
        int offset = amount * (pageNumber - 1);
        List<Person> entries = personDao.getPersonsPage(permission, amount, offset, sortType);
        int total = personDao.countPersons(permission);
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    /**
     * Finds and returns an instance of a customer by email or null when there are no related records
     * @param email a valid email value
     * @return a Person (customer's instance) | null - if it is not present or email string is not valid
     * @throws SQLException
     */
    public Person getCustomer(String email) throws SQLException {
        if (!checkParams(email) || !email.matches(EMAIL_PATTERN)) {
            logger.log(Level.WARN, String.format("Cannot get customer (invalid email parameter - %s)", email));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(PermissionType.CUSTOMER, email);
        return person.orElse(null);
    }

    /**
     * Finds and returns an instance of a customer by id or null when there are no related records
     * @param id a positive BigInteger
     * @return a Person (customer's instance) | null - if it is not present or id is not valid
     * @throws SQLException
     */
    public Person getCustomer(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get customer (invalid id parameter - %s)", id));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonById(PermissionType.CUSTOMER, id);
        return person.orElse(null);
    }

    /**
     * Updates customer's profile
     * @param id a positive BigInteger
     * @param newPermissionType might be null (in this case will be used old person's permission type)
     * @param newEmail an unique valid email
     * @param newPassword might be null (in this case will be used old person's password)
     * @param newFirstName not null String value
     * @param newLastName not null String value
     * @param statusType not null StatusType value
     * @param newRegDate not null LocalDate value
     * @return true if the specified customer's record was changed or false in other cases
     * @throws SQLException
     */
    public synchronized boolean updateCustomer(BigInteger id,
                                               PermissionType newPermissionType,
                                               String newEmail,
                                               String newPassword,
                                               String newFirstName,
                                               String newLastName,
                                               StatusType statusType,
                                               LocalDate newRegDate) throws SQLException {
        if (!checkParams(id, newEmail, newFirstName, newLastName, statusType, newRegDate)) {
            logger.log(Level.WARN, "Cannot update customer (bad parameters passed)");
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonById(PermissionType.CUSTOMER, id);
        if (person.isPresent()) {
            if (newPermissionType == null) {
                newPermissionType = person.get().getPermission();
            }
            if (newPassword == null) {
                newPassword = person.get().getPassword();
            }
            return personDao.updatePerson(PermissionType.CUSTOMER, id, newPermissionType, newEmail, newPassword,
                    newFirstName, newLastName, statusType, newRegDate);
        } else {
            logger.log(Level.WARN, String.format("Cannot update customer (person with id %s not found)", id));
            return false;
        }
    }

    /**
     * Creates a Page of accounts
     * @param amount a number of accounts in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType AccountSortType. If a passed value is null, will be used a default sort type
     * @return a Page of accounts or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Account> getAccounts(int amount, int pageNumber, AccountSortType sortType) throws SQLException {
        if (!checkParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of accounts (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_ACCOUNT_SORT_TYPE;
        }
        int offset = amount * (pageNumber - 1);
        List<Account> entries = accountDao.getAccountsPage(amount, offset, sortType);
        int total = accountDao.countAccounts();
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    /**
     * Collects and returns a list of customer's accounts
     * @param id a positive BigInteger (id of a customer)
     * @return a List of accounts or an empty List if specified customer or accounts are not found
     * @throws SQLException
     */
    public List<Account> getAccountsByOwner(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get accounts by owner (invalid id parameter - %s)", id));
            return new ArrayList<>();
        }
        return accountDao.getAccountsByOwner(id);
    }

    /**
     * Finds and returns id of an account by id of a card which related to this account
     * @param cardId a positive BigInteger
     * @return an account id if the specified account and the card are present or null in other cases
     * @throws SQLException
     */
    public BigInteger getAccountIdByCardId(BigInteger cardId) throws SQLException {
        if (!checkParams(cardId)) {
            logger.log(Level.WARN, String.format("Cannot get account id by card id (invalid cardId parameter - %s)",
                    cardId));
            return null;
        }
        Optional<BigInteger> accountId = cardDao.getCardAccountId(cardId);
        return accountId.orElse(null);
    }

    /**
     * Finds and returns an account status
     * @param id a positive BigInteger
     * @return an account status type if the specified account is present or null in other cases
     * @throws SQLException
     */
    public StatusType getAccountStatus(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get account status by id (invalid id parameter - %s)", id));
            return null;
        }
        Optional<StatusType> statusType = accountDao.getAccountStatus(id);
        return statusType.orElse(null);
    }
    /**
     * Updates an account status
     * @param id a positive BigInteger
     * @param statusType not null StatusType value
     * @return true if the specified account record was changed or false in other cases
     * @throws SQLException
     */
    public boolean updateAccountStatus(BigInteger id, StatusType statusType) throws SQLException {
        if (!checkParams(id, statusType)) {
            logger.log(Level.WARN, "Cannot update account status (invalid parameters passed)");
            return false;
        }
        if (statusType == StatusType.BLOCKED) {
            List<Card> accountCards = getCardsByAccount(id);
            for (Card card : accountCards) {
                updateCardStatus(card.getId(), StatusType.BLOCKED);
            }
        }
        return accountDao.updateAccountStatus(id, statusType);
    }

    /**
     * Creates a Page of cards
     * @param amount a number of cards in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType CardSortType. If a passed value is null, will be used a default sort type
     * @return a Page of cards or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Card> getCards(int amount, int pageNumber, CardSortType sortType) throws SQLException {
        if (!checkParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of cards (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_CARD_SORT_TYPE;
        }
        int offset = amount * (pageNumber - 1);
        List<Card> entries = cardDao.getCardsPage(amount, offset, sortType);
        int total = cardDao.countCards();
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    /**
     * Collects and returns a list of customer's cards
     * @param id a positive BigInteger (an id of a customer)
     * @return a List of cards or an empty List if specified customer or cards are not found
     * @throws SQLException
     */
    public List<Card> getCardsByOwner(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get cards by owner (invalid owner's id parameter - %s)", id));
            return new ArrayList<>();
        }
        return cardDao.getCardsByOwner(id);
    }

    /**
     * Collects and returns an account card list
     * @param id a positive BigInteger (an id of an account)
     * @return a List of cards or an empty List if specified account or cards are not found
     * @throws SQLException
     */
    public List<Card> getCardsByAccount(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get cards by account (invalid account id parameter - %s)", id));
            return new ArrayList<>();
        }
        return cardDao.getCardsByAccount(id);
    }

    /**
     * Finds and returns a card status
     * @param id a positive BigInteger
     * @return a card status type if the specified card is present or null in other cases
     * @throws SQLException
     */
    public StatusType getCardStatus(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get card status by id (invalid id parameter - %s)", id));
            return null;
        }
        Optional<StatusType> statusType = cardDao.getCardStatus(id);
        return statusType.orElse(null);
    }

    /**
     * Updates a card status
     * @param id a positive BigInteger
     * @param statusType not null StatusType value
     * @return true if the specified card record was changed or false in other cases
     * @throws SQLException
     */
    public boolean updateCardStatus(BigInteger id, StatusType statusType) throws SQLException {
        if (!checkParams(id, statusType)) {
            logger.log(Level.WARN, "Cannot update card status (invalid parameters passed)");
            return false;
        }
        return cardDao.updateCardStatus(id, statusType);
    }

    /**
     * Creates a Page of payments (transactions)
     * @param amount a number of payments in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType TransactionSortType. If a passed value is null, will be used a default sort type
     * @return a Page of payments or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Transaction> getPayments(int amount, int pageNumber, TransactionSortType sortType) throws SQLException {
        if (!checkParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of payments (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_PAYMENT_SORT_TYPE;
        }
        int offset = amount * (pageNumber - 1);
        List<Transaction> entries = paymentDao.getPaymentsPage(amount, offset, sortType);
        int total = paymentDao.countPayments();
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    private boolean checkParams(Object... params) {
        if (params == null) {
            logger.log(Level.WARN, String.format("Parameters array passed to %s is null",
                    Thread.currentThread().getStackTrace()[1].getMethodName()));
            return false;
        }
        for (Object p : params) {
            if (
                    p == null ||
                            (p instanceof String && ((String) p).trim().length() == 0) ||
                            (p instanceof Integer && ((Integer) p) < 0) ||
                            (p instanceof BigInteger && ((BigInteger) p).compareTo(BigInteger.ZERO) < 0)
            ) {
                logger.log(Level.WARN, String.format("At least one of the passed parameters to %s is not valid",
                        Thread.currentThread().getStackTrace()[1].getMethodName()));
                return false;
            }
        }
        return true;
    }
}
