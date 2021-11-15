package com.epam.upskillproject.model.dto;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

public class Person {
    private final BigInteger id;
    private final PermissionType permission;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final StatusType status;
    private final LocalDate regDate;
    private final int hash;

    public Person(BigInteger id, PermissionType permission, String email, String password, String firstName,
                  String lastName, StatusType statusType, LocalDate regDate) {
        this.id = id;
        this.permission = permission;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = statusType;
        this.regDate = regDate;
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

    public PermissionType getPermission() {
        return permission;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id.equals(person.id) && permission == person.permission && email.equals(person.email) &&
                password.equals(person.password) && firstName.equals(person.firstName) &&
                lastName.equals(person.lastName) && status == person.status && regDate.equals(person.regDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, permission, email, password, firstName, lastName, status, regDate);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", permission=" + permission +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + status +
                ", regDate=" + regDate +
                '}';
    }
}
