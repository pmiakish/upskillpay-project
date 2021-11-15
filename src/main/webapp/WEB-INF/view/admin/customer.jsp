<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - customer's profile management" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Customer's profile - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Customer's profile</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Admin:</strong><br />
                            <a href="/profile" title="Edit profile">${user.email}</a><br />
                            ${user.firstName} ${user.lastName}<br />
                            <div class="d-grid gap-1 col-6 mx-auto">
                                <a href="/logout" class="btn btn-outline-dark btn-sm" role="button">Logout</a>
                            </div>
                        </p>
                    </c:if>
                </div>
            </div>
        </div>
        <div class="container">
            <nav class="navbar navbar-expand-lg navbar-light bg-light">
                <div class="container-fluid">
                    <a class="navbar-brand" href="/">UpSkillPay</a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#adminNavbar"
                            aria-controls="adminNavbar" aria-expanded="false" aria-label="Toggle navigation">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="adminNavbar">
                        <ul class="navbar-nav">
                            <li class="nav-item">
                                <a class="nav-link" href="/customers">Customers</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/accounts">Accounts</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/cards">Cards</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/payments">Payments</a>
                            </li>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="/admins">Admins</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="/income">Income</a>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <%-- Operation status message --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" />
            <br />
            <c:if test="${customer != null}">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="/customer/${customer.id}">Customer's profile</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/customer/accounts/${customer.id}">Customer's accounts</a>
                    </li>
                </ul>
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">email</th>
                        <th scope="col">First Name</th>
                        <th scope="col">Last Name</th>
                        <th scope="col">Registration</th>
                        <th scope="col">Status</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th scope="row">${customer.id}</th>
                            <td>${customer.email}</td>
                            <td>${customer.firstName}</td>
                            <td>${customer.lastName}</td>
                            <td>${customer.regDate}</td>
                            <td<c:if test="${customer.status == 'BLOCKED'}">  class="table-danger"</c:if>>${customer.status}</td>
                        </tr>
                    </tbody>
                </table>
                <br />
                <p><h3>Edit customer's profile:</h3></p>
                <br />
                <form method="POST" action="/customer/${customer.id}" class="row g-3 needs-validation" novalidate
                      oninput='cPass.setCustomValidity(cPass.value != pass.value ? "Passwords do not match" : "")'>
                    <input type="hidden" id="inputHash" value="${customer.hash}" name="hash" />
                    <div class="mb-3 row">
                        <label for="staticID" class="col-sm-2 col-form-label"><strong>ID</strong></label>
                        <div class="col-sm-10">
                            <input type="text" readonly class="form-control-plaintext" id="staticID" value="${customer.id}" name="id" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="selectPermission" class="col-sm-2 col-form-label"><strong>Permission</strong></label>
                        <div class="col-sm-10">
                            <select class="form-select" id="selectPermission"
                                    name="permission"<c:if test="${user == null || user.permission != 'SUPERADMIN'}"> disabled</c:if>>
                                <option value="CUSTOMER" selected>CUSTOMER</option>
                                <option value="ADMIN">ADMIN</option>
                                <option value="SUPERADMIN">SUPERADMIN</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputEmail" class="col-sm-2 col-form-label"><strong>email</strong></label>
                        <div class="col-sm-10">
                            <input type="email" class="form-control" id="inputEmail" required value="${customer.email}"
                                   name="email" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputPassword" class="col-sm-2 col-form-label"><strong>New password</strong></label>
                        <div class="col-sm-10">
                            <input type="password" class="form-control" id="inputPassword" name="pass" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="confirmPassword" class="col-sm-2 col-form-label"><strong>Confirm password</strong></label>
                        <div class="col-sm-10">
                            <input type="password" class="form-control" id="confirmPassword" name="cPass" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputFirstName" class="col-sm-2 col-form-label"><strong>First Name</strong></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputFirstName" pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+"
                                   required value="${customer.firstName}" name="firstName" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputLastName" class="col-sm-2 col-form-label"><strong>Last Name</strong></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputLastName" pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+"
                                   required value="${customer.lastName}" name="lastName" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputRegDate" class="col-sm-2 col-form-label"><strong>Registration</strong></label>
                        <div class="col-sm-10">
                            <input type="date" class="form-control" id="inputRegDate" required value="${customer.regDate}"
                                   name="regDate" />
                        </div>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" id="activation"
                               name="active"<c:if test="${customer.status == 'ACTIVE'}"> checked</c:if> />
                        <label class="form-check-label" for="activation">Activate customer</label>
                    </div>
                    <br />

                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">Update</button>
                    </div>
                </form>
                <br />
                <c:if test="${user.permission == 'SUPERADMIN'}">
                    <form method="POST" action="/customer/${customer.id}" onsubmit="return confirm('Are you sure you ' +
                     'want to delete profile? This action cannot be canceled!');">
                        <input type="hidden" id="delete" value="true" name="delete" />
                        <input type="hidden" id="delCustomer" value="${customer.id}" name="id" />
                        <div class="col-12">
                            <button type="submit" class="btn btn-danger">Delete customer</button>
                        </div>
                    </form>
                    <br />
                </c:if>
            </c:if>
            <c:if test="${customer == null}">
                <div class="alert alert-danger" role="alert">
                    Customer with specified id not found
                </div>
                <p><a href="/customers">&#9665; back to customers list</a></p>
            </c:if>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" crossorigin="anonymous"></script>
        <script>
            (function () {
                'use strict'
                var forms = document.querySelectorAll('.needs-validation')
                Array.prototype.slice.call(forms)
                    .forEach(function (form) {
                        form.addEventListener('submit', function (event) {
                            if (!form.checkValidity()) {
                                event.preventDefault()
                                event.stopPropagation()
                            }

                            form.classList.add('was-validated')
                        }, false)
                    })
            })()
        </script>
    </body>
</html>