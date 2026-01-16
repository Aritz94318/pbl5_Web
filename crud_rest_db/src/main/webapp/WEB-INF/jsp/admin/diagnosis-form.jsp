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

            <style>
                .form-grid {
                    display: grid;
                    gap: 12px;
                    max-width: 720px;
                }

                .field label {
                    display: block;
                    font-weight: 600;
                    margin-bottom: 6px;
                }

                .field input,
                .field textarea,
                .field select {
                    width: 100%;
                    padding: 10px 12px;
                    border: 1px solid #d0d0d0;
                    border-radius: 10px;
                    outline: none;
                }

                .field textarea {
                    min-height: 90px;
                    resize: vertical;
                }

                .suggest-wrap {
                    position: relative;
                }

                .suggest-list {
                    position: absolute;
                    top: 100%;
                    left: 0;
                    right: 0;
                    z-index: 50;
                    background: #fff;
                    border: 1px solid #ddd;
                    border-radius: 12px;
                    margin-top: 6px;
                    box-shadow: 0 12px 24px rgba(0, 0, 0, .08);
                    max-height: 240px;
                    overflow: auto;
                    display: none;
                }

                .suggest-item {
                    padding: 10px 12px;
                    cursor: pointer;
                    display: flex;
                    justify-content: space-between;
                    gap: 12px;
                }

                .suggest-item:hover {
                    background: #f4f7ff;
                }

                .muted {
                    color: #777;
                    font-size: .9rem;
                }

                .hint {
                    font-size: .92rem;
                    color: #666;
                }

                .error-box {
                    padding: 10px 12px;
                    border: 1px solid #ffb4b4;
                    background: #fff2f2;
                    border-radius: 12px;
                    color: #7a1c1c;
                }

                .ok-box {
                    padding: 10px 12px;
                    border: 1px solid #b2f0c7;
                    background: #effff4;
                    border-radius: 12px;
                    color: #0d5b2a;
                }
            </style>
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
                    <a class="btn-ghost" href="${pageContext.request.contextPath}/admin/dashboard">
                        <i class="bi bi-speedometer2"></i> Dashboard
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
                        action="${pageContext.request.contextPath}/admin/diagnoses" enctype="multipart/form-data">

                        <!-- PATIENT SUGGEST -->
                        <div class="field suggest-wrap">
                            <label>Patient</label>
                            <input id="patientQuery" type="text" placeholder="Start typing a patient name..."
                                autocomplete="off" required>

                            <input type="hidden" name="patientId" id="patientId" required>

                            <!-- Optional: email will be used in the AI JSON -->
                            <label style="margin-top:10px;">Email for notifications</label>
                            <input id="email" name="email" type="email" placeholder="patient@example.com" required>

                            <div id="suggestList" class="suggest-list"></div>

                            <div class="hint" style="margin-top:6px;">
                                You must pick a patient from the dropdown (not just type).
                            </div>
                        </div>

                        <!-- DICOM FILE -->
                        <label>DICOM URL (Google Drive direct download)</label>
                        <input type="url" name="dicomUrl"
                            placeholder="https://drive.google.com/uc?export=download&id=..." required>
                        <div class="hint" style="margin-top:6px;">
                            Must be a public Google Drive download link (uc?export=download&id=...).
                        </div>

                        <!-- <div class="field">
                            <label>DICOM Mammography (.dcm)</label>
                            <input id="dicomFile" type="file" name="image"
                                accept=".dcm,application/dicom,application/octet-stream" required>
                            <div class="hint" style="margin-top:6px;">
                                Only .dcm files are allowed.
                            </div>
                        </div> -->

                        <div class="field">
                            <label>Date</label>
                            <input type="date" name="date" value="${today}" required>
                        </div>

                        <div class="admin-tools" style="margin-top:4px;">
                            <button class="btn-admin btn-primary" type="submit">
                                <i class="bi bi-upload"></i> Create Diagnosis
                            </button>

                            <a class="btn-admin" href="${pageContext.request.contextPath}/admin/dashboard">
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
                    var emailInput = document.getElementById("email");
                    var list = document.getElementById("suggestList");
                    var fileInput = document.getElementById("dicomFile");

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
                            // If your suggest endpoint can include email in the future: p.email
                            var email = p.email ? String(p.email) : "";
                            html += '<div class="suggest-item" data-id="' + p.id + '" data-email="' + escapeHtml(email) + '">';
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
                        var email = item.getAttribute("data-email"); // may be empty

                        hiddenId.value = id;
                        input.value = labelText;

                        // If suggest returns email, autopopulate it; otherwise user fills it.
                        if (email) emailInput.value = email;

                        hideList();
                    });

                    document.addEventListener("click", function (e) {
                        if (!e.target.closest(".suggest-wrap")) hideList();
                    });

                    // Client-side DICOM extension validation
                    fileInput.addEventListener("change", function () {
                        var f = fileInput.files && fileInput.files[0];
                        if (!f) return;
                        var name = (f.name || "").toLowerCase();
                        if (!name.endsWith(".dcm")) {
                            alert("Please upload a DICOM file (.dcm).");
                            fileInput.value = "";
                        }
                    });

                    // Prevent submit if patient not selected from dropdown
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