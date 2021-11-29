package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.CustomSQLCode;
import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.PersonQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.util.PermissionType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.*;
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
    private static final String INVALID_PARAM_SQLSTATE = "22023";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
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
        return findSingleById(rawQuery, id);
    }

    @Override
    public Optional<Person> getSinglePersonById(PermissionType permission, BigInteger id) throws SQLException {
        if (permission == null) {
            return Optional.empty();
        }
        String rawQuery = queryConstructor.singleById(permission);
        return findSingleById(rawQuery, id);
    }

    @Override
    public Optional<Person> getSinglePersonByEmail(String email) throws SQLException {
        String rawQuery = queryConstructor.singleByEmail();
        return findSingleByEmail(rawQuery, email);
    }

    @Override
    public Optional<Person> getSinglePersonByEmail(PermissionType permission, String email) throws SQLException {
        if (permission == null) {
            return Optional.empty();
        }
        String rawQuery = queryConstructor.singleByEmail(permission);
        return findSingleByEmail(rawQuery, email);
    }

    @Override
    public List<Person> getAllPersons(PersonSortType sortType) throws SQLException {
        String rawQuery = queryConstructor.all();
        return findAll(String.format(rawQuery, orderStrategy.getOrder(sortType)));
    }

    @Override
    public List<Person> getAllPersons(PermissionType permission, PersonSortType sortType) throws SQLException {
        if (permission == null) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.all(permission);
        return findAll(String.format(rawQuery, orderStrategy.getOrder(sortType)));
    }

    @Override
    public List<Person> getPersonsPage(int limit, int offset, PersonSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page();
        return findPage(rawQuery, limit, offset, sortType);
    }

    @Override
    public List<Person> getPersonsPage(PermissionType permission, int limit, int offset, PersonSortType sortType)
            throws SQLException {
        if (permission == null || limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page(permission);
        return findPage(rawQuery, limit, offset, sortType);
    }

    @Override
    public int countPersons() throws SQLException {
        String query = queryConstructor.count();
        return retrievePersonsNumber(query);
    }

    @Override
    public int countPersons(PermissionType permission) throws SQLException {
        if (permission == null) {
            return 0;
        }
        String query = queryConstructor.count(permission);
        return retrievePersonsNumber(query);
    }

    @Override
    public Optional<StatusType> getPersonStatus(String email) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.status();
        String query = String.format(rawQuery, email);
        ResultSet rs = queryExecutor.execute(conn, query);
        StatusType statusType = null;
        if (rs != null && rs.next()) {
            try {
                statusType = StatusType.valueOf(rs.getString(1).toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            } finally {
                rs.getStatement().close();
            }
        }
        conn.close();
        return (statusType != null) ? Optional.of(statusType) : Optional.empty();
    }

    @Override
    public Optional<Integer> getPersonHash(String email) throws SQLException {
        Optional<Person> person = getSinglePersonByEmail(email);
        return person.map(Person::getHash);
    }

    @Override
    public synchronized boolean updatePerson(Person personDto) throws SQLException {
        String rawQuery = queryConstructor.update();
        try (Connection conn = dataSource.getConnection()) {
            return executeUpdatePerson(personDto, conn, rawQuery);
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), personDto.getId()), e);
            throw new SQLException("Cannot update person (incorrect parameters passed)", INVALID_PARAM_SQLSTATE,
                    CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode(), e);
        }
    }

    @Override
    public synchronized boolean updatePerson(PermissionType permission, Person personDto) throws SQLException {
        if (permission == null) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s - permission is null, cannot " +
                            "update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    (personDto != null) ? personDto.getId() : null));
            throw new SQLException("Cannot update person (permission is not specified)", INVALID_PARAM_SQLSTATE,
                    CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode());
        }
        try (Connection conn = dataSource.getConnection()) {
            String rawQuery = queryConstructor.update(permission);
            return executeUpdatePerson(personDto, conn, rawQuery);
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot update person (id %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName(), personDto.getId()), e);
            throw new SQLException("Cannot update person (incorrect parameters passed)", INVALID_PARAM_SQLSTATE,
                    CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode(), e);
        }
    }

    @Override
    public synchronized Person addPerson(Person personDto) throws SQLException {
        if (personDto == null) {
            logger.log(Level.WARN, String.format("Incorrect DTO passed to %s - personDto is null, cannot " +
                            "add person", Thread.currentThread().getStackTrace()[1].getMethodName()));
            throw new SQLException("Cannot add person (permission is not specified)", INVALID_PARAM_SQLSTATE,
                    CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode());
        }
        String rawQuery = queryConstructor.add();
        try (Connection conn = dataSource.getConnection()) {
            queryExecutor.executeUpdate(conn, rawQuery,
                    Objects.requireNonNull(personDto.getPermission(), "permission is null").getId(),
                    Objects.requireNonNull(personDto.getEmail(), "email is null").trim(),
                    Objects.requireNonNull(personDto.getPassword(), "password is null").trim(),
                    Objects.requireNonNull(personDto.getFirstName(), "first name is null").trim(),
                    Objects.requireNonNull(personDto.getLastName(), "last name is null").trim(),
                    Objects.requireNonNull(personDto.getStatus(), "statusType is null").getId(),
                    new Date(System.currentTimeMillis())
            );
        } catch (NullPointerException e) {
            logger.log(Level.WARN, String.format("Incorrect parameters passed to %s, cannot add person",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot add person (incorrect parameters passed)", INVALID_PARAM_SQLSTATE,
                    CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode(), e);
        }
        return getSinglePersonByEmail(personDto.getEmail()).orElse(null);
    }

    /**
     * Removes a person which has a specified id. The method is intended for use as a part of a transaction after
     * removing all the cards and accounts associated with the person to avoid database constraints.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id a person id (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
    @Override
    public synchronized boolean deletePersonById(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delSingleById();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    private Optional<Person> findSingleById(String rawQuery, BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrievePerson(conn, rs);
    }

    private Optional<Person> findSingleByEmail(String rawQuery, String email) throws SQLException {
        Connection conn = dataSource.getConnection();
        String query = String.format(rawQuery, email);
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrievePerson(conn, rs);
    }

    private Optional<Person> retrievePerson(Connection conn, ResultSet rs) throws SQLException {
        Person person = null;
        if (rs != null && rs.next()) {
            person = buildInstance(rs);
            rs.getStatement().close();
        }
        conn.close();
        return (person != null) ? Optional.of(person) : Optional.empty();
    }

    private List<Person> findAll(String query) throws SQLException {
        Connection conn = dataSource.getConnection();
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrievePersonList(conn, rs);
    }

    private List<Person> findPage(String rawQuery, int limit, int offset, PersonSortType sortType)
            throws SQLException {
        Connection conn = dataSource.getConnection();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, limit, offset);
        return retrievePersonList(conn, rs);
    }

    private List<Person> retrievePersonList(Connection conn, ResultSet rs) throws SQLException {
        List<Person> persons = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                persons.add(buildInstance(rs));
            }
            rs.getStatement().close();
        }
        conn.close();
        return persons;
    }

    private int retrievePersonsNumber(String query) throws SQLException {
        Connection conn = dataSource.getConnection();
        ResultSet rs = queryExecutor.execute(conn, query);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.getStatement().close();
        }
        conn.close();
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
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (method: %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create person instance (invalid field values were obtained from database)",
                    INVALID_PARAM_SQLSTATE, CustomSQLCode.INVALID_DB_PARAMETER.getCode(), e);
        }
    }

    private boolean executeUpdatePerson(Person personDto, Connection conn, String rawQuery) throws SQLException,
            NullPointerException {
        int result = queryExecutor.executeUpdate(conn, rawQuery,
                Objects.requireNonNull(personDto.getPermission(), "permission is null").getId(),
                personDto.getPassword(),
                Objects.requireNonNull(personDto.getFirstName(), "first name is null").trim(),
                Objects.requireNonNull(personDto.getLastName(), "last name is null").trim(),
                Objects.requireNonNull(personDto.getStatus(), "status type is null").getId(),
                Date.valueOf(personDto.getRegDate()),
                personDto.getId()
        );
        return (result != 0);
    }
}
