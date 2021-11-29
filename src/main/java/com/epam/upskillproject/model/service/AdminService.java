package com.epam.upskillproject.model.service;

import com.epam.upskillproject.model.dao.*;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PaymentSortType;
import com.epam.upskillproject.util.ParamsValidator;
import com.epam.upskillproject.util.PermissionType;
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
    private static final PaymentSortType DEFAULT_PAYMENT_SORT_TYPE = PaymentSortType.ID_DESC;

    private final PersonDao personDao;
    private final AccountDao accountDao;
    private final CardDao cardDao;
    private final PaymentDao paymentDao;
    private final ParamsValidator paramsValidator;

    @Inject
    public AdminService(PersonDao personDao, AccountDao accountDao, CardDao cardDao, PaymentDao paymentDao,
                        ParamsValidator paramsValidator) {
        this.personDao = personDao;
        this.accountDao = accountDao;
        this.cardDao = cardDao;
        this.paymentDao = paymentDao;
        this.paramsValidator = paramsValidator;
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
        if (!paramsValidator.validatePageParams(amount, pageNumber)) {
            logger.log(Level.WARN, String.format("Cannot get page of customers - invalid page parameters (amount: %d, " +
                    "pageNumber: %d)", amount, pageNumber));
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
        if (!paramsValidator.validateEmail(email)) {
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
        if (!paramsValidator.validateId(id)) {
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
     * @param email an unique valid email
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
                                               String email,
                                               String newPassword,
                                               String newFirstName,
                                               String newLastName,
                                               StatusType statusType,
                                               LocalDate newRegDate) throws SQLException {
        if (!paramsValidator.validatePersonUpdateParams(id, email, newPassword, newFirstName, newLastName, statusType,
                newRegDate)) {
            logger.log(Level.WARN, String.format("Cannot update customer (bad parameters passed) [id: %s]", id));
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
            Person personDto = new Person(id, newPermissionType, email, newPassword, newFirstName, newLastName,
                    statusType, newRegDate);
            return personDao.updatePerson(PermissionType.CUSTOMER, personDto);
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
        if (!paramsValidator.validatePageParams(amount, pageNumber)) {
            logger.log(Level.WARN, String.format("Cannot get page of accounts - invalid page parameters (amount: %d, " +
                    "pageNumber: %d)", amount, pageNumber));
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
        if (!paramsValidator.validateId(id)) {
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
        if (!paramsValidator.validateId(cardId)) {
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
        if (!paramsValidator.validateId(id)) {
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
    public synchronized boolean updateAccountStatus(BigInteger id, StatusType statusType) throws SQLException {
        if (statusType == null || !paramsValidator.validateId(id)) {
            logger.log(Level.WARN, String.format("Cannot update account status (invalid parameters passed) " +
                    "[target status: %s, id: %s]", statusType, id));
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
        if (!paramsValidator.validatePageParams(amount, pageNumber)) {
            logger.log(Level.WARN, String.format("Cannot get page of cards - invalid page parameters (amount: %d, " +
                    "pageNumber: %d)", amount, pageNumber));
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
        if (!paramsValidator.validateId(id)) {
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
        if (!paramsValidator.validateId(id)) {
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
        if (!paramsValidator.validateId(id)) {
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
    public synchronized boolean updateCardStatus(BigInteger id, StatusType statusType) throws SQLException {
        if (statusType == null || !paramsValidator.validateId(id)) {
            logger.log(Level.WARN, String.format("Cannot update card status (invalid parameters passed) " +
                    "[target status: %s, id: %s]", statusType, id));
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
    public Page<Payment> getPayments(int amount, int pageNumber, PaymentSortType sortType) throws SQLException {
        if (!paramsValidator.validatePageParams(amount, pageNumber)) {
            logger.log(Level.WARN, String.format("Cannot get page of payments - invalid page parameters (amount: %d, " +
                    "pageNumber: %d)", amount, pageNumber));
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_PAYMENT_SORT_TYPE;
        }
        int offset = amount * (pageNumber - 1);
        List<Payment> entries = paymentDao.getPaymentsPage(amount, offset, sortType);
        int total = paymentDao.countPayments();
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }
}
