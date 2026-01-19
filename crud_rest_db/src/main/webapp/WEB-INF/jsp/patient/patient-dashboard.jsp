<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Mammography Patient Portal</title>

    <!-- Base global styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">

    <!-- Reuse dashboard styles (same look & feel) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">

    <!-- Patient-specific styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-dashboard.css?v=5">

    <!-- Bootstrap Icons (icons only) -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>

<body>
<div class="app-container">

    <!-- HEADER -->
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

    <!-- PATIENT SUMMARY -->
    <section class="card">
        <div class="screening-header-top patient-summary">

            <!-- AVATAR -->
            <div class="patient-avatar">
                <i class="bi bi-person"></i>
            </div>

            <!-- TEXT -->
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
    </section>

    <!-- KPI CARDS -->
    <section class="kpi-grid">

        <!-- Total Screenings -->
        <div class="kpi-card">
            <div class="kpi-left">
                <div class="kpi-icon kpi-blue">
                    <i class="bi bi-file-earmark-text" aria-hidden="true"></i>
                </div>
                <div class="kpi-label">Total Screenings</div>
            </div>
            <div class="kpi-value">${totalCount}</div>
        </div>

        <!-- Pending Results -->
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

        <!-- Upcoming Appointments -->
        <div class="kpi-card">
            <div class="kpi-left">
                <div class="kpi-icon kpi-green">
                    <i class="bi bi-calendar2-week" aria-hidden="true"></i>
                </div>
                <div class="kpi-label">Upcoming Appointments</div>
            </div>
            <div class="kpi-value">
                <c:out value="${upcomingCount}" default="0" />
            </div>
        </div>

    </section>

    <!-- PENDING RESULTS ALERT -->
    <section class="card pending-alert">
        <div class="pending-alert-header">
            <div class="pending-icon">
                <i class="bi bi-clock" aria-hidden="true"></i>
            </div>

            <!-- TEXT WRAPPER -->
            <div class="pending-text">
                <div class="pending-title">Pending Results</div>

                <div class="pending-body">
                    <p>
                        You have <b><c:out value="${pendingCount}" default="0" /></b>
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


    <!-- HISTORY SUMMARY -->
    <section class="card">
        <div class="screening-header-top">
            <div class="history-header">
                <i class="bi bi-clock-history history-icon" aria-hidden="true"></i>
                <div class="text-main history-title">
                    My Screening History
                </div>
            </div>
        </div>

        <!-- LIST OF SCREENINGS (ONLY REVIEWED) -->
        <div class="patient-list">

            <c:forEach var="d" items="${diagnoses}">
                <c:if test="${d.reviewed}">

                    <c:set var="cardClasses" value="patient-card" />
                    <c:choose>
                        <c:when test="${d.urgent}">
                            <c:set var="cardClasses" value="${cardClasses} urgent-border" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="cardClasses" value="${cardClasses} ok-border" />
                        </c:otherwise>
                    </c:choose>

                    <a class="${cardClasses}" href="${pageContext.request.contextPath}/patient/diagnosis/${d.id}">

                        <div class="history-row">
                            <!-- LEFT -->
                            <div class="history-left">
                                <div class="history-date">
                                    ${d.dateDisplay}
                                </div>
                                <div class="history-sub">
                                    Screening ID: ${d.id}
                                </div>
                            </div>

                            <!-- RIGHT -->
                            <div class="history-right">
                                <c:choose>
                                    <c:when test="${d.urgent}">
                                        <span class="history-badge history-badge-danger">
                                            <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                            Positive
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="history-badge history-badge-success">
                                            <i class="bi bi-check-circle" aria-hidden="true"></i>
                                            Negative
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                    </a>

                </c:if>
            </c:forEach>

            <!-- Empty state: no reviewed screenings -->
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

    <!-- OPTIONAL: QUICK ACTIONS -->
    <section class="card">
        <div class="card-title">Quick actions</div>
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
            <a class="btn-ghost" href="${pageContext.request.contextPath}/patient/profile">
                <i class="bi bi-person-circle" aria-hidden="true"></i>
                My profile
            </a>
            <a class="btn-ghost" href="${pageContext.request.contextPath}/patient/messages">
                <i class="bi bi-chat-dots" aria-hidden="true"></i>
                Messages
            </a>
            <a class="btn-ghost" href="${pageContext.request.contextPath}/patient/help">
                <i class="bi bi-question-circle" aria-hidden="true"></i>
                Help
            </a>
        </div>
    </section>

</div>
</body>

</html>
