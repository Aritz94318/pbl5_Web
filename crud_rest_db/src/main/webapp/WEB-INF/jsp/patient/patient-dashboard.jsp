<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Patient Portal</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <div class="app-container">

                <div class="header-admin">
                    <div class="header-left-admin">

                        <div class="admin-shield">
                            <i class="bi bi-person-heart" aria-hidden="true"></i>
                        </div>
                        <div>
                            <h1>Mammography Patient Portal</h1>
                            <p>Your screenings & results</p>
                        </div>
                    </div>

                    <div class="header-right">
                        <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                            Logout
                        </a>
                    </div>
                </div>

                <section class="card">
                    <div class="screening-header-top patient-summary">

                        <div class="patient-avatar">
                            <i class="bi bi-person"></i>
                        </div>

                        <div>
                            <div class="text-main">
                                Welcome, ${patient.user.fullName}
                            </div>
                            <div class="text-meta">
                                Patient ID: PT-${patient.id}
                            </div>
                            <div class="text-meta">
                                Age: ${patient.age} years
                            </div>
                        </div>
                    </div>
                    <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top:20px">
                        <a class="btn-admin btn-secondary" href="${pageContext.request.contextPath}/patient/profile">
                            <i class="bi bi-person-circle" aria-hidden="true"></i>
                            My profile
                        </a>
                        <a class="btn-admin btn-secondary"
                            href="https://www.nationalbreastcancer.org/breast-cancer-resources/">
                            <i class="bi bi-question-circle" aria-hidden="true"></i>
                            Useful resources
                        </a>
                    </div>
                </section>

                <section class="kpi-grid">
                    <div class="kpi-card">
                        <div class="kpi-left">
                            <div class="kpi-icon kpi-blue">
                                <i class="bi bi-file-earmark-text" aria-hidden="true"></i>
                            </div>
                            <div class="kpi-label">Total Screenings</div>
                        </div>
                        <div class="kpi-value">${totalCount}</div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-left">
                            <div class="kpi-icon kpi-amber">
                                <i class="bi bi-clock" aria-hidden="true"></i>
                            </div>
                            <div class="kpi-label">Pending Results</div>
                        </div>
                        <div class="kpi-value">
                            <c:out value="${pendingCount}" default="0" />
                        </div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-left">
                            <div class="kpi-icon kpi-green">
                                <i class="bi bi-calendar-check" aria-hidden="true"></i>
                            </div>
                            <div class="kpi-label">Last Appointment</div>
                        </div>

                        <div class="kpi-value date">
                            <c:choose>
                                <c:when test="${not empty lastAppointmentDate}">
                                    ${lastAppointmentDate}
                                </c:when>
                                <c:otherwise>
                                    —
                                </c:otherwise>
                            </c:choose>
                        </div>

                    </div>


                </section>

                <c:if test="${pendingCount > 0}">
                    <section class="card pending-alert">
                        <div class="pending-alert-header">
                            <div class="pending-icon">
                                <i class="bi bi-clock" aria-hidden="true"></i>
                            </div>
                            <div class="pending-text">
                                <div class="pending-title">Pending Results</div>

                                <div class="pending-body">
                                    <p>
                                        You have <b>
                                            <c:out value="${pendingCount}" />
                                        </b>
                                        <c:choose>
                                            <c:when test="${pendingCount == 1}">
                                                screening
                                            </c:when>
                                            <c:otherwise>
                                                screenings
                                            </c:otherwise>
                                        </c:choose>
                                        awaiting review by your doctor. Results will be available soon.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </section>
                </c:if>
                <section class="card">
                    <div class="screening-header-top">
                        <div class="history-header">
                            <i class="bi bi-clock-history history-icon" aria-hidden="true"></i>
                            <div class="text-main history-title">
                                My Screening History
                            </div>
                        </div>
                    </div>

                    <div class="patient-list">

                        <c:forEach var="d" items="${diagnoses}">
                            <c:if test="${d.reviewed}">
                                <a class="${cardClasses}" style="text-decoration: none;"
                                    href="${pageContext.request.contextPath}/patient/diagnosis/${d.id}">

                                    <div class="history-row patient-card">
                                        <div class="history-left">
                                            <div class="history-date patient-name">
                                                ${d.dateDisplay}
                                            </div>
                                            <div class="history-sub patient-subinfo">
                                                Screening ID: ${d.id}
                                            </div>
                                        </div>

                                        <div class="history-right">
                                            <c:choose>
                                                <c:when test="${not d.patientNotified}">
                                                    <span class="status-chip">
                                                        <i class="bi bi-clock" aria-hidden="true"></i>
                                                        Pending
                                                    </span>
                                                </c:when>

                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${d.finalResult == 'MALIGNANT'}">
                                                            <span class="status-chip status-danger">
                                                                <i class="bi bi-exclamation-triangle"
                                                                    aria-hidden="true"></i>
                                                                Malignant
                                                            </span>
                                                        </c:when>

                                                        <c:when test="${d.finalResult == 'BENIGN'}">
                                                            <span class="status-chip status-success">
                                                                <i class="bi bi-check-circle" aria-hidden="true"></i>
                                                                Benign
                                                            </span>
                                                        </c:when>

                                                        <c:when test="${d.finalResult == 'INCONCLUSIVE'}">
                                                            <span class="status-chip status-warning">
                                                                <i class="bi bi-question-circle" aria-hidden="true"></i>
                                                                Inconclusive
                                                            </span>
                                                        </c:when>

                                                        <c:otherwise>
                                                            <span class="status-chip">
                                                                <i class="bi bi-clock" aria-hidden="true"></i>
                                                                Pending
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                    </div>

                                </a>

                            </c:if>
                        </c:forEach>

                        <c:if test="${reviewedCount == 0}">
                            <div class="patient-card ok-border" style="pointer-events:none; cursor:default;">
                                <div class="patient-main">
                                    <div class="patient-info">
                                        <div class="patient-name-row">
                                            <span class="patient-name">No reviewed screenings found</span>
                                        </div>
                                        <div class="patient-subinfo">
                                            <span class="dot-separator">
                                                Your reviewed screenings will appear here once your doctor reviews them.
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="patient-right">
                                    <span class="status-chip">—</span>
                                </div>
                            </div>
                        </c:if>

                    </div>

                </section>
            </div>
        </body>

        </html>