<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
    <head>
        <meta charset="UTF-8" />
        <meta content="text/html; charset=utf-8" http-equiv="content-type" />
        <meta name="description" content="UpSkillPAY - sign in" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${pageContext.request.contextPath}/img/favicon.ico" rel="icon" type="image/x-icon" />
        <link href="${pageContext.request.contextPath}/img/favicon-16x16.png" rel="icon" sizes="16x16" type="image/png">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>
            <c:if test="${sessionScope.sessLoc == 'RU'}">Войти - UpSkillPAY</c:if>
            <c:if test="${sessionScope.sessLoc != 'RU'}">Sign In - UpSkillPAY</c:if>
        </title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center">
                    <a href="/" title="UpSkillPAY">
                        <img src="${pageContext.request.contextPath}/img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo" />
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>
                        <c:if test="${sessionScope.sessLoc == 'RU'}">Войти:</c:if>
                        <c:if test="${sessionScope.sessLoc != 'RU'}">Sign In:</c:if>
                    </h1>
                </div>
            </div>
        </div>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-6">
                    <form action="j_security_check" method="POST">
                        <div class="mb-3">
                            <label for="loginField" class="form-label">
                                <c:if test="${sessionScope.sessLoc == 'RU'}">Логин (email)</c:if>
                                <c:if test="${sessionScope.sessLoc != 'RU'}">Login (email)</c:if>
                            </label>
                            <input type="text" name="j_username" class="form-control" id="loginField" aria-describedby="emailHelp" />
                            <div id="loginInfo" class="form-text">
                                <c:if test="${sessionScope.sessLoc == 'RU'}">Введите адрес электронной почты, указанный при регистрации</c:if>
                                <c:if test="${sessionScope.sessLoc != 'RU'}">Enter the email specified during registration</c:if>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="passwordField" class="form-label">
                                <c:if test="${sessionScope.sessLoc == 'RU'}">Пароль</c:if>
                                <c:if test="${sessionScope.sessLoc != 'RU'}">Password</c:if>
                            </label>
                            <input type="password" name="j_password" class="form-control" id="passwordField" />
                        </div>
                        <button type="submit" class="btn btn-primary">
                            <c:if test="${sessionScope.sessLoc == 'RU'}">Войти</c:if>
                            <c:if test="${sessionScope.sessLoc != 'RU'}">Submit</c:if>
                        </button>
                    </form>
                </div>
            </div>
        </div>
        <br /><br />
    </body>
</html>
