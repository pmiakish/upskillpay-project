<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - customer's accounts management" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Customer's accounts - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Customer's accounts</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <br /><br /><strong>Admin:</strong><br />
                        <a href="/profile" title="Edit profile">${user.email}</a><br />
                        ${user.firstName} ${user.lastName}<br /><br />
                        <div class="d-grid gap-1 col-6 mx-auto">
                            <a href="/logout" class="btn btn-outline-dark btn-sm" role="button">Logout</a>
                        </div><br />
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
                            <c:if test="${user != null && user.role == 'SUPERADMIN'}">
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
            <%-- Operation status errorMessage --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
            <br />
            <c:if test="${customer != null}">
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link" href="/customer/${customer.id}">Customer's profile</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="/customer/accounts/${customer.id}">Customer's accounts</a>
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
                <br /><h3>Customer's accounts:</h3><br />
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Balance</th>
                        <th scope="col">Created</th>
                        <th scope="col">Status</th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${accounts}" var="account">
                        <tr>
                            <th scope="row">${account.id}</th>
                            <td>${account.balance}</td>
                            <td>${account.regDate}</td>
                            <c:if test="${account.status == 'BLOCKED'}">
                                <td class="table-danger">${account.status}</td>
                                <td>
                                    <form style="display: inline;"
                                          method="POST" action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="command" value="ACTIVATE_ACCOUNT" />
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <button type="submit" class="btn btn-outline-primary">ACTIVATE</button>
                                    </form>
                            </c:if>
                            <c:if test="${account.status != 'BLOCKED'}">
                                <td>${account.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="command" value="BLOCK_ACCOUNT" />
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <button type="submit" class="btn btn-outline-danger">&nbsp;&nbsp;BLOCK&nbsp;&nbsp;</button>
                                    </form>
                            </c:if>
                            <c:if test="${user != null && user.role == 'SUPERADMIN'}">
                                <form style="display: inline;" method="POST"
                                      action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                      onsubmit="return confirm('Are you sure? Action cannot be canceled');">
                                    <input type="hidden" name="command" value="DELETE_ACCOUNT" />
                                    <input type="hidden" name="id" value="${account.id}" />
                                    <button type="submit" class="btn btn-danger">DELETE</button>
                                </form>
                            </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            <br />
                <br /><h3>Customer's cards:</h3><br />
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Network</th>
                        <th scope="col">Account</th>
                        <th scope="col">Expiration date</th>
                        <th scope="col">Status</th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${cards}" var="card">
                        <tr>
                            <th scope="row">${card.id}</th>
                            <td><project:cardnet network="${card.network}" /> ${card.network.name}</td>
                            <td>${card.accountId}</td>
                            <td>${card.expDate}</td>
                            <c:if test="${card.status == 'BLOCKED'}">
                                <td class="table-danger">${card.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="command" value="ACTIVATE_CARD" />
                                        <input type="hidden" name="id" value="${card.id}" />
                                        <button type="submit" class="btn btn-outline-primary">ACTIVATE</button>
                                    </form>
                            </c:if>
                            <c:if test="${card.status != 'BLOCKED'}">
                                <td>${card.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="command" value="BLOCK_CARD" />
                                        <input type="hidden" name="id" value="${card.id}" />
                                        <button type="submit" class="btn btn-outline-danger">&nbsp;&nbsp;BLOCK&nbsp;&nbsp;</button>
                                    </form>
                            </c:if>
                            <c:if test="${user != null && user.role == 'SUPERADMIN'}">
                                <form style="display: inline;" method="POST"
                                      action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                      onsubmit="return confirm('Are you sure? Action cannot be canceled');">
                                    <input type="hidden" name="command" value="DELETE_CARD" />
                                    <input type="hidden" name="id" value="${card.id}" />
                                    <button type="submit" class="btn btn-danger">DELETE</button>
                                </form>
                            </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <br />
            </c:if>
            <c:if test="${customer == null && updated == null}">
                <div class="alert alert-danger" role="alert">
                    <p>Customer with specified id not found</p>
                </div>
                <p><a href="/customers">&#9665; back to customers list</a></p>
            </c:if>
        </div>
        <br /><br />
        <div class="container" style="background-color: rgba(232, 232, 232, 0.3);">
            <br />
            <p class="text-center">
                <a href="/lang?locale=ru&uri=${requestScope['jakarta.servlet.forward.request_uri']}"
                   title="Русская версия">Русский</a>
                &nbsp;|&nbsp;
                <a href="/lang?locale=en&uri=${requestScope['jakarta.servlet.forward.request_uri']}"
                   class="pe-none" tabindex="-1" aria-disabled="true">English</a>
            </p>
            <br />
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" crossorigin="anonymous"></script>
    </body>
</html>