<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="project" uri="/WEB-INF/upskill.tld" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="description" content="UpSkillPAY - регистрация" />
        <meta name="keywords" content="платеж, клиент, счет" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Регистрация - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center">
                    <a href="/" title="UpSkillPAY">
                        <img src="../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY лого">
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Добро пожаловать в UpSkillPAY!</h1>
                </div>
                <div class="col-3">
                    &nbsp;
                </div>
            </div>
        </div>
        <div class="container">
            <p>
                <%-- Operation status message --%>
                <project:status operation="${opName}" result="${opStat}" message="${errMsg}" locale="${sessionScope.sessLoc}" />
                <br />
                <c:if test="${opStat == true}">
                    <br />
                    Теперь вы можете <a href="/payservice/my_accounts">войти в систему и воспользоваться сервисами клиента</a>
                </c:if>
            </p>
        </div>
        <br /><br />
    </body>
</html>