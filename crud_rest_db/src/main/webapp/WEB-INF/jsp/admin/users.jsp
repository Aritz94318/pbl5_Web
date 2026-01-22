<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <title>Admin - Users</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
        </head>

        <body>
            <div class="header-admin">
                <div class="header-left-admin">
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
            <div class="admin-wrap">
                <div class="admin-card">
                    <h2>
                        <i class="bi bi-people-fill"></i>
                        Users
                    </h2>
                    <form method="get" action="${pageContext.request.contextPath}/admin/users" class="users-filter-bar">
                        <label for="role" class="users-filter-label">
                            <i class="bi bi-funnel"></i> Filter by role
                        </label>

                        <select id="role" name="role" class="users-filter-select" onchange="this.form.submit()">
                            <option value="" ${empty roleFilter ? 'selected' : '' }>All</option>

                            <c:forEach var="r" items="${roles}">
                                <option value="${r}" ${roleFilter==r ? 'selected' : '' }>
                                    ${r}
                                </option>
                            </c:forEach>
                        </select>

                        <noscript>
                            <button type="submit" class="btn-admin-users">
                                Apply
                            </button>
                        </noscript>
                    </form>

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
                                            href="${pageContext.request.contextPath}/admin/users/${u.id}/edit?role=${roleFilter}">
                                            <i class="bi bi-pencil-square"></i> Edit
                                        </a>
                                        <form method="post"
                                            action="${pageContext.request.contextPath}/admin/users/${u.id}/delete?role=${roleFilter}"
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