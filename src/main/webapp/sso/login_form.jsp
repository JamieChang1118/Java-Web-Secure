<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
        <link rel="stylesheet" href="https://unpkg.com/purecss@2.0.3/build/pure-min.css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Form</title>
        <script src="https://www.google.com/recaptcha/api.js?render=6LcOcdYZAAAAAEEEBylqGG6cYChaA8RA9C_0VgaD"></script>
        <script>
            function init() {
                grecaptcha.ready(function () {
                    grecaptcha.execute('6LcOcdYZAAAAAEEEBylqGG6cYChaA8RA9C_0VgaD', {action: 'submit'}).then(function (token) {
                        document.getElementById('g-recaptcha-response').value = token;
                    });
                });
            }
        </script>
    </head>
    <body style="padding: 20px" onload="init()">

        <form class="pure-form" method="post" action="${pageContext.request.contextPath}/sso/login">
            <fieldset>
                <legend>Login Form</legend>
                <input type="text" id="g-recaptcha-response" name="g-recaptcha-response"><p />
                <input type="text" placeholder="Username" name="username" /><p />
                <input type="text" placeholder="Password" name="password" /> <p />
                符合:至少一個大寫字母 至少一個小寫字母 至少一個數字 至少一個特殊符號 長度至少8~64個字元, SHA-256 + salt<p />
                <button type="submit" class="pure-button pure-button-primary">Sign in</button>
            </fieldset>
        </form>

    </body>
</html>
