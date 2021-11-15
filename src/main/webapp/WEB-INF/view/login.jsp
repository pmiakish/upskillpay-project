<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
    <head>
        <meta charset="UTF-8" />
        <meta content="text/html; charset=utf-8" http-equiv="content-type" />
        <meta name="description" content="UpSkillPAY - sign in" />
        <meta name="keywords" content="payment, customer, account" />
        <meta name="author" content="P. Miakish" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.1/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-F3w7mX95PdgyTmZZMECAngseQB83DfGTowi0iMjiWaeVhAn4FJkqJByhZMI3AhiU" crossorigin="anonymous">
        <title>Sign In - UpSkillPAY</title>
    </head>
    <body>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-3 align-self-center">
                    <a href="/" title="UpSkillPAY">
                        <img src="../../img/logo.png" class="img-fluid" width="150" height="91" alt="UpSkillPAY logo" />
                    </a>
                </div>
                <div class="col align-self-center">
                    <h1>Sign In:</h1>
                </div>
            </div>
        </div>
        <div class="container">
            <br />
            <div class="row">
                <div class="col-6">
                    <form action="j_security_check" method="POST">
                        <div class="mb-3">
                            <label for="loginField" class="form-label">Login (email)</label>
                            <input type="text" name="j_username" class="form-control" id="loginField" aria-describedby="emailHelp" />
                            <div id="loginInfo" class="form-text">Enter the email specified during registration</div>
                        </div>
                        <div class="mb-3">
                            <label for="passwordField" class="form-label">Password</label>
                            <input type="password" name="j_password" class="form-control" id="passwordField" />
                        </div>
                        <button type="submit" class="btn btn-primary">Submit</button>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>
