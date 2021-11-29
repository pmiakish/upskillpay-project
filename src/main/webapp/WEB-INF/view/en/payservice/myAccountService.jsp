<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - customer's account service" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous" />
        <title>My account service<c:if test="${account != null}"> (account ${account.id})</c:if> - UpSkillPAY</title>
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
                    <h1>Account<c:if test="${account != null}"> ${account.id}</c:if></h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <br /><br /><strong>Customer:</strong><br />
                        <a href="/profile" title="Edit profile">${user.email}</a><br />
                        ${user.firstName} ${user.lastName}<br />
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
                                <a class="nav-link" href="/payservice/my_accounts">My accounts</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <%-- Operation status errorMessage --%>
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
                        <a class="nav-link active" aria-current="page" href="/payservice/my_account_service/${account.id}">Account ${account.id}</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/payservice/my_account_incoming/${account.id}">Incoming payments</a>
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
                            <th scope="col">Allowed actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th scope="row">${account.id}</th>
                            <td>${account.balance}</td>
                            <td>${account.regDate}</td>
                            <c:if test="${account.status == 'BLOCKED'}">
                                <td class="table-danger">${account.status}</td>
                                <td class="table-danger">&nbsp;</td>
                            </c:if>
                            <c:if test="${account.status != 'BLOCKED'}">
                                <td>${account.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                          onsubmit="return confirm('Are you sure? Account may be unblocked only by administrator');">
                                        <input type="hidden" name="accountId" value="${account.id}" />
                                        <input type="hidden" name="command" value="BLOCK_ACCOUNT" />
                                        <button type="submit" class="btn btn-outline-danger">&nbsp;&nbsp;BLOCK&nbsp;&nbsp;</button>
                                    </form>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                          onsubmit="return confirm('Are you sure? Action cannot be canceled');">
                                        <input type="hidden" name="accountId" value="${account.id}" />
                                        <input type="hidden" name="command" value="DELETE_ACCOUNT" />
                                        <button type="submit" class="btn btn-danger">&nbsp;&nbsp;DELETE&nbsp;&nbsp;</button>
                                    </form>
                                </td>
                            </c:if>
                        </tr>
                    </tbody>
                </table>
                <br />
                <c:if test="${cards != null}">
                    <br /><h3>Сards:</h3><br />
                    <table class="table">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Network</th>
                            <th scope="col">Expiration date</th>
                            <th scope="col">Status</th>
                            <th scope="col">Allowed actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${cards}" var="card">
                            <tr>
                                <th scope="row">${card.id}</th>
                                <td><project:cardnet network="${card.network}" /> ${card.network.name}</td>

                                <td>${card.expDate}</td>
                                <c:if test="${card.status == 'BLOCKED'}">
                                    <td class="table-danger">${card.status}</td>
                                    <td class="table-danger">&nbsp;</td>
                                </c:if>
                                <c:if test="${card.status != 'BLOCKED'}">
                                    <td>${card.status}</td>
                                    <td>
                                        <form style="display: inline;"  method="POST"
                                              action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                              onsubmit="return confirm('Are you sure? Card may be unblocked only by administrator');">
                                            <input type="hidden" name="cardId" value="${card.id}" />
                                            <input type="hidden" name="command" value="BLOCK_CARD" />
                                            <button type="submit" class="btn btn-outline-danger">&nbsp;&nbsp;BLOCK&nbsp;&nbsp;</button>
                                        </form>
                                        <form style="display: inline;" method="POST"
                                              action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                              onsubmit="return confirm('Are you sure? Action cannot be canceled');">
                                            <input type="hidden" name="cardId" value="${card.id}" />
                                            <input type="hidden" name="command" value="DELETE_CARD" />
                                            <button type="submit" class="btn btn-danger">&nbsp;&nbsp;DELETE&nbsp;&nbsp;</button>
                                        </form>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <div class="row">
                        <div class="col-6">
                            <form method="POST" action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                  onsubmit="return confirm('Are you sure?');">
                                <input type="hidden" name="accountId" value="${account.id}" />
                                <input type="hidden" name="command" value="ADD_CARD" />
                                <div class="input-group mb-3">
                                    <label class="input-group-text" for="inputNewCard">Card</label>
                                    <select class="form-select" id="inputNewCard" name="cardNet" required>
                                        <option selected>Choose ...</option>
                                        <c:forEach items="${cardNetworks}" var="net">
                                            <option value="${net}">${net.name} ($${net.cost})</option>
                                        </c:forEach>
                                    </select>
                                    <button type="submit" class="btn btn-outline-primary">Add new card</button>
                                </div>
                            </form>
                        </div>
                        <div class="col">
                            <p class="lead">* you can add up to 3 cards per account</p>
                        </div>
                    </div>
                    <br />
                    <c:if test="${cards.size() > 0 && account.status == 'ACTIVE'}">
                        <br /><h3>Make payment:</h3><br />
                        <form method="POST" action="${requestScope['jakarta.servlet.forward.request_uri']}"
                              class="needs-validation" novalidate onsubmit="return confirm('Are you sure? The total ' +
                               'amount with the commission is ' + getTotal());">
                            <input type="hidden" name="command" value="PAYMENT" />
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="inputCard">Card</label>
                                <select class="form-select" id="inputCard" name="cardId" required>
                                    <option selected>Choose ...</option>
                                    <c:forEach items="${cards}" var="card">
                                        <c:if test="${card.status == 'ACTIVE'}">
                                            <option value="${card.id}">${card.id} (${card.network.name})</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                                <label class="input-group-text" for="inputCvc">CVC</label>
                                <input type="password" class="form-control" id="inputCvc" name="cvc" pattern="^[0-9]{3}$"
                                       maxlength="3" required />
                                <label class="input-group-text" for="inputReceiver">Receiver's account</label>
                                <input type="text" class="form-control" id="inputReceiver" name="receiver" pattern="[0-9]+"
                                       maxlength="10" required />
                                <label class="input-group-text" for="inputAmount">Amount</label>
                                <input type="text" class="form-control" id="inputAmount" name="amount" pattern="^[0-9]*.?[0-9]+$"
                                       maxlength="20" required />
                                <button type="submit" class="btn btn-outline-primary">Pay</button>
                            </div>
                        </form>
                    </c:if>
                </c:if>
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