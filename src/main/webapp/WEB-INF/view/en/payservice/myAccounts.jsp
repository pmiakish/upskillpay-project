<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - customer's accounts" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>My accounts - UpSkillPAY</title>
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
                    <h1>Account list</h1>
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
                                <a class="nav-link active" aria-current="page" href="/payservice/my_accounts">My accounts</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <%-- Operation status errorMessage --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
            <br />
            <table class="table">
                <thead class="table-light">
                    <tr>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payservice/my_accounts" target="id" description="ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payservice/my_accounts" target="balance" description="Balance" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payservice/my_accounts" target="regdate" description="Created" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payservice/my_accounts" target="status" description="Status" />
                        </th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${page.entries}" var="account">
                    <tr>
                        <th scope="row">
                            <a href="/payservice/my_account_service/${account.id}" title="Manage account">${account.id}</a>
                        </th>
                        <td>${account.balance}</td>
                        <td>${account.regDate}</td>
                        <c:if test="${account.status == 'BLOCKED'}">
                            <td class="table-danger">${account.status}</td>
                            <td class="table-danger">&nbsp;</td>
                        </c:if>
                        <c:if test="${account.status != 'BLOCKED'}">
                            <td>${account.status}</td>
                            <td>
                                <a href="/payservice/my_account_service/${account.id}" title="Manage account">
                                    <img src="${pageContext.request.contextPath}/img/payment.png" width="25" height="25" alt="manage account" />
                                </a>&nbsp;&nbsp;
                                <button type="button" data-bs-toggle="modal" data-bs-target="#topupModal"
                                        data-bs-accountId="${account.id}" style="border: none; background-color: transparent;">
                                    <img src="${pageContext.request.contextPath}/img/topup.png" width="25" height="25" alt="top up account" />
                                </button>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <div class="row">
                <div class="col-3">
                    <form method="POST" action="/payservice/my_accounts?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}">
                        <input type="hidden" name="command" value="ADD_ACCOUNT" />
                        <button type="submit" class="btn btn-outline-primary">Add new account</button>
                    </form>
                </div>
                <div class="col">
                    <p class="lead">* you can add up to 5 accounts</p>
                </div>
            </div>
        </div>
        <div class="container">
            <project:pagination page="${page}" endpoint="/payservice/my_accounts" />
        </div>
        <%-- Modal --%>
        <div class="modal fade" id="topupModal" tabindex="-1" aria-labelledby="topupModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="topupModalLabel">Top up account</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form method="POST" action="/payservice/my_accounts?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}"
                                class="needs-validation" novalidate>
                        <div class="modal-body">
                            <input type="hidden" id="acc-id" name="accountId" />
                            <input type="hidden" name="command" value="INCREASE_ACCOUNT" />
                            <div class="mb-3">
                                You can replenish your account for no more than $100 per day
                                <label for="amount" class="form-label">Amount:</label>
                                <input type="text" class="form-control" id="amount" name="amount" required
                                       pattern="^[0-9]*.?[0-9]+$" />
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button type="submit" class="btn btn-primary">Top up</button>
                        </div>
                    </form>
                </div>
            </div>
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
            var topupModal = document.getElementById('topupModal')
            topupModal.addEventListener('show.bs.modal', function (event) {
                // Button that triggered the modal
                var button = event.relatedTarget
                // Extract info from data-bs-* attributes
                var acc = button.getAttribute('data-bs-accountId')
                var modalBodyInput = topupModal.querySelector('#acc-id')
                var modalTitle = topupModal.querySelector('.modal-title')
                modalTitle.textContent = 'Top Up Account ' + acc
                modalBodyInput.value = acc
            })
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