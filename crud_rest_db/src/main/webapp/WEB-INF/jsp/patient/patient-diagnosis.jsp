<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Mammography Review Portal - Diagnosis Details</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/patient-dashboard.css">

            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        </head>

        <body>
            <div class="app-container">

                <div class="header-admin">
                    <div class="header-left-admin">
                        <!-- BACK TO DASHBOARD -->
                        <a class="btn-back-icon"
                            href="${pageContext.request.contextPath}/patient/dashboard" title="Back to Dashboard"> <!-- redirigir pag correcta -->
                            <i class="bi bi-arrow-left-circle-fill"></i>
                        </a>
                        <div class="admin-shield">
                            <i class="bi bi-clipboard2-pulse" aria-hidden="true"></i>
                        </div>
                        <div>
                            <h1>Mammography Result</h1>
                            <p>Diagnosis details </p>
                        </div>
                    </div>

                    <div class="header-right">
                        <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i>
                            Logout
                        </a>
                    </div>
                </div>

                <!-- Result -->
                <c:choose>
                    <c:when test="${not diagnosis.reviewed}">
                        <div class="result-container result-inconclusive">
                            <div class="result-title">
                                <i class="bi bi-hourglass-split" aria-hidden="true"></i>
                                Inconclusive
                            </div>
                            <div class="result-description">
                                This result is still under medical review.
                            </div>
                        </div>
                    </c:when>

                    <c:when test="${diagnosis.urgent}">
                        <div class="result-container result-positive">
                            <div class="result-title">
                                <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                Positive
                            </div>
                            <div class="result-description">
                                Findings indicate a high probability of malignancy.
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="result-container result-negative">
                            <div class="result-title">
                                <i class="bi bi-check-circle" aria-hidden="true"></i>
                                Negative
                            </div>
                            <div class="result-description">
                                No signs of malignancy were detected.
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>


                <!-- PATIENT + DIAGNOSIS CARD -->
                <div class="two-cards-row">
                    <!-- Patient information (CARD 1) -->
                    <section class="card two-cards-item">
                        <div class="two-cards-title">
                            <i class="bi bi-person-vcard" aria-hidden="true"></i>
                            Patient Information
                        </div>

                        <div class="text-meta two-cards-body">
                            <div><strong>Full name:</strong> ${patient.user.fullName} · <strong>Age:</strong> ${patient.age}</div>
                            <div><strong>Patient ID:</strong> PT-${patient.id}</div>
                            <div><strong>Birthdate:</strong> ${patient.birthDate}</div>
                            <div><strong>Email:</strong> ${patient.user.email}</div>
                            <div><strong>Username:</strong> ${patient.user.username}</div>
                        </div>
                    </section>

                    <!-- Screening details (CARD 2) -->
                    <section class="card two-cards-item">
                        <div class="two-cards-title">
                            <i class="bi bi-clipboard2-pulse" aria-hidden="true"></i>
                            Screening Details
                        </div>

                        <div class="text-meta two-cards-body">
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
                            <div><strong>Probability:</strong> ${diagnosis.probability}</div>
                            <div><strong>Image path:</strong> ${diagnosis.imagePath}</div>
                        </div>

                        <div class="two-cards-subsection">
                            <div class="two-cards-subtitle">
                                <i class="bi bi-file-text" aria-hidden="true"></i>
                                Description
                            </div>
                            <div class="text-meta">
                                ${diagnosis.description}
                            </div>
                        </div>
                    </section>

                </div> <!--fn-->

            </div>
        </body>

        </html>