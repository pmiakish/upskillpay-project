<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <meta content="text/html; charset=utf-8" http-equiv="content-type" />
        <meta name="description" content="UpSkillPAY - учебный веб-проект" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>UpSkillPAY - добро пожаловать</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center">
                    <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY лого" />
                </div>
                <div class="col align-self-center">
                    <h1>UpSkill платежи</h1>
                </div>
            </div>
        </div>
        <br />
        <div class="container">
            <div class="row">
                <div class="col">
                    <img src="${pageContext.request.contextPath}/img/welcome.png" class="img-fluid" width="620" height="398" alt="UpSkillPAY добро пожаловать" />
                </div>
                <c:if test="${user == null}">
                    <div class="col">
                        <div class="accordion" id="startNav">
                            <div class="accordion-item">
                                <h2 class="accordion-header" id="headingCustomers">
                                    <button class="accordion-button" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#collapseCustomers" aria-expanded="true" aria-controls="collapseCustomers">
                                        Для клиентов
                                    </button>
                                </h2>
                                <div id="collapseCustomers" class="accordion-collapse collapse show"
                                     aria-labelledby="headingCustomers" data-bs-parent="#startNav">
                                    <div class="accordion-body">
                                        <p class="lead">Добро пожаловать в платежную систему <strong>UpSkillPAY</strong>!</p>
                                        <p>Пожалуйста, выберете интересующий раздел для того, чтобы опробавать все возможности.</p>
                                        <div class="d-grid gap-2">
                                            <a href="/payservice/my_accounts" class="btn btn-outline-primary" role="button">
                                                Мои счета
                                            </a>
                                            <a href="/profile" class="btn btn-outline-secondary" role="button">
                                                Мой профиль
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="accordion-item">
                                <h2 class="accordion-header" id="headingReg">
                                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#collapseReg" aria-expanded="false" aria-controls="collapseReg">
                                        Присоединиться к системе
                                    </button>
                                </h2>
                                <div id="collapseReg" class="accordion-collapse collapse" aria-labelledby="headingReg"
                                     data-bs-parent="#startNav">
                                    <div class="accordion-body">
                                        <p class="lead">Зарегистрируйтесь сейчас и получите $50 на первый счет!</p>
                                        <p><strong>Быстрая регистрация:</strong></p>
                                        <form action="/signup" method="POST" class="row g-3 needs-validation"
                                              accept-charset="utf-8" novalidate
                                              oninput='cPass.setCustomValidity(cPass.value != pass.value ?
                                               "Пароли не совпадают" : "")'>
                                            <input type="hidden" name="command" value="ADD_PERSON" />
                                            <div class="col-12">
                                                <label for="inputEmail" class="form-label">Электронная почта <sup>*</sup></label>
                                                <input type="email" class="form-control" id="inputEmail" name="email" required
                                                       minlength="6" maxlength="100"/>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="inputPassword" class="form-label">Пароль <sup>*</sup></label>
                                                <input type="password" class="form-control" id="inputPassword" name="pass" required
                                                       minlength="3" maxlength="255"/>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="confirmPassword" class="form-label">Подтвердите пароль <sup>*</sup></label>
                                                <input type="password" class="form-control" id="confirmPassword" name="cPass"
                                                       minlength="3" maxlength="255"/>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="inputFirstName" class="form-label">Имя <sup>*</sup></label>
                                                <input type="text" class="form-control" id="inputFirstName" name="firstName" required
                                                       pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+" minlength="2" maxlength="30"/>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="inputLastName" class="form-label">Фамилия <sup>*</sup></label>
                                                <input type="text" class="form-control" id="inputLastName" name="lastName" required
                                                       pattern="[A-ZА-Я][\-A-Za-zА-Яа-я ]+" minlength="2" maxlength="30"/>
                                            </div>
                                            <div class="col-12">
                                                <sup>*</sup> - обязательные поля
                                            </div>
                                            <div class="col-12">
                                                <button type="submit" class="btn btn-primary">Регистрация</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            <div class="accordion-item">
                                <h2 class="accordion-header" id="headingAdmin">
                                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#collapseAdmin" aria-expanded="false" aria-controls="collapseAdmin">
                                        Для администраторов проекта
                                    </button>
                                </h2>
                                <div id="collapseAdmin" class="accordion-collapse collapse" aria-labelledby="headingAdmin"
                                     data-bs-parent="#startNav">
                                    <div class="accordion-body">
                                        <div class="d-grid gap-2">
                                            <a href="/customers" class="btn btn-outline-primary" role="button">
                                                Управление пользователями
                                            </a>
                                            <a href="/accounts" class="btn btn-outline-primary" role="button">
                                                Управление счетами
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${user != null}">
                    <div class="col" style="background-color: rgba(232, 232, 232, 0.3)">
                        <p>
                            <strong>Добро пожаловать, ${user.firstName} ${user.lastName}</strong><br />
                            <div class="d-grid gap-2">
                                <c:if test="${user.role == 'SUPERADMIN' || user.role == 'ADMIN'}">
                                    <a href="/customers" class="btn btn-outline-primary" role="button">Управение клиентами</a>
                                    <a href="/accounts" class="btn btn-outline-primary" role="button">Управление счетами</a>
                                    <a href="/cards" class="btn btn-outline-primary" role="button">Управление картами</a>
                                    <a href="/payments" class="btn btn-outline-primary" role="button">Платежи</a>
                                </c:if>
                                <c:if test="${user.role == 'SUPERADMIN'}">
                                    <a href="/admins" class="btn btn-outline-danger" role="button">Управление администраторами</a>
                                </c:if>
                                <c:if test="${user.role == 'CUSTOMER'}">
                                    <a href="/payservice/my_accounts" class="btn btn-outline-primary" role="button">Мои счета</a>
                                </c:if>
                                <a href="/profile" class="btn btn-outline-secondary" role="button">Мой профиль</a>
                                <a href="/logout" class="btn btn-outline-dark" role="button">Выйти</a>
                            </div>
                        </p>
                    </div>
                </c:if>
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
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-/bQdsTh/da6pkI1MST/rWKFNjaCP5gBSY4sEBT38Q/9RBh9AH40zEOg7Hlq2THRZ" crossorigin="anonymous">
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
