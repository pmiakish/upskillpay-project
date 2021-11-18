# UpSkillPay - payment service

> UpSkill Lab Java training web-project by P. Miakish

---

## General information

The web-application implements a payment system with the assignment of user roles. The system allows customers to make payments and other actions with their accounts and related cards, and allows administrators to manage customers.

## Project architecture

The project is a Java web-application based on the *MVC-pattern* and built using *Apache Maven* tools. 
The application is developed on the *Jakarta EE* platform (v.9.1.0), implements CDI, Servlet and JSP technologies.

### Data storage

- The application uses an in-memory relational database *HSQLDB* to store the data
- *JDBC API* is used for connection to the database
- Access to the database is maintained using a custom connection pool
- Interaction with the data storage in the application is carried out using the *DAO-pattern* and *Service layer*
- HTTP-session is used to store user information between requests 

### Access security

- The web-application uses the *Jakarta Security API* (form based HTTP authentication mechanism) for authentication and authorization 

### Content view

- The dynamic web content is being represented using *JSP technology*
- The *custom project Tag Library Descriptor* and several *custom JSP-tags* are implemented including **the pagination-tag** for long lists and **the sort-tag** representing column headers in tables as links which allows to sort rows in ascending and descending order

### Logging

- Logging is conducted using *Apache Log4j* utility

### Used patterns

- The application uses the behavioral design pattern '*Strategy*' and the factory method to produce suitable combinations of column names for result set ordering in generated SQL-queries

## Program features 

The application interface is internationalized and includes English and Russian localizations. 

All users are divided into three *roles*: customers, admins and superadmins. There are different permissions for each of these roles.

For each type of users, it is possible to log in and log out, edit the own profile. There is also a possibility to register for new customers. 

### Customer's interface

The customer's interface provides the ability to view accounts and related cards information, view incoming and outgoing payments history, allows to top up an account and perform payments to another account using one of account cards. Customers also can block or delete their accounts and cards.

### Admin's interface

The admin's interface provides the ability to view and edit customer's profiles, view payments history, block / unblock customers' accounts and cards.

### Superadmin's interface

Superadmins have all the admin's permissions. In additional, superadmins can manage admins' profiles, get system balance information and perform delete operations (with profiles, accounts and cards).