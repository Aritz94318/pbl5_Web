<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Review Portal - Diagnosis Details</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">

            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <div class="app-container">

                <!-- HEADER -->
                <header class="header">
                    <div class="header-left">
                        <div class="admin-shield">
                            <i class="bi bi-clipboard2-pulse" aria-hidden="true"></i>
                        </div>
                        <h1>Mammography Review Portal</h1>
                        <p>Diagnosis details</p>
                    </div>

                    <div class="header-right" style="display:flex; gap:10px;">
                        <a class="btn-ghost"
                            href="${pageContext.request.contextPath}/doctor/dashboard?date=${diagnosis.date}">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i>
                            Back
                        </a>
                        <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                            Logout
                        </a>
                    </div>
                </header>

                <!-- PATIENT + DIAGNOSIS CARD -->
                <section class="card">
                    <div class="screening-header-top">
                        <div>
                            <div class="text-main">
                                ${patient.user.fullName} (PT-${patient.id})
                            </div>
                            <div class="text-meta">
                                ${patient.age} years old • Birthdate: ${patient.birthDate}
                            </div>
                        </div>

                        <div class="legend">
                            <c:if test="${diagnosis.urgent}">
                                <div class="legend-item">
                                    <span class="legend-dot urgent"></span> Urgent
                                </div>
                            </c:if>
                            <c:if test="${diagnosis.reviewed}">
                                <div class="legend-item">
                                    <span class="legend-dot completed"></span> Reviewed
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <div style="margin-top:14px; display:flex; gap:14px; flex-wrap:wrap;">
                        <!-- Patient info -->
                        <div class="card" style="flex: 1; min-width: 280px;">
                            <div class="card-title">Patient information</div>
                            <div class="text-meta" style="margin-top:10px; line-height:1.8;">
                                <div><strong>Full name:</strong> ${patient.user.fullName}</div>
                                <div><strong>Email:</strong> ${patient.user.email}</div>
                                <div><strong>Username:</strong> ${patient.user.username}</div>
                                <div><strong>Patient ID:</strong> PT-${patient.id}</div>
                                <div><strong>Birthdate:</strong> ${patient.birthDate}</div>
                                <div><strong>Age:</strong> ${patient.age}</div>
                            </div>
                        </div>

                        <!-- Diagnosis info -->
                        <div class="card" style="flex: 1; min-width: 280px;">
                            <div class="card-title">Diagnosis information</div>

                            <div class="text-meta" style="margin-top:10px; line-height:1.8;">
                                <div><strong>Diagnosis ID:</strong> ${diagnosis.id}</div>
                                <div><strong>Date:</strong> ${diagnosis.date}</div>

                                <div>
                                    <strong>Status:</strong>
                                    <c:choose>
                                        <c:when test="${not diagnosis.reviewed}">
                                            Pending Review
                                        </c:when>
                                        <c:when test="${diagnosis.urgent}">
                                            Malignant
                                        </c:when>
                                        <c:otherwise>
                                            Benignant
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div><strong>Urgent:</strong> ${diagnosis.urgent}</div>
                                <div><strong>Reviewed:</strong> ${diagnosis.reviewed}</div>

                                <!-- Only show Probability if you added it to the model -->
                                <div><strong>Probability:</strong> ${diagnosis.probability}</div>

                                <div><strong>Image path:</strong> ${diagnosis.imagePath}</div>
                            </div>

                            <div style="margin-top:12px;">
                                <div class="card-title" style="font-size:14px;">Description</div>
                                <div class="text-meta" style="margin-top:6px;">
                                    ${diagnosis.description}
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- HISTORY TABLE -->
                <section class="card" style="margin-top:14px;">
                    <div class="card-title">Patient diagnosis history</div>

                    <div style="margin-top:10px; overflow:auto;">
                        <table style="width:100%; border-collapse:collapse;">
                            <thead>
                                <tr style="text-align:left;">
                                    <th style="padding:10px;">Date</th>
                                    <th style="padding:10px;">Reviewed</th>
                                    <th style="padding:10px;">Urgent</th>
                                    <th style="padding:10px;">Probability</th>
                                    <th style="padding:10px;">Open</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="h" items="${historyDiagnoses}">
                                    <tr style="border-top:1px solid rgba(0,0,0,0.06);">
                                        <td style="padding:10px;">${h.date}</td>
                                        <td style="padding:10px;">
                                            <c:choose>
                                                <c:when test="${h.reviewed}">Yes</c:when>
                                                <c:otherwise>No</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding:10px;">
                                            <c:choose>
                                                <c:when test="${h.urgent}">Yes</c:when>
                                                <c:otherwise>No</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding:10px;">${h.probability}</td>
                                        <td style="padding:10px;">
                                            <a class="btn-ghost"
                                                href="${pageContext.request.contextPath}/doctor/diagnosis/${h.id}">
                                                Open
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>

            </div>
        </body>

        </html>