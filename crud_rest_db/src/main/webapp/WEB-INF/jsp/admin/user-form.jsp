<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html>

        <head>
            <title>Admin - User Form</title>
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
                    <h2><i class="bi bi-person-gear"></i> User</h2>

                    <c:set var="isEdit" value="${not empty user.id}" />

                    <form method="post"
                        action="${pageContext.request.contextPath}<c:choose><c:when test='${isEdit}'>/admin/users/${user.id}</c:when><c:otherwise>/admin/users</c:otherwise></c:choose>">

                        <div style="display:grid; gap:10px; max-width:520px;">
                            <label>
                                Username
                                <input name="username" value="<c:out value='${user.username}'/>" required
                                    style="width:100%; padding:10px;">
                            </label>

                            <label>
                                Full name
                                <input name="fullName" value="<c:out value='${user.fullName}'/>" required
                                    style="width:100%; padding:10px;">
                            </label>

                            <label>
                                Email
                                <input type="email" name="email" value="<c:out value='${user.email}'/>" required
                                    style="width:100%; padding:10px;">
                            </label>


                            <label>
                                Role
                                <select name="role" required style="width:100%; padding:10px;">
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r}" <c:if test="${user.role == r}">selected</c:if>>
                                            <c:out value="${r}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>

                            <label>
                                Password
                                <input type="password" name="rawPassword" placeholder="Set / change password"
                                    style="width:100%; padding:10px;">
                            </label>


                            <div class="admin-tools">
                                <button class="btn-admin btn-primary" type="submit">
                                    <i class="bi bi-check2-circle"></i> Save
                                </button>
                                <a class="btn-admin" href="${pageContext.request.contextPath}/admin/dashboard">
                                    <i class="bi bi-x-circle"></i> Cancel
                                </a>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

        </body>

        </html>