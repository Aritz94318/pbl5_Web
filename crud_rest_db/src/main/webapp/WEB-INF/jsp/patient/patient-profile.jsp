<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>My Profile - Mammography Patient Portal</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <div class="app-container">

                <!-- HEADER -->
                <div class="header-admin">
                    <div class="header-left-admin">
                        <a class="btn-back-icon"
                            href="${pageContext.request.contextPath}/patient/dashboard"
                            title="Back to Dashboard">
                            <i class="bi bi-arrow-left-circle-fill"></i>
                        </a>
                        <div class="admin-shield">
                            <i class="bi bi-person-heart" aria-hidden="true"></i>
                        </div>
                        <div>
                            <h1>Mammography Patient Portal</h1>
                            <p>My profile</p>
                        </div>
                    </div>

                    <div class="header-right">
                        <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                            Logout
                        </a>
                    </div>
                </div>

                <c:if test="${not empty error}">
                    <section class="card" style="border-left: 4px solid #d62a2a;">
                        <div class="text-main">Error</div>
                        <div class="text-meta">
                            <c:out value="${error}" />
                        </div>
                    </section>
                </c:if>

                <!-- PROFILE SUMMARY -->
                <section class="card">
                    <div class="screening-header-top patient-summary">
                        <div class="patient-avatar">
                            <i class="bi bi-person"></i>
                        </div>

                        <div>
                            <div class="text-main">
                                <c:out value="${patient.user.fullName}" />
                            </div>
                            <div class="text-meta">
                                Patient ID: PT-
                                <c:out value="${patient.id}" />
                            </div>
                            <div class="text-meta">
                                Age:
                                <c:out value="${patient.age}" />
                            </div>
                        </div>
                    </div>

                    <div class="text-meta" style="margin-top:16px;">
                        This page is read-only. If you need to update your information, contact the medical team at
                        <a href="mailto:pinkalert@gmail.com
    ?subject=Pink Alert%20-%20Profile%20Update%20Request
    &body=Hello%20Pink%20Alert%20team,%0A%0AI%20would%20like%20to%20request%20an%20update%20to%20my%20profile.%0A%0AThank%20you."
                            class="contact-link">
                            pinkalert@gmail.com
                        </a>
                    </div>

                </section>

                <!-- ACCOUNT INFO -->
                <section class="card">
                    <div class="card-title">
                        <i class="bi bi-person-badge" aria-hidden="true"></i>
                        Account information
                    </div>

                    <div style="display:grid; gap:14px; max-width:720px; margin-top:20px">
                        <div>
                            <div class="info-label text-main">Full name</div>
                            <div class="info-value text-meta">
                                <c:out value="${patient.user.fullName}" default="—" />
                            </div>
                        </div>

                        <div>
                            <div class="info-label text-main">Username</div>
                            <div class="info-value text-meta">
                                <c:out value="${patient.user.username}" default="—" />
                            </div>
                        </div>

                        <div>
                            <div class="info-label text-main">Email</div>
                            <div class="info-value text-meta">
                                <c:choose>
                                    <c:when test="${not empty patient.user.email}">
                                        <c:out value="${patient.user.email}" />
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div>
                            <div class="info-label text-main">Role</div>
                            <div class="info-value text-meta">
                                <c:out value="${patient.user.role}" default="—" />
                            </div>
                        </div>
                    </div>
                </section>

                <!-- PATIENT DETAILS -->
                <section class="card">
                    <div class="card-title">
                        <i class="bi bi-clipboard2-heart" aria-hidden="true"></i>
                        Patient details
                    </div>

                    <div style="display:grid; gap:14px; max-width:720px; margin-top:20px">
                        <div>
                            <div class="info-label text-main">Birth date</div>
                            <div class="info-value  text-meta">
                                <c:choose>
                                    <c:when test="${not empty patient.birthDate}">
                                        <c:out value="${patient.birthDate}" />
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div>
                            <div class="info-label text-main">Phone</div>
                            <div class="info-value text-meta">
                                <c:choose>
                                    <c:when test="${not empty patient.phone}">
                                        <c:out value="${patient.phone}" />
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </section>

            </div>
        </body>

        </html>