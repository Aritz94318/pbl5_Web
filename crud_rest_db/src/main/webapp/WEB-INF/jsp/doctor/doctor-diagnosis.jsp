<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Review Portal - Diagnosis Details</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/doctor-diagnosis.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <div class="app-container">

                <div class="header-admin">
                    <div class="header-left-admin">
                        <a class="btn-back-icon"
                            href="${pageContext.request.contextPath}/doctor/dashboard?date=${diagnosis.date}"
                            title="Back to Dashboard">
                            <i class="bi bi-arrow-left-circle-fill"></i>
                        </a>

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

                <div class="page-wrap">
                    <div class="content-grid">

                        <!-- LEFT COLUMN -->
                        <!-- LEFT COLUMN -->
                        <div>
                            <div class="panel">
                                <div class="panel-title">
                                    <i class="bi bi-person"></i>
                                    <span>Patient Information</span>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Full Name</div>
                                    <div class="info-value">${patient.user.fullName}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Patient ID</div>
                                    <div class="info-value">PT-${patient.id}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Age</div>
                                    <div class="info-value">${patient.age} years</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Email</div>
                                    <div class="info-value">${patient.user.email}</div>
                                </div>

                                <div class="info-row" style="margin-bottom:0;">
                                    <c:choose>
                                        <c:when test="${not empty previousScreenings}">
                                            ${previousScreenings[d.patient.id]} total screenings
                                        </c:when>
                                        <c:otherwise>
                                            Total screenings unavailable
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="panel">
                                <div class="panel-title">
                                    <i class="bi bi-calendar-event"></i>
                                    <span>Screening Details</span>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Date</div>
                                    <div class="info-value">${diagnosis.date}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Diagnosis ID</div>
                                    <div class="info-value">${diagnosis.id}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Status</div>
                                    <div class="info-value">
                                        <c:choose>
                                            <c:when test="${not diagnosis.reviewed}">
                                                <span class="warn">Pending Review</span>
                                            </c:when>
                                            <c:when test="${diagnosis.urgent}">
                                                <span class="danger">Malignant</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ok">Benignant</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <div class="chip-row">
                                    <c:if test="${diagnosis.urgent}">
                                        <span class="chip urgent"><i class="bi bi-exclamation-triangle"></i>
                                            Urgent</span>
                                    </c:if>
                                    <c:if test="${diagnosis.reviewed}">
                                        <span class="chip reviewed"><i class="bi bi-check-circle"></i> Reviewed</span>
                                    </c:if>
                                </div>

                                <div class="info-row" style="margin-top:14px; margin-bottom:0;">
                                    <div class="info-label">Probability (malignant)</div>
                                    <div class="info-value">
                                        <c:out value="${diagnosis.probability}" default="—" />
                                    </div>
                                </div>
                            </div>

                            <!-- HISTORY TABLE (NOW ON THE LEFT, BELOW SCREENING DETAILS) -->
                            <div class="panel" style="margin-top:14px;">
                                <div class="panel-title">
                                    <i class="bi bi-clock-history"></i>
                                    <span>Patient diagnosis history</span>
                                </div>

                                <div class="table-wrap">
                                    <table class="hist" style="min-width: 0;">
                                        <thead>
                                            <tr>
                                                <th>Date</th>
                                                <!-- <th>Reviewed</th>
                                                <th>Urgent</th>
                                                <th>Probability</th> -->
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="h" items="${historyDiagnoses}">
                                                <tr>
                                                    <td>${h.date}</td>
                                                    <!-- <td>
                                                        <c:choose>
                                                            <c:when test="${h.reviewed}">Yes</c:when>
                                                            <c:otherwise>No</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${h.urgent}">Yes</c:when>
                                                            <c:otherwise>No</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:out value="${h.probability}" default="—" />
                                                    </td> -->
                                                    <td>
                                                        <a class="btn-soft"
                                                            href="${pageContext.request.contextPath}/doctor/diagnosis/${h.id}">
                                                            <i class="bi bi-box-arrow-up-right"></i>
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <!-- RIGHT COLUMN -->
                        <div>
                            <!-- Mammography Images (4) -->
                            <div class="panel">
                                <div class="panel-title">
                                    <i class="bi bi-image"></i>
                                    <span>Mammography Images</span>
                                </div>

                                <div class="images-grid">

                                    <!-- Image 1 -->
                                    <div class="image-card">
                                        <c:choose>
                                            <c:when test="${not empty diagnosis.previewPath}">
                                                <img class="preview-img"
                                                    src="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                    alt="Mammography preview 1" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="image-tile">
                                                    Preview not generated yet.<br />
                                                    Use Open/Download.
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="image-meta">
                                            <div class="image-actions">
                                                <c:if test="${not empty diagnosis.previewPath}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <!-- Optional open PNG -->
                                                <c:if test="${not empty diagnosis.previewPath}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                        target="_blank" rel="noopener">
                                                        <i class="bi bi-box-arrow-up-right"></i> Open
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Image 2 -->
                                    <div class="image-card">
                                        <c:choose>
                                            <c:when test="${not empty diagnosis.preview2Path}">
                                                <img class="preview-img"
                                                    src="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                    alt="Mammography preview 2" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="image-tile">
                                                    Preview not generated yet.<br />
                                                    Use Open/Download.
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="image-meta">
                                            <div class="image-actions">
                                                <c:if test="${not empty diagnosis.preview2Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <!-- Optional open PNG -->
                                                <c:if test="${not empty diagnosis.preview2Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                        target="_blank" rel="noopener">
                                                        <i class="bi bi-box-arrow-up-right"></i> Open
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Image 3 -->
                                    <div class="image-card">
                                        <c:choose>
                                            <c:when test="${not empty diagnosis.preview3Path}">
                                                <img class="preview-img"
                                                    src="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                    alt="Mammography preview 3" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="image-tile">
                                                    Preview not generated yet.<br />
                                                    Use Open/Download.
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="image-meta">
                                            <div class="image-actions">
                                                <c:if test="${not empty diagnosis.preview3Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <!-- Optional open PNG -->
                                                <c:if test="${not empty diagnosis.preview3Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                        target="_blank" rel="noopener">
                                                        <i class="bi bi-box-arrow-up-right"></i> Open
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Image 4 -->
                                    <div class="image-card">
                                        <c:choose>
                                            <c:when test="${not empty diagnosis.preview4Path}">
                                                <img class="preview-img"
                                                    src="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                    alt="Mammography preview 4" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="image-tile">
                                                    Preview not generated yet.<br />
                                                    Use Open/Download.
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="image-meta">
                                            <div class="image-actions">
                                                <c:if test="${not empty diagnosis.preview4Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <!-- Optional open PNG -->
                                                <c:if test="${not empty diagnosis.preview4Path}">
                                                    <a class="btn-soft"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                        target="_blank" rel="noopener">
                                                        <i class="bi bi-box-arrow-up-right"></i> Open
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>

                                </div>

                            </div>

                            <!-- Screening Results -->
                            <div class="panel" style="margin-top:14px;">
                                <div class="panel-title">
                                    <i class="bi bi-file-earmark-medical"></i>
                                    <span>Screening Results</span>
                                </div>

                                <div class="info-label" style="margin-bottom:6px;">Result</div>

                                <!-- display-only pills -->
                                <div class="chip-row">
                                    <span
                                        class="chip <c:if test='${diagnosis.reviewed and not diagnosis.urgent}'>reviewed</c:if>">
                                        Benignant
                                    </span>
                                    <span
                                        class="chip <c:if test='${diagnosis.reviewed and diagnosis.urgent}'>urgent</c:if>">
                                        Malignant
                                    </span>
                                    <span class="chip <c:if test='${not diagnosis.reviewed}'>urgent</c:if>">
                                        Pending
                                    </span>
                                </div>

                                <div style="margin-top:14px;">
                                    <div class="notes-label">Clinical Notes</div>
                                    <div class="notes">
                                        <c:out value="${diagnosis.description}" default="—" />
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </body>

        </html>