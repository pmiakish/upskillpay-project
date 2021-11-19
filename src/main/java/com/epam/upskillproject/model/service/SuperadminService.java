package com.epam.upskillproject.model.service;

import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.model.dao.CardDao;
import com.epam.upskillproject.model.dao.queryhandlers.FinancialTransactionsPerformer;
import com.epam.upskillproject.model.dao.IncomeDao;
import com.epam.upskillproject.model.dao.PersonDao;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Singleton
public class SuperadminService {

    private static final Logger logger = LogManager.getLogger(SuperadminService.class.getName());

    private static final PersonSortType DEFAULT_PERSON_SORT_TYPE = PersonSortType.ID;
    private static final String EMAIL_PATTERN = "^\\w+@\\w+\\.\\w+$";

    private final PersonDao personDao;
    private final CardDao cardDao;
    private final IncomeDao incomeDao;
    private final FinancialTransactionsPerformer financialTransactionsPerformer;

    @Inject
    public SuperadminService(PersonDao personDao, CardDao cardDao, IncomeDao incomeDao,
                             FinancialTransactionsPerformer financialTransactionsPerformer) {
        this.personDao = personDao;
        this.cardDao = cardDao;
        this.incomeDao = incomeDao;
        this.financialTransactionsPerformer = financialTransactionsPerformer;
    }

    /**
     * Creates a Page of admins
     * @param amount a number of admins in a returning Page (a positive integer)
     * @param pageNumber a positive integer
     * @param sortType PersonSortType. If a passed value is null, will be used a default sort type
     * @return a Page of admins or null if parameters are invalid
     * @throws SQLException
     */
    public Page<Person> getAdmins(int amount, int pageNumber, PersonSortType sortType) throws SQLException {
        if (!checkParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of admins (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_PERSON_SORT_TYPE;
        }
        PermissionType permission = PermissionType.ADMIN;
        int offset = amount * (pageNumber - 1);
        List<Person> entries = personDao.getPersonsPage(permission, amount, offset, sortType);
        int total = personDao.countPersons(permission);
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    /**
     * Finds and returns an instance of an admin by email or null when there are no related records
     * @param email a valid email value
     * @return a Person (admin's instance) | null - if it is not present or email string is not valid
     * @throws SQLException
     */
    public Person getAdmin(String email) throws SQLException {
        if (!checkParams(email) || !email.matches(EMAIL_PATTERN)) {
            logger.log(Level.WARN, String.format("Cannot get admin (invalid email parameter - %s)", email));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(PermissionType.ADMIN, email);
        return person.orElse(null);
    }

    /**
     * Finds and returns an instance of an admin by id or null when there are no related records
     * @param id a positive BigInteger
     * @return a Person (admin's instance) | null - if it is not present or id is not valid
     * @throws SQLException
     */
    public Person getAdmin(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot get admin (invalid id parameter - %s)", id));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonById(PermissionType.ADMIN, id);
        return person.orElse(null);
    }

    /**
     * Updates admin's profile
     * @param id a positive BigInteger
     * @param newPermissionType might be null (in this case will be used old person's permission type)
     * @param newEmail an unique valid email
     * @param newPassword might be null (in this case will be used old person's password)
     * @param newFirstName not null String value
     * @param newLastName not null String value
     * @param statusType not null StatusType value
     * @param newRegDate not null LocalDate value
     * @return true if the specified admin's record was changed or false in other cases
     * @throws SQLException
     */
    public synchronized boolean updateAdmin(BigInteger id,
                                            PermissionType newPermissionType,
                                            String newEmail,
                                            String newPassword,
                                            String newFirstName,
                                            String newLastName,
                                            StatusType statusType,
                                            LocalDate newRegDate) throws SQLException {
        if (!checkParams(id, newEmail, newFirstName, newLastName, statusType, newRegDate)) {
            logger.log(Level.WARN, "Cannot update admin (bad parameters passed)");
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonById(PermissionType.ADMIN, id);
        if (person.isPresent()) {
            if (newPermissionType == null) {
                newPermissionType = person.get().getPermission();
            }
            if (newPassword == null) {
                newPassword = person.get().getPassword();
            }
            return personDao.updatePerson(PermissionType.ADMIN, id, newPermissionType, newEmail, newPassword,
                    newFirstName, newLastName, statusType, newRegDate);
        } else {
            logger.log(Level.WARN, String.format("Cannot update admin (person with id %s not found)", id));
            return false;
        }
    }

    /**
     * Removes the specified person. All the related accounts and cards will be also removed
     * @param id a positive BigInteger
     * @return true if a person was deleted, otherwise false
     */
    public boolean deletePerson(BigInteger id) throws TransactionException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot delete person (bad id parameter passed: %s)", id));
            return false;
        }
        financialTransactionsPerformer.deletePerson(id);
        try {
            return personDao.getSinglePersonById(id).isEmpty();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Removes the specified account. All the related cards will be also removed
     * @param id a positive BigInteger
     * @return true if an account was deleted, otherwise false
     */
    public boolean deleteAccount(BigInteger id) throws TransactionException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot delete account (bad id parameter passed: %s)", id));
            return false;
        }
        return financialTransactionsPerformer.deleteAccount(id);
    }

    /**
     * Removes the specified card
     * @param id a positive BigInteger
     * @return true if a card was deleted, otherwise false
     */
    public boolean deleteCard(BigInteger id) throws SQLException {
        if (!checkParams(id)) {
            logger.log(Level.WARN, String.format("Cannot delete card (bad id parameter passed: %s)", id));
            return false;
        }
        return cardDao.deleteCardById(id);
    }

    /**
     * Allows get a rest of system income
     * @return a system income balance
     * @throws SQLException
     */
    public BigDecimal getIncomeBalance() throws SQLException {
        return incomeDao.getBalance();
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
