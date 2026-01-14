<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <title>Admin - Users</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">

            <!-- Bootstrap Icons -->
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

            <!-- <style>
                /* Back arrow button */
                .btn-back-icon {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 14px;
                    color: #fff;
                    text-decoration: none;
                }

                .btn-back-icon i {
                    font-size: 1.6rem;
                    opacity: .9;
                    transition: transform .15s ease, opacity .15s ease;
                }

                .btn-back-icon:hover i {
                    transform: translateX(-2px);
                    opacity: 1;
                }
            </style> -->
        </head>

        <body>

            <!-- TOP BAR -->
            <div class="header-admin">

                <div class="header-left-admin">

                    <!-- BACK TO DASHBOARD -->
                    <a class="btn-back-icon" href="${pageContext.request.contextPath}/admin/dashboard"
                        title="Back to Dashboard">
                        <i class="bi bi-arrow-left-circle-fill"></i>
                    </a>

                    <div class="admin-shield">
                        <i class="bi bi-shield-lock"></i>
                    </div>

                    <div>
                        <h1>Administrative Dashboard</h1>
                        <p>System Analytics &amp; Management</p>
                    </div>
                </div>

                <div class="header-right">
                    <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                        <i class="bi bi-box-arrow-right"></i>
                        Logout
                    </a>
                </div>

            </div>

            <!-- CONTENT -->
            <div class="admin-wrap">
                <div class="admin-card">

                    <h2>
                        <i class="bi bi-people-fill"></i>
                        Users
                    </h2>

                    <table class="users-table" style="width:100%;">
                        <thead>
                            <tr style="text-align:left;">
                                <th>ID</th>
                                <th>Full name</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th style="width:220px;">Actions</th>
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
                                    <td>
                                        <a class="btn-admin-users"
                                            href="${pageContext.request.contextPath}/admin/users/${u.id}/edit">
                                            <i class="bi bi-pencil-square"></i> Edit
                                        </a>

                                        <form method="post"
                                            action="${pageContext.request.contextPath}/admin/users/${u.id}/delete"
                                            style="display:inline;">
                                            <button class="btn-admin-users" type="submit"
                                                onclick="return confirm('Delete this user?');">
                                                <i class="bi bi-trash3"></i> Delete
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                </div>
            </div>

        </body>

        </html>