package com.epam.upskillproject.model.service;

import com.epam.upskillproject.exception.TransactionException;
import com.epam.upskillproject.model.dao.AccountDao;
import com.epam.upskillproject.model.dao.queryhandler.FinancialTransactionsPerformer;
import com.epam.upskillproject.model.dao.PersonDao;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.util.ParamsValidator;
import com.epam.upskillproject.util.RoleType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class SystemService {

    private static final Logger logger = LogManager.getLogger(SystemService.class.getName());

    private static final BigInteger SYSTEM_INCOME_ID = BigInteger.ZERO;
    private static final BigDecimal INITIAL_BALANCE_AMOUNT = BigDecimal.valueOf(50.00);

    private final PersonDao personDao;
    private final AccountDao accountDao;
    private final ParamsValidator paramsValidator;
    private final FinancialTransactionsPerformer financialTransactionsPerformer;

    @Inject
    public SystemService(PersonDao personDao, AccountDao accountDao, ParamsValidator paramsValidator,
                         FinancialTransactionsPerformer financialTransactionsPerformer) {
        this.personDao = personDao;
        this.accountDao = accountDao;
        this.paramsValidator = paramsValidator;
        this.financialTransactionsPerformer = financialTransactionsPerformer;
    }

    /**
     * Finds and returns an instance of a person by email or null when there are no related records
     * @param email a valid email value
     * @return a Person | null - if it is not present or email string is not valid
     * @throws SQLException
     */
    public Person getPerson(String email) throws SQLException {
        if (!paramsValidator.validateEmail(email)) {
            logger.log(Level.WARN, String.format("Cannot get person (invalid email parameter - %s)", email));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(email);
        return person.orElse(null);
    }

    /**
     * Creates new customer
     * @param email a valid email value
     * @param password not null string (hashed password)
     * @param firstName not null string
     * @param lastName not null string
     * @return true if new person was created, otherwise false
     * @throws SQLException
     * @throws TransactionException exception might be thrown by FinancialTransactionsPerformer instance
     */
    public synchronized boolean addCustomer(String email, String password, String firstName, String lastName) throws
            SQLException, TransactionException {
        if (!paramsValidator.validatePersonAddParams(email, password, firstName, lastName)) {
            logger.log(Level.WARN, String.format("Cannot add new customer (invalid parameters) [email: %s, password " +
                    "not null: %s, name: %s %s]", email, (password != null), firstName, lastName));
            return false;
        }
        Person personDto = new Person(RoleType.CUSTOMER, email, password, firstName, lastName, StatusType.ACTIVE);
        Person createdCustomer = personDao.addPerson(personDto);
        if (createdCustomer != null) {
            logger.log(Level.INFO, String.format("New customer created (id: %s, email: %s)", createdCustomer.getId(), email));
            Account accountDto = new Account(createdCustomer.getId(), BigDecimal.ZERO, StatusType.ACTIVE);
            Account createdAccount = accountDao.addAccount(accountDto);
            if (createdAccount != null) {
                logger.log(Level.INFO, String.format("Set new account initial balance (user id: %s, account id: %s, " +
                        "balance: %s)", createdCustomer.getId(), createdAccount.getId(), INITIAL_BALANCE_AMOUNT));
                financialTransactionsPerformer.makePayment(INITIAL_BALANCE_AMOUNT, SYSTEM_INCOME_ID, createdAccount.getId());
            } else {
                logger.log(Level.WARN, String.format("An account of a new customer was not created (user id: %s)",
                        createdCustomer.getId()));
            }
            return true;
        } else {
            logger.log(Level.WARN, String.format("Cannot add new customer (returned null from DAO) [email: %s, " +
                    "password not null: %s, name %s %s]", email, (password != null), firstName, lastName));
            return false;
        }
    }

    /**
     * Checks if a status of a person is active
     * @param email a valid email value
     * @return true if a person status is present and active, otherwise false
     * @throws SQLException
     */
    public boolean checkActiveStatus(String email) throws SQLException {
        if (!paramsValidator.validateEmail(email)) {
            logger.log(Level.WARN, String.format("Cannot check person's status (invalid email parameter) [email: %s]",
                    email));
            return false;
        }
        Optional<StatusType> statusType = personDao.getPersonStatus(email);
        return statusType.filter(type -> (type == StatusType.ACTIVE)).isPresent();
    }

    /**
     * Checks if a person's hash is present and is not changed (passed value and db-stored value are equal)
     * @param email a valid email value
     * @param hash person's hash
     * @return true if a person's hash is present and is not changed, otherwise false
     * @throws SQLException
     */
    public boolean checkPersonHash(String email, int hash) throws SQLException {
        if (!paramsValidator.validateEmail(email)) {
            logger.log(Level.WARN, String.format("Cannot check person's hash (invalid email parameter) [email: %s]",
                    email));
            return false;
        }
        Integer dbPersonHash = personDao.getPersonHash(email).orElse(null);
        return (dbPersonHash != null && dbPersonHash == hash);
    }

    /**
     * Updates a user's record (with any role type)
     * @param email a valid email value
     * @param newPassword not null string (hashed password)
     * @param newFirstName not null string
     * @param newLastName not null string
     * @return true if a user's record was updated, otherwise false
     * @throws SQLException
     */
    public synchronized boolean updateUser(String email, String newPassword, String newFirstName, String newLastName)
            throws SQLException {
        if (!paramsValidator.validatePersonAddParams(email, newPassword, newFirstName, newLastName)) {
            logger.log(Level.WARN, String.format("Cannot update user's record (invalid parameters) [email: %s, " +
                    "password not null: %s, name: %s %s]", email, (newPassword != null), newFirstName, newLastName));
            return false;
        }
        Person person = personDao.getSinglePersonByEmail(email).orElse(null);
        if (person != null) {
            if (newPassword == null) {
                newPassword = person.getPassword();
            }
            logger.log(Level.INFO, String.format("Try to update user's record [email: %s]", email));
            Person personDto = new Person(person.getId(), person.getrole(), person.getEmail(), newPassword,
                    newFirstName, newLastName, person.getStatus(), person.getRegDate());
            return personDao.updatePerson(personDto);
        } else {
            logger.log(Level.WARN, String.format("Cannot update user's record (related person not found) [email: %s]",
                    email));
            return false;
        }
    }

}
