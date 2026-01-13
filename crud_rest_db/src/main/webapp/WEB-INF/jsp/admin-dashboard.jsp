<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Pink Alert - Admin Dashboard</title>

            <!-- Base global styles -->
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">

            <!-- Admin dashboard styles -->
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">

            <!-- Bootstrap Icons (icons only) -->
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

            <!-- Optional: make icons align nicely inside your buttons/cards -->
        </head>

        <body>

            <%--=========================SAFE DEFAULTS (EL/JSTL)=========================--%>

                <c:choose>
                    <c:when test="${not empty totalPatients}">
                        <c:set var="totalPatientsSafe" value="${totalPatients}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="totalPatientsSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty totalScreenings}">
                        <c:set var="totalScreeningsSafe" value="${totalScreenings}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="totalScreeningsSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty completionRate}">
                        <c:set var="completionRateSafe" value="${completionRate}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="completionRateSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty urgentCases}">
                        <c:set var="urgentCasesSafe" value="${urgentCases}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="urgentCasesSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty positiveRate}">
                        <c:set var="positiveRateSafe" value="${positiveRate}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="positiveRateSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty negativeCount}">
                        <c:set var="negativeSafe" value="${negativeCount}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="negativeSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty positiveCount}">
                        <c:set var="positiveSafe" value="${positiveCount}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="positiveSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty pendingCount}">
                        <c:set var="pendingSafe" value="${pendingCount}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="pendingSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty inconclusiveCount}">
                        <c:set var="inconclusiveSafe" value="${inconclusiveCount}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="inconclusiveSafe" value="0" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty timelineLabelsJs}">
                        <c:set var="labelsJs" value="${timelineLabelsJs}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="labelsJs" value="" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty timelineTotalJs}">
                        <c:set var="totalJs" value="${timelineTotalJs}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="totalJs" value="" />
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty timelineCompletedJs}">
                        <c:set var="completedJs" value="${timelineCompletedJs}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="completedJs" value="" />
                    </c:otherwise>
                </c:choose>

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

                    <!-- KPI CARDS -->
                    <div class="kpi-grid">
                        <div class="kpi-card">
                            <div class="kpi-top">
                                <div class="kpi-icon icon-blue">
                                    <i class="bi bi-people" aria-hidden="true"></i>
                                </div>
                                <div class="kpi-value">
                                    <c:out value="${totalPatientsSafe}" />
                                </div>
                            </div>
                            <div class="kpi-label">Total Patients</div>
                            <div class="kpi-sub">Unique patients registered</div>
                        </div>

                        <div class="kpi-card">
                            <div class="kpi-top">
                                <div class="kpi-icon icon-green">
                                    <i class="bi bi-file-earmark-medical" aria-hidden="true"></i>
                                </div>
                                <div class="kpi-value">
                                    <c:out value="${totalScreeningsSafe}" />
                                </div>
                            </div>
                            <div class="kpi-label">Total Screenings</div>
                            <div class="kpi-sub">
                                <c:out value="${completionRateSafe}" />% completion rate
                            </div>
                        </div>

                        <div class="kpi-card">
                            <div class="kpi-top">
                                <div class="kpi-icon icon-red">
                                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                </div>
                                <div class="kpi-value">
                                    <c:out value="${urgentCasesSafe}" />
                                </div>
                            </div>
                            <div class="kpi-label">Urgent Cases</div>
                            <div class="kpi-sub">Require immediate attention</div>
                        </div>

                        <div class="kpi-card">
                            <div class="kpi-top">
                                <div class="kpi-icon icon-purple">
                                    <i class="bi bi-graph-up-arrow" aria-hidden="true"></i>
                                </div>
                                <div class="kpi-value">
                                    <c:out value="${positiveRateSafe}" />%
                                </div>
                            </div>
                            <div class="kpi-label">Positive Rate</div>
                            <div class="kpi-sub">Of completed screenings</div>
                        </div>
                    </div>

                    <!-- USER MANAGEMENT BUTTONS -->
                    <div class="admin-card" style="margin-top:18px;">
                        <h2>User Management</h2>
                        <div class="admin-tools">
                            <a class="btn-admin" href="${pageContext.request.contextPath}/admin/users">
                                <i class="bi bi-people-fill" aria-hidden="true"></i>
                                View Users
                            </a>
                            <a class="btn-admin" href="${pageContext.request.contextPath}/admin/users/new">
                                <i class="bi bi-person-plus-fill" aria-hidden="true"></i>
                                Add User
                            </a>
                            <a class="btn-admin" href="${pageContext.request.contextPath}/">
                                <i class="bi bi-heart-pulse-fill" aria-hidden="true"></i>
                                Simulation
                            </a>
                        </div>
                    </div>

                    <!-- CHARTS -->
                    <div class="admin-grid-2">
                        <div class="admin-card">
                            <h2>Results Distribution</h2>
                            <div class="chart-wrap">
                                <canvas id="resultsChart"></canvas>
                            </div>
                        </div>

                        <div class="admin-card">
                            <h2>Screenings Timeline</h2>
                            <div class="chart-wrap">
                                <canvas id="timelineChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Chart.js -->
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

                <script>
                    const resultsData = {
                        negative: Number("<c:out value='${negativeSafe}'/>"),
                        positive: Number("<c:out value='${positiveSafe}'/>"),
                        pending: Number("<c:out value='${pendingSafe}'/>"),
                        inconclusive: Number("<c:out value='${inconclusiveSafe}'/>")
                    };

                    const resultsCtx = document.getElementById('resultsChart');
                    new Chart(resultsCtx, {
                        type: 'pie',
                        data: {
                            labels: ['Negative', 'Positive', 'Pending', 'Inconclusive'],
                            datasets: [{
                                data: [resultsData.negative, resultsData.positive, resultsData.pending, resultsData.inconclusive],
                                borderWidth: 1
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: { legend: { position: 'bottom' } }
                        }
                    });

                    const labels = [<c:out value="${labelsJs}" escapeXml="false" />];
                    const total = [<c:out value="${totalJs}" escapeXml="false" />];
                    const completed = [<c:out value="${completedJs}" escapeXml="false" />];

                    const timelineCtx = document.getElementById('timelineChart');
                    new Chart(timelineCtx, {
                        type: 'line',
                        data: {
                            labels: labels,
                            datasets: [
                                { label: 'Completed', data: completed, tension: 0.35 },
                                { label: 'Total Screenings', data: total, tension: 0.35 }
                            ]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: { legend: { position: 'bottom' } },
                            scales: { y: { beginAtZero: true } }
                        }
                    });
                </script>

        </body>

        </html>