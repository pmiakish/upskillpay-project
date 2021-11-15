package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.PersonQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.PermissionType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.sort.PersonSortType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class PersonDaoImpl implements PersonDao {

    private static final Logger logger = LogManager.getLogger(PersonDaoImpl.class.getName());

    private static final String ID_COLUMN_NAME = "ID";
    private static final String EMAIL_COLUMN_NAME = "EMAIL";
    private static final String PASSWORD_COLUMN_NAME = "PASSWORD";
    private static final String FIRSTNAME_COLUMN_NAME = "FIRSTNAME";
    private static final String LASTNAME_COLUMN_NAME = "LASTNAME";
    private static final String REGDATE_COLUMN_NAME = "REGDATE";
    private static final String PERMISSION_COLUMN_ALIAS = "prmName";
    private static final String STATUS_COLUMN_ALIAS = "statName";

    private final PersonQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;
    private final OrderStrategy orderStrategy;

    @Inject
    public PersonDaoImpl(PersonQueryConstructor queryConstructor,
                         QueryExecutor queryExecutor,
                         @Named("personOrder") OrderStrategy orderStrategy) {
        this.queryConstructor = queryConstructor;
        this.queryExecutor = queryExecutor;
        this.orderStrategy = orderStrategy;
    }

    @Override
    public Optional<Person> getSinglePersonById(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.singleById();
        return executeQuerySingleById(rawQuery, id);
    }

    @Override
    public Optional<Person> getSinglePersonById(PermissionType permission, BigInteger id) throws SQLException {
        if (permission == null) {
            return Optional.empty();
        }
        String rawQuery = queryConstructor.singleById(permission);
        return executeQuerySingleById(rawQuery, id);
    }

    @Override
    public Optional<Person> getSinglePersonByEmail(String email) throws SQLException {
        String rawQuery = queryConstructor.singleByEmail();
        return executeQuerySingleByEmail(rawQuery, email);
    }

    @Override
    public Optional<Person> getSinglePersonByEmail(PermissionType permission, String email) throws SQLException {
        if (permission == null) {
            return Optional.empty();
        }
        String rawQuery = queryConstructor.singleByEmail(permission);
        return executeQuerySingleByEmail(rawQuery, email);
    }

    @Override
    public List<Person> getAllPersons(PersonSortType sortType) throws SQLException {
        String rawQuery = queryConstructor.all();
        return executeQueryAll(String.format(rawQuery, orderStrategy.getOrder(sortType)));
    }

    @Override
    public List<Person> getAllPersons(PermissionType permission, PersonSortType sortType) throws SQLException {
        if (permission == null) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.all(permission);
        return executeQueryAll(String.format(rawQuery, orderStrategy.getOrder(sortType)));
    }

    @Override
    public List<Person> getPersonsPage(int limit, int offset, PersonSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page();
        return executeQueryPage(rawQuery, limit, offset, sortType);
    }

    @Override
    public List<Person> getPersonsPage(PermissionType permission, int limit, int offset, PersonSortType sortType)
            throws SQLException {
        if (permission == null || limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page(permission);
        return executeQueryPage(rawQuery, limit, offset, sortType);
    }

    @Override
    public int countPersons() throws SQLException {
        String query = queryConstructor.count();
        return executeQueryCount(query);
    }

    @Override
    public int countPersons(PermissionType permission) throws SQLException {
        if (permission == null) {
            return 0;
        }
        String query = queryConstructor.count(permission);
        return executeQueryCount(query);
    }

    @Override
    public Optional<StatusType> getPersonStatus(String email) throws SQLException {
        String rawQuery = queryConstructor.status();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, email));
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
    public Optional<Integer> getPersonHash(String email) throws SQLException {
        Optional<Person> person = getSinglePersonByEmail(email);
        return person.map(Person::getHash);
    }

    @Override
    public synchronized boolean updatePerson(BigInteger id,
                                             PermissionType newPermission,
                                             String newEmail,
                                             String newPassword,
                                             String newFirstName,
                                             String newLastName,
                                             StatusType newStatusType,
                                             LocalDate newRegDate) throws SQLException {
        String rawQuery = queryConstructor.update();
        try {
            int result = queryExecutor.executeUpdate(
                    rawQuery,
                    Objects.requireNonNull(newPermission, "newPermission is null").getId(),
                    Objects.requireNonNull(newEmail, "newPermission is null").trim(),
                    newPassword,
                    Objects.requireNonNull(newFirstName, "newFirstName is null").trim(),
                    Objects.requireNonNull(newLastName, "newLastName is null").trim(),
                    Objects.requireNonNull(newStatusType, "newStatusType is null").getId(),
                    Date.valueOf(newRegDate),
                    id
            );
            return (result != 0);
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), id), e);
            throw new SQLException("Cannot update person (incorrect parameters passed)", e);
        }
    }

    @Override
    public synchronized boolean updatePerson(PermissionType permission,
                                             BigInteger id,
                                             PermissionType newPermission,
                                             String newEmail,
                                             String newPassword,
                                             String newFirstName,
                                             String newLastName,
                                             StatusType newStatusType,
                                             LocalDate newRegDate) throws SQLException {
        if (permission == null) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s - permission is null, cannot " +
                            "update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), id));
            throw new SQLException("Cannot update person (permission is not specified)");
        }
        String rawQuery = queryConstructor.update(permission);
        try {
            int result = queryExecutor.executeUpdate(
                    rawQuery,
                    id,
                    Objects.requireNonNull(newPermission, "newPermission is null").getId(),
                    Objects.requireNonNull(newEmail, "newEmail is null").trim(),
                    newPassword,
                    Objects.requireNonNull(newFirstName, "newFirstName is null").trim(),
                    Objects.requireNonNull(newLastName, "newLastName is null").trim(),
                    Objects.requireNonNull(newStatusType, "newStatusType is null").getId(),
                    Date.valueOf(newRegDate)
            );
            return (result != 0);
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), id), e);
            throw new SQLException("Cannot update person (incorrect parameters passed)", e);
        }
    }

    @Override
    public synchronized Person addPerson(PermissionType permission,
                                         String email,
                                         String password,
                                         String firstName,
                                         String lastName,
                                         StatusType statusType) throws SQLException {
        String rawQuery = queryConstructor.add();
        try {
            queryExecutor.executeUpdate(
                    rawQuery,
                    Objects.requireNonNull(permission, "permission is null").getId(),
                    Objects.requireNonNull(email, "email is null").trim(),
                    password,
                    Objects.requireNonNull(firstName, "firstName is null").trim(),
                    Objects.requireNonNull(lastName, "lastName is null").trim(),
                    Objects.requireNonNull(statusType, "statusType is null").getId(),
                    new Date(System.currentTimeMillis())
            );
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot add person",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot add person (incorrect parameters passed)", e);
        }
        return getSinglePersonByEmail(email).orElse(null);
    }

    // should use the method as a transaction part after removing all the cards and accounts of the person
    @Override
    public synchronized boolean deletePersonById(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delSingleById();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    private Optional<Person> executeQuerySingleById(String rawQuery, BigInteger id) throws SQLException {
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        Person person = null;
        if (rs != null && rs.next()) {
            person = buildInstance(rs);
            rs.close();
        }
        return (person != null) ? Optional.of(person) : Optional.empty();
    }

    private Optional<Person> executeQuerySingleByEmail(String rawQuery, String email) throws SQLException {
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, email));
        Person person = null;
        if (rs != null && rs.next()) {
            person = buildInstance(rs);
            rs.close();
        }
        return (person != null) ? Optional.of(person) : Optional.empty();
    }

    private List<Person> executeQueryAll(String query) throws SQLException {
        ResultSet rs = queryExecutor.execute(query);
        List<Person> persons = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                persons.add(buildInstance(rs));
            }
            rs.close();
        }
        return persons;
    }

    private List<Person> executeQueryPage(String rawQuery, int limit, int offset, PersonSortType sortType)
            throws SQLException {
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)), limit, offset);
        List<Person> persons = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                persons.add(buildInstance(rs));
            }
            rs.close();
        }
        return persons;
    }

    private int executeQueryCount(String query) throws SQLException {
        ResultSet rs = queryExecutor.execute(query);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    private Person buildInstance(ResultSet rs) throws SQLException {
        try {
            return new Person(
                    new BigInteger(rs.getString(ID_COLUMN_NAME)),
                    PermissionType.valueOf(rs.getString(PERMISSION_COLUMN_ALIAS)),
                    rs.getString(EMAIL_COLUMN_NAME),
                    rs.getString(PASSWORD_COLUMN_NAME),
                    rs.getString(FIRSTNAME_COLUMN_NAME),
                    rs.getString(LASTNAME_COLUMN_NAME),
                    StatusType.valueOf(rs.getString(STATUS_COLUMN_ALIAS)),
                    rs.getDate(REGDATE_COLUMN_NAME).toLocalDate()
            );
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create person instance (invalid field values were obtained from database)", e);
        }
    }
}
