<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - управление счетами клиента" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Счета клиента - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../../../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY лого">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Счета клиента</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Администратор:</strong><br />
                            <a href="/profile" title="Редактировать профиль">${user.email}</a><br />
                            ${user.firstName} ${user.lastName}<br />
                            <div class="d-grid gap-1 col-6 mx-auto">
                                <a href="/logout" class="btn btn-outline-dark btn-sm" role="button">Выйти</a>
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
                                <a class="nav-link" href="/customers">Клиенты</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/accounts">Счета</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/cards">Карты</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/payments">Платежи</a>
                            </li>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
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
            <%-- Operation status message --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
            <br />
            <c:if test="${customer != null}">
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link" href="/customer/${customer.id}">Профиль клиента</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="/customer/accounts/${customer.id}">Счета клиента</a>
                </li>
            </ul>
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Электронная почта</th>
                        <th scope="col">Имя</th>
                        <th scope="col">Фамилия</th>
                        <th scope="col">Регистрация</th>
                        <th scope="col">Статус</th>
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
                <p><h3>Счета клиента:</h3></p>
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Баланс</th>
                        <th scope="col">Создан</th>
                        <th scope="col">Статус</th>
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
                                        <input type="hidden" name="target" value="accStat" />
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <input type="hidden" name="currentStatus" value="${account.status}" />
                                        <button type="submit" class="btn btn-outline-primary">АКТИВИРОВАТЬ</button>
                                    </form>
                            </c:if>
                            <c:if test="${account.status != 'BLOCKED'}">
                                <td>${account.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="target" value="accStat" />
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <input type="hidden" name="currentStatus" value="${account.status}" />
                                        <button type="submit" class="btn btn-outline-danger">БЛОКИРОВАТЬ</button>
                                    </form>
                            </c:if>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
                                <form style="display: inline;" method="POST"
                                      action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                      onsubmit="return confirm('Вы уверены? Действие не может быть отменено!');">
                                    <input type="hidden" name="target" value="accDelete" />
                                    <input type="hidden" name="id" value="${account.id}" />
                                    <button type="submit" class="btn btn-danger">УДАЛИТЬ</button>
                                </form>
                            </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            <br />
                <p><h3>Карты клиента:</h3></p>
                <table class="table">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Сеть</th>
                        <th scope="col">Счет</th>
                        <th scope="col">Окончание действия</th>
                        <th scope="col">Статус</th>
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
                                        <input type="hidden" name="target" value="cardStat" />
                                        <input type="hidden" name="id" value="${card.id}" />
                                        <input type="hidden" name="currentStatus" value="${card.status}" />
                                        <button type="submit" class="btn btn-outline-primary">АКТИВИРОВАТЬ</button>
                                    </form>
                            </c:if>
                            <c:if test="${card.status != 'BLOCKED'}">
                                <td>${card.status}</td>
                                <td>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}">
                                        <input type="hidden" name="target" value="cardStat" />
                                        <input type="hidden" name="id" value="${card.id}" />
                                        <input type="hidden" name="currentStatus" value="${card.status}" />
                                        <button type="submit" class="btn btn-outline-danger">БЛОКИРОВАТЬ</button>
                                    </form>
                            </c:if>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
                                <form style="display: inline;" method="POST"
                                      action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                      onsubmit="return confirm('Вы уверены? Действие не может быть отменено!');">
                                    <input type="hidden" name="target" value="cardDelete" />
                                    <input type="hidden" name="id" value="${card.id}" />
                                    <button type="submit" class="btn btn-danger">УДАЛИТЬ</button>
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
                    <p>Клиент с указанным идентификатором не найден</p>
                    <p><a href="/customers">< назад к списку клиентов</a></p></div>
            </c:if>
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
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" crossorigin="anonymous"></script>
    </body>
</html>