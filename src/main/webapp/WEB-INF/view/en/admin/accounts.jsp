<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - accounts management" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Account list - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150"
                             height="91" alt="UpSkillPAY logo" />
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Account list</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <br /><br /><strong>Admin:</strong><br />
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
                                <a class="nav-link" href="/customers">Customers</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" aria-current="page" href="/accounts">Accounts</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/cards">Cards</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/payments">Payments</a>
                            </li>
                            <c:if test="${user != null && user.role == 'SUPERADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="/admins">Admins</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="/income">Income</a>
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
            <p class="lead">Total found: ${page.total} entries</p>
            <br />
            <table class="table">
                <thead class="table-light">
                    <tr>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/accounts" target="id" description="ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/accounts" target="owner" description="Owner's ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/accounts" target="balance" description="Balance" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/accounts" target="regdate" description="Created" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/accounts" target="status" description="Status" />
                        </th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${page.entries}" var="account">
                    <tr>
                        <th scope="row">${account.id}</th>
                        <td><a href="customer/${account.ownerId}">${account.ownerId}</a></td>
                        <td>${account.balance}</td>
                        <td>${account.regDate}</td>
                        <c:if test="${account.status == 'BLOCKED'}">
                            <td class="table-danger">${account.status}</td>
                            <td>
                                <form method="POST" action="/accounts?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}">
                                    <input type="hidden" name="id" value="${account.id}" />
                                    <input type="hidden" name="command" value="ACTIVATE_ACCOUNT" />
                                    <button type="submit" class="btn btn-outline-primary">ACTIVATE</button>
                                </form>
                            </td>
                        </c:if>
                        <c:if test="${account.status != 'BLOCKED'}">
                            <td>${account.status}</td>
                            <td>
                                <form method="POST" action="/accounts?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}">
                                    <input type="hidden" name="id" value="${account.id}" />
                                    <input type="hidden" name="command" value="BLOCK_ACCOUNT" />
                                    <button type="submit" class="btn btn-outline-danger">&nbsp;&nbsp;BLOCK&nbsp;&nbsp;</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="container">
            <div class="row">
                <div class="col">
                    <project:pagination page="${page}" endpoint="/accounts" />
                </div>
                <div class="col-2">
                    <form method="GET" action="/accounts">
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