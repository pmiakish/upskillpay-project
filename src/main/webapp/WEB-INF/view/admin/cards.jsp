<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - cards management" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Card list - UpSkillPAY</title>
    </head>
    <body>
        <br />
        <div class="container">
            <div class="row">
                <div class="col-3 align-self-center" >
                    <a href="/" title="UpSkillPAY">
                        <img src="../../../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Card list</h1>
                </div>
                <div class="col-3">
                    <c:if test="${user != null}">
                        <p>
                            <br /><strong>Admin:</strong><br />
                            <a href="/profile" title="Edit profile">${user.email}</a><br />
                            ${user.firstName} ${user.lastName}<br />
                            <div class="d-grid gap-1 col-6 mx-auto">
                                <a href="/logout" class="btn btn-outline-dark btn-sm" role="button">Logout</a>
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
                                <a class="nav-link" href="/customers">Customers</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/accounts">Accounts</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" aria-current="page" href="/cards">Cards</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="/payments">Payments</a>
                            </li>
                            <c:if test="${user != null && user.permission == 'SUPERADMIN'}">
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
            <%-- Operation status message --%>
            <project:status operation="${opName}" result="${opStat}" message="${errMsg}" />
            <br />
            <p class="lead">Total found: ${page.total} entries</p>
            <br />
            <table class="table">
                <thead class="table-light">
                    <tr>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="id" description="ID" /></th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="network" description="Network" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="owner" description="Owner's ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="account" description="Account ID" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="expdate" description="Expiration date" />
                        </th>
                        <th scope="col">
                            <project:sortlink page="${page}" endpoint="/cards" target="status" description="Status" />
                        </th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${page.entries}" var="card">
                    <tr>
                        <th scope="row">${card.id}</th>
                        <td><project:cardnet network="${card.network}" /> ${card.network.name}</td>
                        <td><a href="customer/${card.ownerId}">${card.ownerId}</a></td>
                        <td>${card.accountId}</td>
                        <td>${card.expDate}</td>
                        <c:if test="${card.status == 'BLOCKED'}">
                            <td class="table-danger">${card.status}</td>
                            <td>
                                <form method="POST" action="/cards?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}">
                                    <input type="hidden" name="id" value="${card.id}" />
                                    <input type="hidden" name="currentStatus" value="${card.status}" />
                                    <button type="submit" class="btn btn-outline-primary">ACTIVATE</button>
                                </form>
                            </td>
                        </c:if>
                        <c:if test="${card.status != 'BLOCKED'}">
                            <td>${card.status}</td>
                            <td>
                                <form method="POST" action="/cards?page=${page.pageNumber}&entries=${page.pageSize}&sort=${page.sort}">
                                    <input type="hidden" name="id" value="${card.id}" />
                                    <input type="hidden" name="currentStatus" value="${card.status}" />
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
                    <project:pagination page="${page}" endpoint="/cards" />
                </div>
                <div class="col-2">
                    <form method="GET" action="/cards">
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
    </body>
</html>