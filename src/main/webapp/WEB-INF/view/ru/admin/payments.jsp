<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - информация о платежах" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>История платежей - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150"
                             height="91" alt="UpSkillPAY лого"/>
                    </a>
                </div>
                <div class="col-6 align-self-center">
                    <h1>Платежи</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <br /><br /><strong>Администратор:</strong><br />
                        <a href="/profile" title="Редактировать профиль">${user.email}</a><br />
                        ${user.firstName} ${user.lastName}<br />
                        <div class="d-grid gap-1 col-6 mx-auto">
                            <a href="/logout" class="btn btn-outline-dark btn-sm" role="button">Выйти</a>
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
                                <a class="nav-link" href="/customers">Клиенты</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/accounts">Счета</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/cards">Карты</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" aria-current="page" href="/payments">Платежи</a>
                            </li>
                            <c:if test="${user != null && user.role == 'SUPERADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="/admins">Администраторы</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="/income">Системный счет</a>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
        <div class="container">
            <br />
            <p class="lead">Всего найдено: ${page.total} записей</p>
            <br />
            <table class="table">
                <thead class="table-light">
                    <tr>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payments" target="id" description="ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payments" target="amount" description="Сумма" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payments" target="payer" description="Счет плательщика" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payments" target="receiver" description="Счет получателя" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/payments" target="date" description="Дата и время" />
                        </th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${page.entries}" var="payment">
                    <tr>
                        <th scope="row">${payment.id}</th>
                        <td>${payment.amount}</td>
                        <td>
                            <c:if test="${payment.payerId == 0}">СИСТЕМА</c:if>
                            <c:if test="${payment.payerId != 0}">${payment.payerId}</c:if>
                        </td>
                        <td>
                            <c:if test="${payment.receiverId == 0}">СИСТЕМА</c:if>
                            <c:if test="${payment.receiverId != 0}">${payment.receiverId}</c:if>
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
                    <project:pagination page="${page}" endpoint="/payments" />
                </div>
                <div class="col-2">
                    <form method="GET" action="/payments">
                        <input type="hidden" name="page" value="1" />
                        <input type="hidden" name="sort" value="${page.sort}" />
                        <div class="form-group">
                            <select name="entries" class="form-control" id="pageSizeSelect" onchange='this.form.submit()'>
                                <option selected>Показать записей</option>
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
        <br /><br />
        <div class="container" style="background-color: rgba(232, 232, 232, 0.3);">
            <br />
            <p class="text-center">
                <a href="/lang?locale=ru&uri=${requestScope['jakarta.servlet.forward.request_uri']}"
                   class="pe-none" tabindex="-1" aria-disabled="true">Русский</a>
                &nbsp;|&nbsp;
                <a href="/lang?locale=en&uri=${requestScope['jakarta.servlet.forward.request_uri']}"
                   title="English version">English</a>
            </p>
            <br />
        </div>
    </body>
</html>