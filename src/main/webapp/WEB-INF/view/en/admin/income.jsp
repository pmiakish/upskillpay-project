<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - system income info" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>System income info - UpSkillPAY</title>
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
                    <h1>System income</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <br /><br /><strong>Superadmin:</strong><br />
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
                            <li class="nav-item">
                                <a class="nav-link" href="/admins">Admins</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" aria-current="page" href="/income">Income</a>
                            </li>

                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <br />
            <h3>Current system balance:</h3>
            <p class="lead">
                <c:if test="${balance != null}">$${balance}</c:if>
                <c:if test="${balance == null}"><div class="alert alert-danger" role="alert">Cannot retrieve balance</div></c:if>
            </p>
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
    </body>
</html>