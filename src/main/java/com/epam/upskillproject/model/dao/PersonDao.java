package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dto.PermissionType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PersonDao {
    Optional<Person> getSinglePersonById(BigInteger id) throws SQLException;
    Optional<Person> getSinglePersonById(PermissionType permission, BigInteger id) throws SQLException;
    Optional<Person> getSinglePersonByEmail(String email) throws SQLException;
    Optional<Person> getSinglePersonByEmail(PermissionType permission, String email) throws SQLException;
    List<Person> getAllPersons(PersonSortType sortType) throws SQLException;
    List<Person> getAllPersons(PermissionType permission, PersonSortType sortType) throws SQLException;
    List<Person> getPersonsPage(int limit, int offset, PersonSortType sortType) throws SQLException;
    List<Person> getPersonsPage(PermissionType permission, int limit, int offset, PersonSortType sortType)
            throws SQLException;
    int countPersons() throws SQLException;
    int countPersons(PermissionType permission) throws SQLException;
    Optional<StatusType> getPersonStatus(String email) throws SQLException ;
    Optional<Integer> getPersonHash(String email) throws SQLException;
    boolean updatePerson(Person personDto) throws SQLException;
    boolean updatePerson(PermissionType permission, Person personDto) throws SQLException;
    Person addPerson(Person personDto) throws SQLException;
    boolean deletePersonById(Connection conn, BigInteger id) throws SQLException;
}