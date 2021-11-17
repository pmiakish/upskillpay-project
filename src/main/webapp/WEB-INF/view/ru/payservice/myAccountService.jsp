<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - сервис клиента" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous" />
        <title>Клиентский сервис<c:if test="${account != null}"> (счет ${account.id})</c:if> - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY лого">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Account<c:if test="${account != null}"> ${account.id}</c:if></h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Клиент:</strong><br />
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
                                <a class="nav-link" href="/payservice/my_accounts">Мои счета</a>
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
                        <strong>CVC - ${createdCvc}</strong>
                    </div>
                </c:if>
            <br />
            <c:if test="${account != null}">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="/payservice/my_account_service/${account.id}">Счет ${account.id}</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/payservice/my_account_incoming/${account.id}">Приходные операции</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/payservice/my_account_outgoing/${account.id}">Расходные операции</a>
                    </li>
                </ul>
                <table class="table">
                    <thead class="table-light">
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Баланс</th>
                            <th scope="col">Создан</th>
                            <th scope="col">Статус</th>
                            <th scope="col">Доступные действия</th>
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
                                          onsubmit="return confirm('Вы уверены? Счет может быть разблокирован только администратором!');">
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <input type="hidden" name="target" value="accBlock" />
                                        <button type="submit" class="btn btn-outline-danger">БЛОКИРОВАТЬ</button>
                                    </form>
                                    <form style="display: inline;" method="POST"
                                          action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                          onsubmit="return confirm('Вы уверены? Действие не может быть отменено!');">
                                        <input type="hidden" name="id" value="${account.id}" />
                                        <input type="hidden" name="target" value="accDelete" />
                                        <button type="submit" class="btn btn-danger">УДАЛИТЬ</button>
                                    </form>
                                </td>
                            </c:if>
                        </tr>
                    </tbody>
                </table>
                <br />
                <c:if test="${cards != null}">
                    <p><h3>Карты:</h3></p>
                    <table class="table">
                        <thead class="table-light">
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Сеть</th>
                            <th scope="col">Окончание действия</th>
                            <th scope="col">Статус</th>
                            <th scope="col">Доступные действия</th>
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
                                              onsubmit="return confirm('Вы уверены? Карта может быть разблокироана только администратором!');">
                                            <input type="hidden" name="target" value="cardBlock" />
                                            <input type="hidden" name="id" value="${card.id}" />
                                            <button type="submit" class="btn btn-outline-danger">БЛОКИРОВАТЬ</button>
                                        </form>
                                        <form style="display: inline;" method="POST"
                                              action="${requestScope['jakarta.servlet.forward.request_uri']}"
                                              onsubmit="return confirm('Вы уверены? Действие не может быть отменено!');">
                                            <input type="hidden" name="id" value="${card.id}" />
                                            <input type="hidden" name="target" value="cardDelete" />
                                            <button type="submit" class="btn btn-danger">УДАЛИТЬ</button>
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
                                  onsubmit="return confirm('Вы уверены?');">
                                <input type="hidden" name="target" value="addCard" />
                                <div class="input-group mb-3">
                                    <label class="input-group-text" for="inputNewCard">Карта</label>
                                    <select class="form-select" id="inputNewCard" name="cardNet" required>
                                        <option selected>Выбрать ...</option>
                                        <c:forEach items="${cardNetworks}" var="net">
                                            <option value="${net}">${net.name} ($${net.cost})</option>
                                        </c:forEach>
                                    </select>
                                    <button type="submit" class="btn btn-outline-primary">Выпустить карту</button>
                                </div>
                            </form>
                        </div>
                        <div class="col">
                            <p class="lead">* вы можете выпустить до 3 карт к счету</p>
                        </div>
                    </div>
                    <br />
                    <c:if test="${cards.size() > 0 && account.status == 'ACTIVE'}">
                        <p><h3>Совершить платеж:</h3></p>
                        <form method="POST" action="${requestScope['jakarta.servlet.forward.request_uri']}"
                              class="needs-validation" novalidate onsubmit="return confirm('Вы уверены? Итоговая ' +
                               'сумма с комиссией составит ' + getTotal());">
                            <input type="hidden" name="target" value="payment" />
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="inputCard">Карта</label>
                                <select class="form-select" id="inputCard" name="id" required>
                                    <option selected>Выбрать ...</option>
                                    <c:forEach items="${cards}" var="card">
                                        <c:if test="${card.status == 'ACTIVE'}">
                                            <option value="${card.id}">${card.id} (${card.network.name})</option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                                <label class="input-group-text" for="inputCvc">CVC</label>
                                <input type="password" class="form-control" id="inputCvc" name="cvc" pattern="^[0-9]{3}$" required />
                                <label class="input-group-text" for="inputReceiver">Счет получателя</label>
                                <input type="text" class="form-control" id="inputReceiver" name="receiver" pattern="[0-9]+" required />
                                <label class="input-group-text" for="inputAmount">Сумма</label>
                                <input type="text" class="form-control" id="inputAmount" name="amount" pattern="^[0-9]*.?[0-9]+$" required />
                                <button type="submit" class="btn btn-outline-primary">Оплатить</button>
                            </div>
                        </form>
                    </c:if>
                </c:if>
            </c:if>
            <c:if test="${account == null}">
                <div class="alert alert-danger" role="alert">
                    Счет с указанным идентификатором не найден
                </div>
                <p><a href="/payservice/my_accounts">&#9665; назад к списку счетов</a></p>
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