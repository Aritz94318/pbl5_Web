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
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">

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
                        <input type="hidden" name="roleFilter" value="${roleFilter}" />
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

                            <label id="doctorFields" style="display:none;">
                                Doctor phone
                                <input name="doctorPhone"
                                    value="<c:out value='${user.doctor != null ? user.doctor.phone : ""}'/>"
                                    style="width:100%; padding:10px;">
                            </label>

                            <div id="patientFields" style="display:none; display:grid; gap:10px;">
                                <label>
                                    Patient birth date
                                    <input type="date" name="patientBirthDate"
                                        value="<c:out value='${user.patient != null ? user.patient.birthDate : ""}'/>"
                                        style="width:100%; padding:10px;">
                                </label>

                                <label>
                                    Patient phone
                                    <input name="patientPhone"
                                        value="<c:out value='${user.patient != null ? user.patient.phone : ""}'/>"
                                        style="width:100%; padding:10px;">
                                </label>
                            </div>

                            <div class="admin-tools">
                                <button class="btn-admin btn-primary" type="submit">
                                    <i class="bi bi-check2-circle"></i> Save
                                </button>
                                <a class="btn-admin btn-secondary"
                                    href="${pageContext.request.contextPath}/admin/dashboard">
                                    <i class="bi bi-x-circle"></i> Cancel
                                </a>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <script>
                (function () {
                    const roleSel = document.querySelector("select[name='role']");
                    const doctorFields = document.getElementById("doctorFields");
                    const patientFields = document.getElementById("patientFields");

                    function refresh() {
                        const role = roleSel.value;
                        doctorFields.style.display = (role === "DOCTOR") ? "block" : "none";
                        patientFields.style.display = (role === "PATIENT") ? "grid" : "none";
                    }

                    roleSel.addEventListener("change", refresh);
                    refresh(); // initial
                })();
            </script>

        </body>

        </html>