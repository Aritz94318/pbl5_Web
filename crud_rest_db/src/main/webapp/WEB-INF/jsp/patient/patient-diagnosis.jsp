<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Patient Portal - Diagnosis</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-diagnosis.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        </head>

        <body>
            <div class="app-container">

                <div class="header-admin">
                    <div class="header-left-admin">
                        <a class="btn-back-icon" href="${pageContext.request.contextPath}/patient/dashboard"
                            title="Back to Dashboard">
                            <i class="bi bi-arrow-left-circle-fill"></i>
                        </a>

                        <div class="admin-shield">
                            <i class="bi bi-person-heart" aria-hidden="true"></i>
                        </div>

                        <div>
                            <h1>Mammography Patient Portal</h1>
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

                <div class="detail-grid">

                    <section class="card">
                        <div class="screening-header-top">
                            <div class="text-main" style="margin-bottom:0;">
                                Your result
                            </div>
                        </div>

                        <div class="result-row" style="margin-top:12px;">
                            <div>
                                <c:choose>
                                    <c:when test="${not diagnosis.patientNotified}">
                                        <span class="status-chip">
                                            <i class="bi bi-clock" aria-hidden="true"></i>
                                            Pending
                                        </span>
                                    </c:when>

                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${diagnosis.finalResult == 'MALIGNANT'}">
                                                <span class="status-chip status-danger">
                                                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                                    Malignant
                                                </span>
                                            </c:when>

                                            <c:when test="${diagnosis.finalResult == 'BENIGN'}">
                                                <span class="status-chip status-success">
                                                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                                                    Benign
                                                </span>
                                            </c:when>

                                            <c:when test="${diagnosis.finalResult == 'INCONCLUSIVE'}">
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

                        <div style="margin-top:16px;">
                            <div class="text-main" style="margin-bottom:10px;">
                                Doctor’s clinical notes
                            </div>

                            <c:choose>
                                <c:when test="${diagnosis.patientNotified}">
                                    <c:choose>
                                        <c:when test="${not empty diagnosis.description}">
                                            <div class="notes-box">
                                                <c:out value="${diagnosis.description}" />
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="notes-box" style="color:#64748b;">
                                                No clinical notes were provided.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <div class="notes-box" style="color:#64748b;">
                                        Notes will be available once the medical team has notified you about your
                                        results.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <c:if test="${diagnosis.patientNotified}">
                        <section class="card" style="margin-bottom: 5rem">
                            <div class="screening-header-top">
                                <div class="text-main" style="margin-bottom:0;">
                                    Mammography images
                                </div>
                            </div>

                            <div class="images-grid-pt">

                                <div class="img-card">
                                    <c:choose>
                                        <c:when test="${not empty diagnosis.previewPath}">
                                            <img src="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                alt="Mammography image 1" />
                                            <div class="img-actions">
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                    target="_blank" rel="noopener">
                                                    <i class="bi bi-box-arrow-up-right"></i> Open
                                                </a>
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                    download>
                                                    <i class="bi bi-download"></i> Download
                                                </a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="img-empty">Preview not available.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Image 2 -->
                                <div class="img-card">
                                    <c:choose>
                                        <c:when test="${not empty diagnosis.preview2Path}">
                                            <img src="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                alt="Mammography image 2" />
                                            <div class="img-actions">
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                    target="_blank" rel="noopener">
                                                    <i class="bi bi-box-arrow-up-right"></i> Open
                                                </a>
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                    download>
                                                    <i class="bi bi-download"></i> Download
                                                </a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="img-empty">Preview not available.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Image 3 -->
                                <div class="img-card">
                                    <c:choose>
                                        <c:when test="${not empty diagnosis.preview3Path}">
                                            <img src="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                alt="Mammography image 3" />
                                            <div class="img-actions">
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                    target="_blank" rel="noopener">
                                                    <i class="bi bi-box-arrow-up-right"></i> Open
                                                </a>
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                    download>
                                                    <i class="bi bi-download"></i> Download
                                                </a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="img-empty">Preview not available.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Image 4 -->
                                <div class="img-card">
                                    <c:choose>
                                        <c:when test="${not empty diagnosis.preview4Path}">
                                            <img src="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                alt="Mammography image 4" />
                                            <div class="img-actions">
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                    target="_blank" rel="noopener">
                                                    <i class="bi bi-box-arrow-up-right"></i> Open
                                                </a>
                                                <a class="btn-admin btn-secondary"
                                                    href="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                    download>
                                                    <i class="bi bi-download"></i> Download
                                                </a>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="img-empty">Preview not available.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                            </div>
                        </section>
                    </c:if>

                </div>
            </div>
        </body>

        </html>