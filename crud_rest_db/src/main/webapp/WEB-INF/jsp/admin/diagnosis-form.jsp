<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <title>Machine Simulator - New Diagnosis</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/diagnosis-form.css">

        </head>

        <body>

            <div class="header-admin">
                <div class="header-left-admin">
                    <a class="btn-back-icon" href="${pageContext.request.contextPath}/admin/dashboard"
                        title="Back to Dashboard" style="margin-right:12px; color:#fff; text-decoration:none;">
                        <i class="bi bi-arrow-left-circle-fill" style="font-size:1.6rem;"></i>
                    </a>
                    <div class="admin-shield">
                        <i class="bi bi-camera" aria-hidden="true"></i>
                    </div>
                    <div>
                        <h1>Mammography Machine Simulator</h1>
                        <p>Create a Diagnosis by uploading a DICOM mammography</p>
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
                <div class="admin-card">
                    <c:if test="${not empty error}">
                        <div class="error-box" style="margin-top:12px;">
                            <i class="bi bi-exclamation-triangle"></i>
                            <c:out value="${error}" />
                        </div>
                    </c:if>
                    <c:if test="${not empty ok}">
                        <div class="ok-box" style="margin-top:12px;">
                            <i class="bi bi-check-circle"></i>
                            <c:out value="${ok}" />
                        </div>
                    </c:if>
                    <form id="newDiagnosisForm" class="form-grid" style="margin-top:14px;" method="post"
                        action="${pageContext.request.contextPath}/admin/diagnoses">

                        <div class="row-2">
                            <div class="field suggest-wrap">
                                <label>Patient</label>
                                <input id="patientQuery" type="text" placeholder="Start typing a patient name..."
                                    autocomplete="off" required>
                                <input type="hidden" name="patientId" id="patientId" required>
                                <div id="suggestList" class="suggest-list"></div>
                                <div class="hint" style="margin-top:6px;">
                                    Pick a patient from the dropdown.
                                </div>
                            </div>

                            <div class="field">
                                <label>Date</label>
                                <input type="date" name="date" value="${today}" required>
                            </div>
                        </div>
                  

                        <div class="field pictures">
                            <label>Mammography pictures</label>
                            <div class="hint" style="margin-top:6px;">
                                Must be public Google Drive direct download links
                            </div>
                            <input type="url" name="dicomUrl"
                                placeholder="https://drive.google.com/uc?export=download&id=..." required>
                            <input type="url" name="dicomUrl2"
                                placeholder="https://drive.google.com/uc?export=download&id=..." required
                                style="margin-top:8px;">
                            <input type="url" name="dicomUrl3"
                                placeholder="https://drive.google.com/uc?export=download&id=..." required
                                style="margin-top:8px;">
                            <input type="url" name="dicomUrl4"
                                placeholder="https://drive.google.com/uc?export=download&id=..." required
                                style="margin-top:8px;">
                        </div>

                        <div class="admin-tools" style="margin-top:4px;">
                            <button class="btn-admin btn-primary" type="submit">
                                <i class="bi bi-upload"></i> Create Diagnosis
                            </button>
                            <a class="btn-admin btn-secondary"
                                href="${pageContext.request.contextPath}/admin/dashboard">
                                <i class="bi bi-x-circle"></i> Cancel
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <script>
                (function () {
                    var ctx = "<c:out value='${pageContext.request.contextPath}'/>";
                    var input = document.getElementById("patientQuery");
                    var hiddenId = document.getElementById("patientId");
                    var list = document.getElementById("suggestList");
                    var lastQuery = "";
                    var debounceTimer = null;

                    function hideList() {
                        list.style.display = "none";
                        list.innerHTML = "";
                    }

                    function escapeHtml(str) {
                        return (str == null ? "" : String(str)).replace(/[&<>"']/g, function (m) {
                            return ({
                                "&": "&amp;",
                                "<": "&lt;",
                                ">": "&gt;",
                                "\"": "&quot;",
                                "'": "&#039;"
                            })[m];
                        });
                    }

                    function showList(items) {
                        if (!items || items.length === 0) {
                            hideList();
                            return;
                        }
                        var html = "";
                        for (var i = 0; i < items.length; i++) {
                            var p = items[i];

                            html += '<div class="suggest-item" data-id="' + p.id + '">';
                            html += '  <div>' + escapeHtml(p.label) + '</div>';
                            html += '  <div class="muted">PT-' + p.id + '</div>';
                            html += '</div>';
                        }
                        list.innerHTML = html;
                        list.style.display = "block";
                    }

                    input.addEventListener("input", function () {
                        var q = input.value.trim();
                        hiddenId.value = "";
                        if (q.length < 1) {
                            hideList();
                            return;
                        }
                        clearTimeout(debounceTimer);
                        debounceTimer = setTimeout(function () {
                            if (q === lastQuery) return;
                            lastQuery = q;
                            var url = ctx + "/admin/patients/suggest?q=" + encodeURIComponent(q);
                            fetch(url)
                                .then(function (res) {
                                    if (!res.ok) throw new Error("Suggest failed");
                                    return res.json();
                                })
                                .then(function (data) {
                                    showList(data);
                                })
                                .catch(function () {
                                    hideList();
                                });
                        }, 200);
                    });

                    list.addEventListener("click", function (e) {
                        var item = e.target.closest(".suggest-item");
                        if (!item) return;
                        var id = item.getAttribute("data-id");
                        var labelText = (item.querySelector("div") ? item.querySelector("div").textContent : "");

                        hiddenId.value = id;
                        input.value = labelText;
                        hideList();
                    });

                    document.addEventListener("click", function (e) {
                        if (!e.target.closest(".suggest-wrap")) hideList();
                    });

                    var fileInput = document.getElementById("dicomFile");
                    if (fileInput) {
                        fileInput.addEventListener("change", function () {
                            var f = fileInput.files && fileInput.files[0];
                            if (!f) return;
                            var name = (f.name || "").toLowerCase();
                            if (!name.endsWith(".dcm")) {
                                alert("Please upload a DICOM file (.dcm).");
                                fileInput.value = "";
                            }
                        });
                    }
                    var form = document.getElementById("newDiagnosisForm");
                    form.addEventListener("submit", function (e) {
                        if (!hiddenId.value) {
                            e.preventDefault();
                            alert("Please select a patient from the dropdown.");
                        }
                    });
                })();
            </script>

        </body>

        </html>