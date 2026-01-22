<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <!DOCTYPE html>
    <html>

    <head>
      <title>Admin - User Form</title>
      <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin-dashboard.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/header.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/simulation.css">
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    </head>

    <body>
      <div class="app-container">
        <div class="header-admin">
          <div class="header-left-admin">
            <a class="btn-back-icon" href="${pageContext.request.contextPath}/admin/dashboard"
              title="Back to Dashboard">
              <i class="bi bi-arrow-left-circle-fill"></i>
            </a>
            <div class="admin-shield">
              <i class="bi bi-shield-lock"></i>
            </div>
            <div>
              <h1>Administrative Dashboard</h1>
              <p>Configure parameters and visualize real-time actions</p>
            </div>
          </div>
          <div class="header-right">
            <a class="btn-ghost" href="${pageContext.request.contextPath}/login">
              <i class="bi bi-box-arrow-right"></i>
              Logout
            </a>
          </div>
        </div>

        <div class="admin-wrap">

          <!-- SETTINGS -->
          <section class="admin-card">
            <h2>Settings</h2>

            <c:if test="${not empty error}">
              <div class="error-box">${error}</div>
            </c:if>

            <form id="simForm" action="<c:url value='/admin/simulation/start'/>" method="post">

              <div class="sim-form">
                <div class="sim-field">
                  <label for="numPatients" class="detail-label">Number of patients</label>
                  <input class="sim-num" id="numPatients" name="numPatients" type="number" min="0" step="1"
                    value="${empty numPatients ? 1 : numPatients}" placeholder="1" required />
                </div>

                <div class="sim-field">
                  <label for="numDoctors" class="detail-label">Number of doctors</label>
                  <input class="sim-num" id="numDoctors" name="numDoctors" type="number" min="0" step="1"
                    value="${empty numDoctors ? 1 : numDoctors}" placeholder="1" required />
                </div>

                <div class="sim-field">
                  <label for="numMachines" class="detail-label">Number of machines</label>
                  <input class="sim-num" id="numMachines" name="numMachines" type="number" min="0" step="1"
                    value="${empty numMachines ? 1 : numMachines}" placeholder="1" required />
                </div>

                <div class="sim-actions">
                  <button class="btn-admin btn-primary" type="button" id="startSimBtn">▶ Start simulation</button>
                  <button class="btn-admin btn-secondary" type="button" id="modifySimBtn">▶ Modify simulation</button>
                </div>
              </div>

              <input type="hidden" id="csrfToken" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </form>
          </section>

          <!-- SIM TIME (unchanged) -->
          <section class="admin-grid-2">
            <div class="patient-card" style="cursor: default; border-left-color:#f5b700;">
              <div class="patient-main">
                <div class="patient-icon" style="background:#f5b700;">⏳</div>
                <div class="patient-info">
                  <div class="patient-name">Predicted total time</div>
                  <div class="patient-subinfo">
                    <span id="simTimeText">Waiting…</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- ACTIONS: NOW ONE PER ROW -->
          <section class="admin-grid-1">

            <div class="admin-card">
              <h2>Actions · Patients</h2>

              <div class="card" style="margin-top:0;">
                <div id="patientList" class="patient-list" style="margin-top: 0;">
                  <c:choose>
                    <c:when test="${not empty patientLogs}">
                      <c:forEach items="${patientLogs}" var="line">
                        <div class="patient-card" style="cursor: default;">
                          <div class="patient-main">
                            <div class="patient-icon">🧑</div>
                            <div class="patient-info">
                              <div class="patient-name">Patient</div>
                              <div class="patient-subinfo">
                                <span>${line}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <p class="text-meta">There are no patients yet.</p>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>

            <div class="admin-card">
              <h2>Actions · Doctors</h2>

              <div class="card" style="margin-top:0;">
                <div id="doctorList" class="patient-list" style="margin-top: 0;">
                  <c:choose>
                    <c:when test="${not empty doctorLogs}">
                      <c:forEach items="${doctorLogs}" var="line">
                        <div class="patient-card" style="cursor: default; border-left-color:#2ac769;">
                          <div class="patient-main">
                            <div class="patient-icon" style="background:#2ac769;">🩺</div>
                            <div class="patient-info">
                              <div class="patient-name">Doctor</div>
                              <div class="patient-subinfo">
                                <span>${line}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <p class="text-meta">There are no doctors yet.</p>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>

            <div class="admin-card">
              <h2>Actions · Machines</h2>

              <div class="card" style="margin-top:0;">
                <div id="machineList" class="patient-list" style="margin-top: 0;">
                  <c:choose>
                    <c:when test="${not empty machineLogs}">
                      <c:forEach items="${machineLogs}" var="line">
                        <div class="patient-card" style="cursor: default; border-left-color:#7f8bad;">
                          <div class="patient-main">
                            <div class="patient-icon" style="background:#7f8bad;">🖥</div>
                            <div class="patient-info">
                              <div class="patient-name">Machine</div>
                              <div class="patient-subinfo">
                                <span>${line}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <p class="text-meta">There are no patients yet.</p>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>

          </section>

        </div>
      </div>

      <script>
        const startSimBtn = document.getElementById("startSimBtn");

        startSimBtn.addEventListener("click", () => {
          clearLists();
          fetch("/admin/simulation/start", { method: "POST" })
            .then(res => {
              if (!res.ok) console.error("Error al iniciar la simulación");
            })
            .catch(err => console.error(err));
        });

        const modifyBtn = document.getElementById("modifySimBtn");

        modifyBtn.addEventListener("click", () => {
          const numPatients = document.getElementById("numPatients").value;
          const numDoctors = document.getElementById("numDoctors").value;
          const numMachines = document.getElementById("numMachines").value;

          const csrfInput = document.getElementById("csrfToken");
          const csrfToken = csrfInput?.value;
          const csrfParam = csrfInput?.name;

          const params = new URLSearchParams({ numPatients, numDoctors, numMachines });
          if (csrfToken && csrfParam) params.append(csrfParam, csrfToken);

          fetch("<c:url value='/admin/simulation/modify'/>", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
          })
            .then(async res => {
              if (!res.ok) {
                const txt = await res.text().catch(() => "");
                throw new Error(`HTTP ${res.status}: ${txt}`);
              }
              console.log("✅ Simulación modificada");
            })
            .catch(err => console.error("❌ Error modificando simulación", err));
        });

        function clearLists() {
          document.getElementById("patientList").innerHTML = "";
          document.getElementById("doctorList").innerHTML = "";
          document.getElementById("machineList").innerHTML = "";
          document.getElementById("simTimeText").textContent = "Waiting…";
        }

        function removeEmptyText(listEl) {
          const emptyP = listEl.querySelector("p.text-meta");
          if (emptyP) emptyP.remove();
        }

        function appendCard(listEl, whoLabel, iconEmoji, iconBg, borderColor, lineText) {
          removeEmptyText(listEl);

          const card = document.createElement("div");
          card.className = "patient-card";
          card.style.cursor = "default";
          if (borderColor) card.style.borderLeftColor = borderColor;

          const main = document.createElement("div");
          main.className = "patient-main";

          const icon = document.createElement("div");
          icon.className = "patient-icon";
          icon.textContent = iconEmoji;
          if (iconBg) icon.style.background = iconBg;

          const info = document.createElement("div");
          info.className = "patient-info";

          const name = document.createElement("div");
          name.className = "patient-name";
          name.textContent = whoLabel;

          const sub = document.createElement("div");
          sub.className = "patient-subinfo";

          const span = document.createElement("span");
          span.textContent = lineText;

          sub.appendChild(span);
          info.appendChild(name);
          info.appendChild(sub);

          main.appendChild(icon);
          main.appendChild(info);
          card.appendChild(main);

          listEl.appendChild(card);
          listEl.scrollTop = listEl.scrollHeight;
        }

        const patientList = document.getElementById("patientList");
        const doctorList = document.getElementById("doctorList");
        const machineList = document.getElementById("machineList");

        const es = new EventSource("<c:url value='/admin/sim/stream'/>");

        es.addEventListener("sim", (evt) => {
          const e = JSON.parse(evt.data);

          if (e.actor === "PATIENT") {
            appendCard(patientList, "Patient " + e.actorId, "🧑", null, null, e.text);
          } else if (e.actor === "DOCTOR") {
            appendCard(doctorList, "Doctor " + e.actorId, "🩺", "#2ac769", "#2ac769", e.text);
          } else if (e.actor === "MACHINE") {
            appendCard(machineList, "Machine " + e.actorId, "🖥", "#7f8bad", "#7f8bad", e.text);
          }
        });

        es.addEventListener("sim-time", (evt) => {
          const t = JSON.parse(evt.data);
          const text = t.hours + "h " + t.minutes + "m " + t.seconds + "s";
          document.getElementById("simTimeText").textContent = text;
        });

        es.onerror = () => {
          console.log("SSE desconectado (reintentando)...");
        };
      </script>

    </body>

    </html>