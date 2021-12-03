package com.epam.upskillproject.model.service;

import com.epam.upskillproject.exception.TransactionException;
import com.epam.upskillproject.model.dao.CardDao;
import com.epam.upskillproject.model.dao.queryhandler.FinancialTransactionsPerformer;
import com.epam.upskillproject.model.dao.IncomeDao;
import com.epam.upskillproject.model.dao.PersonDao;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PersonSortType;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Singleton
public class SuperadminService {

    private static final Logger logger = LogManager.getLogger(SuperadminService.class.getName());

    private static final PersonSortType DEFAULT_PERSON_SORT_TYPE = PersonSortType.ID;

    private final PersonDao personDao;
    private final CardDao cardDao;
    private final IncomeDao incomeDao;
    private final ParamsValidator paramsValidator;
    private final FinancialTransactionsPerformer financialTransactionsPerformer;

    @Inject
    public SuperadminService(PersonDao personDao, CardDao cardDao, IncomeDao incomeDao, ParamsValidator paramsValidator,
                             FinancialTransactionsPerformer financialTransactionsPerformer) {
        this.personDao = personDao;
        this.cardDao = cardDao;
        this.incomeDao = incomeDao;
        this.paramsValidator = paramsValidator;
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
        if (!paramsValidator.validatePageParams(amount, pageNumber)) {
            logger.log(Level.WARN, "Cannot get page of admins (invalid page parameters)");
            return null;
        }
        if (sortType == null) {
            sortType = DEFAULT_PERSON_SORT_TYPE;
        }
        RoleType role = RoleType.ADMIN;
        int offset = amount * (pageNumber - 1);
        List<Person> entries = personDao.getPersonsPage(role, amount, offset, sortType);
        int total = personDao.countPersons(role);
        return new Page<>(entries, pageNumber, amount, total, sortType);
    }

    /**
     * Finds and returns an instance of an admin by email or null when there are no related records
     * @param email a valid email value
     * @return a Person (admin's instance) | null - if it is not present or email string is not valid
     * @throws SQLException
     */
    public Person getAdmin(String email) throws SQLException {
        if (!paramsValidator.validateEmail(email)) {
            logger.log(Level.WARN, String.format("Cannot get admin (invalid email parameter - %s)", email));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonByEmail(RoleType.ADMIN, email);
        return person.orElse(null);
    }

    /**
     * Finds and returns an instance of an admin by id or null when there are no related records
     * @param id a positive BigInteger
     * @return a Person (admin's instance) | null - if it is not present or id is not valid
     * @throws SQLException
     */
    public Person getAdmin(BigInteger id) throws SQLException {
        if (!paramsValidator.validateId(id)) {
            logger.log(Level.WARN, String.format("Cannot get admin (invalid id parameter - %s)", id));
            return null;
        }
        Optional<Person> person = personDao.getSinglePersonById(RoleType.ADMIN, id);
        return person.orElse(null);
    }

    /**
     * Updates admin's profile
     * @param id a positive BigInteger
     * @param newRoleType might be null (in this case will be used old person's role type)
     * @param email an unique valid email
     * @param newPassword might be null (in this case will be used old person's password)
     * @param newFirstName not null String value
     * @param newLastName not null String value
     * @param statusType not null StatusType value
     * @param newRegDate not null LocalDate value
     * @return true if the specified admin's record was changed or false in other cases
     * @throws SQLException
     */
    public synchronized boolean updateAdmin(BigInteger id,
                                            RoleType newRoleType,
                                            String email,
                                            String newPassword,
                                            String newFirstName,
                                            String newLastName,
                                            StatusType statusType,
                                            LocalDate newRegDate) throws SQLException {
        if (!paramsValidator.validatePersonUpdateParams(id, email, newPassword, newFirstName, newLastName, statusType,
                newRegDate)) {
            logger.log(Level.WARN, String.format("Cannot update admin (bad parameters passed) [id: %s]", id));
            return false;
        }
        Optional<Person> person = personDao.getSinglePersonById(RoleType.ADMIN, id);
        if (person.isPresent()) {
            if (newRoleType == null) {
                newRoleType = person.get().getrole();
            }
            if (newPassword == null) {
                newPassword = person.get().getPassword();
            }
            Person personDto = new Person(id, newRoleType, email, newPassword, newFirstName, newLastName,
                    statusType, newRegDate);
            return personDao.updatePerson(RoleType.ADMIN, personDto);
        } else {
            logger.log(Level.WARN, String.format("Cannot update admin (person with id %s not found)", id));
            return false;
        }
    }

    /**
     * Removes the specified person. All the related accounts and cards will be also removed
     * @param id a positive BigInteger
     * @return true if a person was deleted, otherwise false
     * @throws TransactionException
     */
    public boolean deletePerson(BigInteger id) throws TransactionException {
        if (!paramsValidator.validateId(id)) {
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
     * @throws TransactionException
     */
    public boolean deleteAccount(BigInteger id) throws TransactionException {
        if (!paramsValidator.validateId(id)) {
            logger.log(Level.WARN, String.format("Cannot delete account (bad id parameter passed: %s)", id));
            return false;
        }
        return financialTransactionsPerformer.deleteAccount(id);
    }

    /**
     * Removes the specified card
     * @param id a positive BigInteger
     * @return true if a card was deleted, otherwise false
     * @throws SQLException
     */
    public boolean deleteCard(BigInteger id) throws SQLException {
        if (!paramsValidator.validateId(id)) {
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
}
