<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html>

        <head>
            <title>Admin -
                <c:out value="${title}" />
            </title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <!-- TOP BAR -->
            <div class="header-admin">
                <div class="header-left-admin">
                    <div class="admin-shield">
                        <i class="bi bi-shield-lock" aria-hidden="true"></i>
                    </div>
                    <div>
                        <h1>Administrative Dashboard</h1>
                        <p>System Analytics &amp; Management</p>
                    </div>
                </div>

                <div class="header-right">
                    <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                        <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                        Logout
                    </a>
                </div>
            </div>
            <div class="admin-wrap">
                <div class="admin-card">
                    <h2><i class="bi bi-person-lines-fill"></i>
                        <c:out value="${title}" />
                    </h2>

                    <div class="admin-tools" style="margin-bottom:12px;">
                        <a class="btn-admin" href="${pageContext.request.contextPath}/admin/users">
                            <i class="bi bi-people-fill"></i> All Users
                        </a>
                        <a class="btn-admin" href="${pageContext.request.contextPath}/admin/dashboard">
                            <i class="bi bi-speedometer2"></i> Dashboard
                        </a>
                    </div>

                    <table style="width:100%; border-collapse:collapse;">
                        <thead>
                            <tr style="text-align:left;">
                                <th>ID</th>
                                <th>Full name</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="u" items="${users}">
                                <tr>
                                    <td>
                                        <c:out value="${u.id}" />
                                    </td>
                                    <td>
                                        <c:out value="${u.fullName}" />
                                    </td>
                                    <td>
                                        <c:out value="${u.username}" />
                                    </td>
                                    <td>
                                        <c:out value="${u.email}" />
                                    </td>
                                    <td>
                                        <c:out value="${u.role}" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                </div>
            </div>

        </body>

        </html>