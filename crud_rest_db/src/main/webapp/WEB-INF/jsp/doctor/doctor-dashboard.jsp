<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Review Portal</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/doctor-diagnosis.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        </head>

        <body>
            <div class="app-container">

                <div class="header-admin">
                    <div class="header-left-admin">

                        <div class="admin-shield">
                            <i class="bi bi-clipboard2-pulse" aria-hidden="true"></i>
                        </div>
                        <div>
                            <h1>Mammography Review Portal</h1>
                            <p>Diagnosis details</p>
                        </div>
                    </div>

                    <div class="header-right">
                        <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                            Logout
                        </a>
                    </div>
                </div>

                <!-- SELECT DATE -->
                <section class="card card-dates">
                    <div class="card-title">Select Date</div>

                    <div class="date-selector-row">

                        <!-- Pills (last 6 days) -->
                        <div class="date-selector">
                            <c:forEach var="p" items="${datePills}">
                                <a
                                    href="${pageContext.request.contextPath}/doctor/dashboard?date=${p.param}&status=${statusFilter}&result=${resultFilter}">
                                    <button class="date-pill ${p.active ? 'active' : ''}" type="button">
                                        <span class="label">${p.label}</span>
                                        <span class="date">${p.display}</span>
                                    </button>
                                </a>
                            </c:forEach>
                        </div>

                        <!-- Calendar (jump to any date) -->
                        <div class="date-picker">
                            <label class="date-picker-label" for="jumpDate">
                                <i class="bi bi-calendar3"></i>
                                Jump to date
                            </label>

                            <input id="jumpDate" class="date-input" type="date" value="${selectedDateIso}" />
                        </div>

                    </div>
                </section>

                <script>
                    (function () {
                        const input = document.getElementById('jumpDate');
                        if (!input) return;

                        input.addEventListener('change', function () {
                            if (!this.value) return;
                            const base = '${pageContext.request.contextPath}/doctor/dashboard?status=${statusFilter}&result=${resultFilter}&date=';
                            window.location.href = base + encodeURIComponent(this.value);
                        });
                    })();
                </script>

                <!-- SCREENINGS SUMMARY + LIST -->
                <section class="card">

                    <div class="screening-header-top">
                        <div>
                            <div class="text-main">
                                Screenings for ${selectedDate}
                            </div>

                            <div class="text-meta">
                                ${totalCount} total

                                •
                                <c:choose>
                                    <c:when test="${urgentCount > 0}">
                                        <span>${urgentCount} urgent</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span>0 urgent</span>
                                    </c:otherwise>
                                </c:choose>

                                •
                                <c:choose>
                                    <c:when test="${malignantCount > 0}">
                                        ${malignantCount} malignant
                                    </c:when>
                                    <c:otherwise>
                                        0 malignant
                                    </c:otherwise>
                                </c:choose>

                                •
                                <c:choose>
                                    <c:when test="${inconclusiveCount > 0}">
                                        ${inconclusiveCount} inconclusive
                                    </c:when>
                                    <c:otherwise>
                                        0 inconclusive
                                    </c:otherwise>
                                </c:choose>

                                •
                                <c:choose>
                                    <c:when test="${benignCount > 0}">
                                        ${benignCount} benign
                                    </c:when>
                                    <c:otherwise>
                                        0 benign
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="legend">
                            <div class="legend-item legend-urgent">
                                <span class="patient-icon patient-icon--small">◎</span>
                                <span>Urgent</span>
                            </div>

                            <div class="legend-item legend-completed">
                                <span class="badge-pill badge-check">✔</span>
                                <!-- <i class="bi bi-check-circle-fill legend-check"></i> -->
                                <span>Completed</span>
                            </div>

                            <div class="legend-item">
                                <span class="legend-dot danger"></span>
                                <span>Malignant</span>
                            </div>

                            <div class="legend-item">
                                <span class="legend-dot success"></span>
                                <span>Benign</span>
                            </div>

                            <div class="legend-item">
                                <span class="legend-dot warning"></span>
                                <span>Inconclusive</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="filters-row">
                        <!-- Status filter -->
                        <div class="filter-group">
                            <span class="filter-label">Status:</span>

                            <a class="filter-chip ${statusFilter == 'ALL' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=ALL&result=${resultFilter}">
                                All
                            </a>

                            <a class="filter-chip ${statusFilter == 'PENDING' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=PENDING&result=${resultFilter}">
                                Pending
                            </a>

                            <a class="filter-chip ${statusFilter == 'REVIEWED' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=REVIEWED&result=${resultFilter}">
                                Reviewed
                            </a>
                        </div>

                        <!-- Result filter -->
                        <div class="filter-group">
                            <span class="filter-label">Result:</span>

                            <a class="filter-chip ${resultFilter == 'ALL' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=${statusFilter}&result=ALL">
                                All
                            </a>

                            <a class="filter-chip ${resultFilter == 'MALIGNANT' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=${statusFilter}&result=MALIGNANT">
                                Malignant
                            </a>

                            <a class="filter-chip ${resultFilter == 'BENIGN' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=${statusFilter}&result=BENIGN">
                                Benign
                            </a>

                            <a class="filter-chip ${resultFilter == 'INCONCLUSIVE' ? 'active' : ''}"
                                href="${pageContext.request.contextPath}/doctor/dashboard?date=${selectedDateIso}&status=${statusFilter}&result=INCONCLUSIVE">
                                Inconclusive
                            </a>
                        </div>

                        <div class="filter-meta">
                            Showing ${filteredCount} of ${totalCount}
                        </div>
                    </div>
                    <!-- LIST OF SCREENINGS -->
                    <div class="patient-list">

                        <c:forEach var="d" items="${diagnoses}">

                            <c:set var="cardClasses" value="patient-card" />
                            <c:choose>
                                <c:when test="${d.urgent}">
                                    <c:set var="cardClasses" value="${cardClasses} urgent-border" />
                                </c:when>
                                <c:when test="${d.aiPrediction == 'PENDING' && d.reviewed == false}">
                                    <c:set var="cardClasses" value="${cardClasses} pending-border" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="cardClasses" value="${cardClasses} ok-border" />
                                </c:otherwise>
                            </c:choose>


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
                                                        ${previousScreenings[d.patient.id]} total screenings
                                                    </c:when>
                                                    <c:otherwise>
                                                        Total screenings unavailable
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                        </div>
                                    </div>
                                </div>

                                <div class="patient-right">
                                    <c:choose>

                                        <c:when test="${not d.reviewed}">
                                            <span class="status-chip bi bi-clock">Pending Review</span>
                                        </c:when>

                                        <c:when test="${empty d.finalResult}">
                                            <span class="status-chip bi bi-clock">Pending Result</span>
                                        </c:when>

                                        <c:when test="${d.finalResult == 'MALIGNANT'}">
                                            <span
                                                class="status-chip status-danger bi bi-exclamation-triangle">Malignant</span>
                                        </c:when>

                                        <c:when test="${d.finalResult == 'BENIGN'}">
                                            <span class="status-chip status-success bi bi-check-circle">Benign</span>
                                        </c:when>

                                        <c:when test="${d.finalResult == 'INCONCLUSIVE'}">
                                            <span
                                                class="status-chip status-warning bi bi-exclamation-triangle">Inconclusive</span>
                                        </c:when>

                                        <c:otherwise>
                                            <span class="status-chip">Unknown</span>
                                        </c:otherwise>

                                    </c:choose>
                                </div>


                            </a>

                        </c:forEach>

                    </div>
                </section>

            </div>
        </body>

        </html>