<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html>

        <head>
            <meta charset="UTF-8">
            <title>Pink Alert - Login</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/login.css">

        </head>

        <body class="bg-gradient">

            <div class="top-brand">
                <img src="${pageContext.request.contextPath}/static/img/logo.png" alt="" class="logo">
                <h2 class="brand-title">Pink Alert</h2>
                <p class="brand-subtitle">Secure access to mammography screening results</p>
            </div>

            <div class="login-card">
                <!-- <div class="icon-circle">
                    <span class="icon">🩺</span>
                </div> -->

                <h3 class="login-title">Login</h3>
                <!-- <p class="login-subtitle">Access your medical review portal</p> -->

                <!-- Error message -->
                <c:if test="${not empty error}">
                    <div class="error-box">${error}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post" class="form">

                    <div class="form-group">
                        <label>Email / Username</label>
                        <input type="text" name="username" required placeholder="Enter your email or username">
                    </div>

                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" required placeholder="Enter your password">
                    </div>

                    <button type="submit" class="login-btn">Log In</button>

                </form>

                <!-- <a href="${pageContext.request.contextPath}/" class="back-link">Back</a> -->
            </div>

        </body>

        </html>