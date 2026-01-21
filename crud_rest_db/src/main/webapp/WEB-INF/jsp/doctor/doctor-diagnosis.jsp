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
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
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
                                    <div class="info-value">${patient.age}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Email</div>
                                    <div class="info-value">${patient.user.email}</div>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Phone number</div>
                                    <div class="info-value">${patient.phone}</div>
                                </div>

                                <div class="info-row" style="margin-bottom:0;">
                                    <c:choose>
                                        <c:when test="${not empty previousScreenings}">
                                            ${previousScreenings[patient.id]} total screenings
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

                                <div class="info-row" style="margin-bottom: 1px;">
                                    <div class="info-label">Status</div>
                                    <div class="chip-row">
                                        <c:if test="${diagnosis.urgent}">
                                            <span class="chip urgent"><i class="bi bi-exclamation-triangle"></i>
                                                Urgent</span>
                                        </c:if>
                                        <c:if test="${diagnosis.reviewed}">
                                            <span class="chip reviewed"><i class="bi bi-check-circle"></i>
                                                Reviewed</span>
                                        </c:if>
                                        <c:if test="${not diagnosis.reviewed}">
                                            <span class="status-chip"><i class="bi bi-clock"></i> Pending Review</span>
                                        </c:if>
                                    </div>
                                    <!-- <div class="info-value">
                                        <div class="info-row" style="margin-top:12px; margin-bottom:0;"> -->

                                    <div class="info-row" style="margin-top:15px">

                                        <div class="info-label">Doctor's final diagnosis</div>
                                        <div class="info-value">
                                            <c:choose>
                                                <c:when test="${diagnosis.finalResult == 'BENIGN'}">
                                                    <span class="chip chip-benign bi bi-check-circle">Benign</span>
                                                </c:when>
                                                <c:when test="${diagnosis.finalResult == 'MALIGNANT'}">
                                                    <span
                                                        class="chip chip-malignant bi bi-exclamation-triangle">Malignant</span>
                                                </c:when>
                                                <c:when test="${diagnosis.finalResult == 'INCONCLUSIVE'}">
                                                    <span
                                                        class="chip chip-inconclusive bi bi-exclamation-triangle">Inconclusive</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="chip chip-pending bi bi-question-circle">Unknown</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <!-- </div>
                                </div> -->
                                    </div>
                                </div>
                            </div>

                            <div class="panel" style="margin-top:14px;">
                                <div class="panel-title">
                                    <i class="bi bi-cpu"></i>
                                    <span>AI’s prediction</span>
                                </div>

                                <div class="info-row">
                                    <div class="info-label">Predicted result</div>
                                    <div class="info-value">
                                        <c:choose>
                                            <c:when test="${diagnosis.aiPrediction == 'BENIGN'}">
                                                <span class="chip chip-malignant">
                                                    <i class="bi bi-exclamation-triangle"></i> Malignant
                                                </span>
                                            </c:when>
                                            <c:when test="${diagnosis.aiPrediction == 'MALIGNANT'}">
                                                <span class="chip chip-benign">
                                                    <i class="bi bi-exclamation-triangle"></i> Benign
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="chip chip-inconclusive">
                                                    <i class="bi bi-check-circle"></i> Pending
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <div class="info-row" style="margin-bottom:0;">
                                    <div class="info-label">Probability for the prediction to be correct</div>
                                    <div class="info-value">
                                        <c:out value="${diagnosis.probability}" default="—" />
                                    </div>
                                </div>

                                <!-- <c:if test="${diagnosis.reviewed and not empty diagnosis.finalResult}">
                                    <div class="info-row" style="margin-top:12px; margin-bottom:0;">
                                        <div class="info-label">Doctor final result</div>
                                        <div class="info-value">
                                            <c:choose>
                                                <c:when test="${diagnosis.finalResult == 'BENIGN'}">
                                                    <span class="chip chip-benign">Benign</span>
                                                </c:when>
                                                <c:when test="${diagnosis.finalResult == 'MALIGNANT'}">
                                                    <span class="chip chip-malignant">Malignant</span>
                                                </c:when>
                                                <c:when test="${diagnosis.finalResult == 'INCONCLUSIVE'}">
                                                    <span class="chip chip-inconclusive">Inconclusive</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="chip chip-pending">Unknown</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:if> -->
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
                                                <th>Result</th>
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="h" items="${historyDiagnoses}">
                                                <tr>
                                                    <td>${h.date}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${empty h.finalResult}">
                                                                <span
                                                                    class="chip chip-pending bi bi-clock">Pending</span>
                                                            </c:when>

                                                            <c:when test="${h.finalResult == 'BENIGN'}">
                                                                <span
                                                                    class="chip chip-benign bi bi-check-circle">Benign</span>
                                                            </c:when>

                                                            <c:when test="${h.finalResult == 'MALIGNANT'}">
                                                                <span
                                                                    class="chip chip-malignant bi bi-exclamation-triangle">Malignant</span>
                                                            </c:when>

                                                            <c:when test="${h.finalResult == 'INCONCLUSIVE'}">
                                                                <span
                                                                    class="chip chip-inconclusive bi bi-exclamation-triangle">Inconclusive</span>
                                                            </c:when>

                                                            <c:otherwise>
                                                                <span
                                                                    class="chip chip-pending bi bi-question-circle">Unknown</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
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
                                                    <a class="btn-soft btn-secondary"
                                                        href="${pageContext.request.contextPath}/${diagnosis.previewPath}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <c:if test="${not empty diagnosis.previewPath}">
                                                    <a class="btn-soft btn-secondary"
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
                                                    <a class="btn-soft btn-secondary"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview2Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <c:if test="${not empty diagnosis.preview2Path}">
                                                    <a class="btn-soft btn-secondary"
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
                                                    <a class="btn-soft btn-secondary"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview3Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <c:if test="${not empty diagnosis.preview3Path}">
                                                    <a class="btn-soft btn-secondary"
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
                                                    <a class="btn-soft btn-secondary"
                                                        href="${pageContext.request.contextPath}/${diagnosis.preview4Path}"
                                                        download>
                                                        <i class="bi bi-download"></i> Download
                                                    </a>
                                                </c:if>

                                                <c:if test="${not empty diagnosis.preview4Path}">
                                                    <a class="btn-soft btn-secondary"
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

                                <form method="post"
                                    action="${pageContext.request.contextPath}/doctor/diagnosis/${diagnosis.id}/review"
                                    id="reviewForm">

                                    <!-- This is what the server will read -->
                                    <input type="hidden" name="finalResult" id="finalResult"
                                        value="${empty diagnosis.finalResult ? '' : diagnosis.finalResult}" />

                                    <div class="info-label" style="margin-bottom:6px;">Result</div>

                                    <div class="chip-row">
                                        <button type="button"
                                            class="chip chip-btn chip-benign bi bi-check-circle${diagnosis.finalResult == 'BENIGN' ? 'selected' : ''}"
                                            data-value="BENIGN" aria-pressed="${diagnosis.finalResult == 'BENIGN'}">
                                            Benign
                                        </button>

                                        <button type="button"
                                            class="chip chip-btn chip-malignant bi bi-exclamation-triangle ${diagnosis.finalResult == 'MALIGNANT' ? 'selected' : ''}"
                                            data-value="MALIGNANT"
                                            aria-pressed="${diagnosis.finalResult == 'MALIGNANT'}">
                                            Malignant
                                        </button>

                                        <button type="button"
                                            class="chip chip-btn chip-inconclusive bi bi-exclamation-triangle ${diagnosis.finalResult == 'INCONCLUSIVE' ? 'selected' : ''}"
                                            data-value="INCONCLUSIVE"
                                            aria-pressed="${diagnosis.finalResult == 'INCONCLUSIVE'}">
                                            Inconclusive
                                        </button>
                                    </div>

                                    <div style="margin-top:14px;">
                                        <div class="notes-label">Clinical Notes</div>
                                        <textarea class="notes-input" name="description" rows="5"
                                            placeholder="Write relevant clinical notes here...">${diagnosis.description}</textarea>
                                    </div>

                                    <div style="margin-top:12px; display:flex; gap:10px;">
                                        <button type="submit" class="btn-soft btn-primary">
                                            <i class="bi bi-save"></i> Save results
                                        </button>
                                    </div>

                                </form>

                                <script>
                                    (function () {
                                        const hidden = document.getElementById('finalResult');
                                        const buttons = document.querySelectorAll('.chip-btn');

                                        function setSelected(value) {
                                            hidden.value = value;

                                            buttons.forEach(btn => {
                                                const isSelected = btn.dataset.value === value;
                                                btn.setAttribute('aria-pressed', isSelected ? 'true' : 'false');

                                                // reset visual classes (keep base "chip chip-btn")
                                                btn.classList.remove('selected');

                                                if (isSelected) btn.classList.add('selected');
                                            });
                                        }

                                        // initial highlight based on hidden input
                                        setSelected(hidden.value || '');

                                        buttons.forEach(btn => {
                                            btn.addEventListener('click', () => setSelected(btn.dataset.value));
                                        });
                                    })();
                                </script>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </body>

        </html>