<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - профиль" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Мой профиль - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY лого">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Мой профиль</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Пользователь:</strong><br />
                            ${user.email}<br />
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
                            <c:if test="${user != null && (user.permission == 'SUPERADMIN' || user.permission == 'ADMIN')}">
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
                            </c:if>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="/admins">Администраторы</a>
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
            <c:if test="${user != null}">
                <p><h3>Редактировать профиль:</h3></p>
                <br />
                <form method="POST" action="/profile" class="row g-3 needs-validation" novalidate
                      oninput='cPass.setCustomValidity(cPass.value != pass.value ? "Пароли не совпадают" : "")'>
                    <input type="hidden" id="inputHash" value="${user.hash}" name="hash" />
                    <div class="mb-3 row">
                        <label for="staticEmail" class="col-sm-2 col-form-label"><strong>Электронная почта (логин)</strong></label>
                        <div class="col-sm-10">
                            <input type="text" readonly class="form-control-plaintext" id="staticEmail" value="${user.email}" name="email" />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputPassword" class="col-sm-2 col-form-label"><strong>Новый пароль</strong></label>
                        <div class="col-sm-10">
                            <input type="password" class="form-control" id="inputPassword" name="pass"  maxlength="100" />
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
                                   maxlength="30" value="${user.firstName}" name="firstName" required />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="inputLastName" class="col-sm-2 col-form-label"><strong>Фамилия</strong></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="inputLastName" pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+"
                                   maxlength="30" value="${user.lastName}" name="lastName" required />
                        </div>
                    </div>
                    <div class="mb-3 row">
                        <label for="staticRegDate" class="col-sm-2 col-form-label"><strong>Регистрация</strong></label>
                        <div class="col-sm-10">
                            <input type="text" readonly class="form-control-plaintext" id="staticRegDate"
                                   value="${user.regDate}" name="regDate" />
                        </div>
                    </div>
                    <br />
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">Применить</button>
                    </div>
                </form>
                <br />
            </c:if>
            <c:if test="${user == null}">
                <div class="alert alert-danger" role="alert">
                    Ошибка загрузки пользовательских данных
                </div>
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
                integrity="sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" crossorigin="anonymous">
        </script>
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