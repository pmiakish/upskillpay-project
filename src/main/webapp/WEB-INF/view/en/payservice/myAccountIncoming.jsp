<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - customer's account incoming payments" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous" />
        <title>Incoming payments<c:if test="${account != null}"> (account ${account.id})</c:if> - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Account<c:if test="${account != null}"> ${account.id}</c:if></h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Customer:</strong><br />
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
                                <a class="nav-link" href="/payservice/my_accounts">My accounts</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <%-- Operation status message --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
                <c:if test="${createdCvc != null}">
                    <div class="alert alert-success" role="alert">
                        <strong>CVC is ${createdCvc}</strong>
                    </div>
                </c:if>
            <br />
            <c:if test="${account != null}">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link" href="/payservice/my_account_service/${account.id}">Account ${account.id}</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="/payservice/my_account_incoming/${account.id}">Incoming payments</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/payservice/my_account_outgoing/${account.id}">Outgoing payments</a>
                    </li>
                </ul>
                <table class="table">
                    <thead class="table-light">
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Balance</th>
                            <th scope="col">Created</th>
                            <th scope="col">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th scope="row">${account.id}</th>
                            <td>${account.balance}</td>
                            <td>${account.regDate}</td>
                            <c:if test="${account.status == 'BLOCKED'}">
                                <td class="table-danger">${account.status}</td>
                            </c:if>
                            <c:if test="${account.status != 'BLOCKED'}">
                                <td>${account.status}</td>
                            </c:if>
                        </tr>
                    </tbody>
                </table>
                <br />
                <div class="container">
                    <p><h3>Incoming payments:</h3></p>
                    <p class="lead">Total found: ${page.total} entries</p>
                    <br />
                    <table class="table">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">
                                <project:sortlink page="${page}" endpoint="/payservice/my_account_incoming/${account.id}"
                                                  target="id" description="ID" />
                            </th>
                            <th scope="col">
                                <project:sortlink page="${page}" endpoint="/payservice/my_account_incoming/${account.id}"
                                                  target="amount" description="Amount" />
                            </th>
                            <th scope="col">
                                <project:sortlink page="${page}" endpoint="/payservice/my_account_incoming/${account.id}"
                                                  target="payer" description="Payer's account" />
                            </th>
                            <th scope="col">
                                <project:sortlink page="${page}" endpoint="/payservice/my_account_incoming/${account.id}"
                                                  target="date" description="Timestamp" />
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${page.entries}" var="payment">
                            <tr>
                                <th scope="row">${payment.id}</th>
                                <td>${payment.amount}</td>
                                <td>
                                    <c:if test="${payment.payerId == 0}">SYSTEM</c:if>
                                    <c:if test="${payment.payerId != 0}">${payment.payerId}</c:if>
                                </td>
                                <td>${payment.dateTime.format(formatter)}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="container">
                    <div class="row">
                        <div class="col">
                            <project:pagination page="${page}" endpoint="/payservice/my_account_incoming/${account.id}" />
                        </div>
                        <div class="col-2">
                            <form method="GET" action="/payservice/my_account_incoming/${account.id}">
                                <input type="hidden" name="page" value="1" />
                                <input type="hidden" name="sort" value="${page.sort}" />
                                <div class="form-group">
                                    <select name="entries" class="form-control" id="pageSizeSelect" onchange='this.form.submit()'>
                                        <option selected>Page size</option>
                                        <option value="3">3</option>
                                        <option value="5">5</option>
                                        <option value="10">10</option>
                                        <option value="20">20</option>
                                        <option value="50">50</option>
                                    </select>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

            </c:if>
            <c:if test="${account == null}">
                <div class="alert alert-danger" role="alert">
                    Account with specified id not found
                </div>
                <p><a href="/payservice/my_accounts">&#9665; back to accounts list</a></p>
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
        <script type="text/javascript">
            function getTotal() {
                var amount = Math.round(document.getElementById('inputAmount').value * 100) / 100;
                var commissionAmount = Math.round(amount * (${commissionRate}) * 100) / 100;
                amount += commissionAmount;
                return amount.toFixed(2);
            }
        </script>
        <script type="text/javascript">
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