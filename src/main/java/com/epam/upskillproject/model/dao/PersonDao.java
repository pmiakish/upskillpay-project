package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PersonSortType;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PersonDao {
    Optional<Person> getSinglePersonById(BigInteger id) throws SQLException;
    Optional<Person> getSinglePersonById(RoleType role, BigInteger id) throws SQLException;
    Optional<Person> getSinglePersonByEmail(String email) throws SQLException;
    Optional<Person> getSinglePersonByEmail(RoleType role, String email) throws SQLException;
    List<Person> getAllPersons(PersonSortType sortType) throws SQLException;
    List<Person> getAllPersons(RoleType role, PersonSortType sortType) throws SQLException;
    List<Person> getPersonsPage(int limit, int offset, PersonSortType sortType) throws SQLException;
    List<Person> getPersonsPage(RoleType role, int limit, int offset, PersonSortType sortType)
            throws SQLException;
    int countPersons() throws SQLException;
    int countPersons(RoleType role) throws SQLException;
    Optional<StatusType> getPersonStatus(String email) throws SQLException ;
    Optional<Integer> getPersonHash(String email) throws SQLException;
    boolean updatePerson(Person personDto) throws SQLException;
    boolean updatePerson(RoleType role, Person personDto) throws SQLException;
    Person addPerson(Person personDto) throws SQLException;
    boolean deletePersonById(Connection conn, BigInteger id) throws SQLException;
}