package com.epam.upskillproject.model.dto;

import com.epam.upskillproject.util.RoleType;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

public class Person {
    private final BigInteger id;
    private final RoleType role;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final StatusType status;
    private final LocalDate regDate;
    private final int hash;

    public Person(BigInteger id, RoleType role, String email, String password, String firstName,
                  String lastName, StatusType statusType, LocalDate regDate) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = statusType;
        this.regDate = regDate;
        this.hash = hashCode();
    }

    public Person(BigInteger id, RoleType role, String email, String firstName, String lastName,
                  StatusType statusType, LocalDate regDate) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.password = null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = statusType;
        this.regDate = regDate;
        this.hash = hashCode();
    }

    public Person(RoleType role, String email, String password, String firstName, String lastName,
                  StatusType statusType) {
        this.id = null;
        this.role = role;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = statusType;
        this.regDate = null;
        this.hash = hashCode();
    }

    public BigInteger getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public StatusType getStatus() {
        return status;
    }

    public LocalDate getRegDate() {
        return regDate;
    }

    public RoleType getrole() {
        return role;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) &&
                role == person.role &&
                Objects.equals(email, person.email) &&
                Objects.equals(password, person.password) &&
                Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName) &&
                status == person.status &&
                Objects.equals(regDate, person.regDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, email, password, firstName, lastName, status, regDate);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + status +
                ", regDate=" + regDate +
                '}';
    }
}
