<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Review Portal</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
        </head>

        <body>
            <div class="app-container">

                <!-- HEADER -->
                <header class="header">
                    <div class="header-left">
                        <h1>Mammography Review Portal</h1>
                        <p>Breast Cancer Department</p>
                    </div>
                    <div class="header-right">
                        <span class="header-badge">
                            <c:choose>
                                <c:when test="${urgentCount == 1}">
                                    1 Urgent Case Pending
                                </c:when>
                                <c:otherwise>
                                    ${urgentCount} Urgent Cases Pending
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </header>

                <!-- SELECT DATE -->
                <section class="card card-dates">
                    <div class="card-title">Select Date</div>

                    <div class="date-selector">

                        <c:forEach var="p" items="${datePills}">
                            <a href="${pageContext.request.contextPath}/doctor/dashboard?date=${p.param}">
                                <button class="date-pill ${p.active ? 'active' : ''}" type="button">
                                    <span class="label">${p.label}</span>
                                    <span class="date">${p.display}</span>
                                </button>
                            </a>
                        </c:forEach>

                    </div>
                </section>

                <!-- SCREENINGS SUMMARY + LIST -->
                <section class="card">

                    <div class="screening-header-top">
                        <div>
                            <div class="text-main">
                                Screenings for ${selectedDate}
                            </div>
                            <div class="text-meta">
                                ${totalCount} total screenings •
                                <span>${urgentCount} urgent</span> •
                                ${routineCount} routine
                            </div>
                        </div>

                        <div class="legend">
                            <div class="legend-item">
                                <span class="legend-dot urgent"></span> Urgent
                            </div>
                            <div class="legend-item">
                                <span class="legend-dot completed"></span> Completed
                            </div>
                        </div>
                    </div>

                    <!-- LIST OF SCREENINGS -->
                    <div class="patient-list">

                        <c:forEach var="d" items="${diagnoses}">

                            <c:set var="cardClasses" value="patient-card" />
                            <c:if test="${d.urgent}">
                                <c:set var="cardClasses" value="${cardClasses} urgent-border" />
                            </c:if>

                            <a class="${cardClasses}"
                                href="${pageContext.request.contextPath}/doctor/diagnosis/${d.id}">
                                <div class="patient-main">
                                    <c:if test="${d.urgent}">
                                        <div class="patient-icon">◎</div>
                                    </c:if>

                                    <div class="patient-info">
                                        <div class="patient-name-row">

                                            <span class="patient-name">
                                                ${d.patient.user.fullName}
                                            </span>

                                            <!-- <c:if test="${d.urgent}">
                                                <div class="patient-icon">◎</div>
                                            </c:if> -->

                                            <c:if test="${d.reviewed}">
                                                <span class="badge-pill badge-check">✔</span>
                                            </c:if>

                                        </div>

                                        <div class="patient-subinfo">

                                            <span>PT-${d.patient.id}</span>

                                            <span class="dot-separator">
                                                ${d.patient.age} years old
                                            </span>

                                            <span class="dot-separator">
                                                ${d.date}
                                            </span>

                                            <span class="dot-separator">
                                                <c:choose>
                                                    <c:when test="${not empty previousScreenings}">
                                                        ${previousScreenings[d.patient.id]} previous screenings
                                                    </c:when>
                                                    <c:otherwise>
                                                        previous screenings unavailable
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                        </div>
                                    </div>
                                </div>

                                <div class="patient-right">
                                    <span class="status-chip">Pending Review</span>
                                </div>
                            </a>

                        </c:forEach>

                    </div>
                </section>

            </div>
        </body>

        </html>