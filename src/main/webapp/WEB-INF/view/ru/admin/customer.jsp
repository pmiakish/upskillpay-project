<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - управление профилем клиента" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Профиль клиента - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150"
                             height="91" alt="UpSkillPAY лого" />
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Профиль клиента</h1>
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
                                <a class="nav-link" href="/payments">Платежи</a>
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
            <%-- Operation status errorMessage --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
            <br />
            <c:if test="${customer != null}">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="/customer/${customer.id}">Профиль клиента</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/customer/accounts/${customer.id}">Счета клиента</a>
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
                <br /><h3>Edit customer's profile:</h3><br />
                <br />
                <form method="POST" action="/customer/${customer.id}" class="row g-3 needs-validation" novalidate
                      oninput='cPass.setCustomValidity(cPass.value != pass.value ? "Пароли не совпадают" : "")'>
                    <input type="hidden" name="id" value="${customer.id}" />
                    <input type="hidden" name="command" value="UPDATE_PERSON" />
                    <input type="hidden" name="hash" value="${customer.hash}" />
                    <div class="mb-3 row">
                        <label for="staticID" class="col-sm-2 col-form-label"><strong>ID</strong></label>
                        <div class="col-sm-10">
                            <input type="text" readonly class="form-control-plaintext" id="staticID" value="${customer.id}" name="id" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="staticEmail" class="col-sm-2 col-form-label">
                            <strong>Электронная почта (логин)</strong>
                        </label>
                        <div class="col-sm-10">
                            <input type="text" readonly class="form-control-plaintext" id="staticEmail"
                                   value="${customer.email}" name="email" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="selectRole" class="col-sm-2 col-form-label"><strong>Разрешения</strong></label>
                        <div class="col-sm-10">
                            <select class="form-select" id="selectRole"
                                    name="role"<c:if test="${user == null || user.role != 'SUPERADMIN'}"> disabled</c:if>>
                                <option value="CUSTOMER" selected>Клиент</option>
                                <option value="ADMIN">Администратор</option>
                                <option value="SUPERADMIN">Главный администратор</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputPassword" class="col-sm-2 col-form-label"><strong>Новый пароль</strong></label>
                        <div class="col-sm-10">
                            <input type="password" class="form-control" id="inputPassword" name="pass" maxlength="100" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="confirmPassword" class="col-sm-2 col-form-label"><strong>Подтвердить пароль</strong></label>
                        <div class="col-sm-10">
                            <input type="password" class="form-control" id="confirmPassword" name="cPass" maxlength="100" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputFirstName" class="col-sm-2 col-form-label"><strong>Имя</strong></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputFirstName" pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+"
                                   maxlength="30" value="${customer.firstName}" name="firstName" required />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputLastName" class="col-sm-2 col-form-label"><strong>Фамилия</strong></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputLastName" pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+"
                                   maxlength="30" value="${customer.lastName}" name="lastName" required />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputRegDate" class="col-sm-2 col-form-label"><strong>Регистрация</strong></label>
                        <div class="col-sm-10">
                            <input type="date" class="form-control" id="inputRegDate" value="${customer.regDate}"
                                   name="regDate" required />
                        </div>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" id="activation"
                               name="active"<c:if test="${customer.status == 'ACTIVE'}"> checked</c:if> />
                        <label class="form-check-label" for="activation">Активировать клиента</label>
                    </div>
                    <br />

                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">Применить</button>
                    </div>
                </form>
                <br />
                <c:if test="${user.role == 'SUPERADMIN'}">
                    <form method="POST" action="/customer/${customer.id}" onsubmit="return confirm('Вы уверены, что ' +
                     'хотите удалить профиль? Данное действие не может быть отменено!');">
                        <input type="hidden" name="id" value="${customer.id}" />
                        <input type="hidden" name="command" value="DELETE_PERSON" />
                        <div class="col-12">
                            <button type="submit" class="btn btn-danger">Удалить клиента</button>
                        </div>
                    </form>
                    <br />
                </c:if>

            </c:if>
            <c:if test="${customer == null}">
                <div class="alert alert-danger" role="alert">
                    Клиент с указанным идентификатором не найден
                </div>
                <p><a href="/customers">&#9665; назад к списку клиентов</a></p>
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