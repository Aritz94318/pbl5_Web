package edu.mondragon.we2.pinkAlert.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.NotificationService;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.FinalResult;
import edu.mondragon.we2.pinkAlert.model.Notification;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DiagnosisService diagnosisService;
    private final NotificationService notificationService;

    public DoctorController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
        try {
            this.notificationService = new NotificationService();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init NotificationService", e);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,

            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "result", required = false, defaultValue = "ALL") String result,

            Model model) {

        LocalDate today = LocalDate.now();
        if (selectedDate == null)
            selectedDate = today;

        final LocalDate finalSelectedDate = selectedDate;

        // 1) Fetch diagnoses for selected date
        List<Diagnosis> diagnosesAll = diagnosisService.findByDateSortedByUrgency(finalSelectedDate);

        // --- counts based on ALL diagnoses of that date (keep your header accurate)
        // ---
        int total = diagnosesAll.size();
        long urgent = diagnosesAll.stream().filter(Diagnosis::isUrgent).count();
        long routine = total - urgent;

        long malignantCount = diagnosesAll.stream()
                .filter(Diagnosis::isReviewed)
                .filter(d -> d.getFinalResult() == FinalResult.MALIGNANT)
                .count();

        long benignCount = diagnosesAll.stream()
                .filter(Diagnosis::isReviewed)
                .filter(d -> d.getFinalResult() == FinalResult.BENIGN)
                .count();

        long inconclusiveCount = diagnosesAll.stream()
                .filter(Diagnosis::isReviewed)
                .filter(d -> d.getFinalResult() == FinalResult.INCONCLUSIVE)
                .count();

        // 2) Apply filters to list
        List<Diagnosis> diagnosesFiltered = diagnosesAll.stream()
                .filter(d -> matchesStatus(d, status))
                .filter(d -> matchesResult(d, result))
                .collect(Collectors.toList());

        // 3) Previous screenings per patient (for the *filtered list* or all list?)
        // Usually you want it to reflect ALL history, but at minimum keep it
        // consistent:
        Map<Integer, Long> previousScreenings = diagnosesAll.stream()
                .collect(Collectors.groupingBy(d -> d.getPatient().getId(), Collectors.counting()));

        // model (header counts = all; list = filtered)
        model.addAttribute("diagnoses", diagnosesFiltered);

        model.addAttribute("totalCount", total);
        model.addAttribute("urgentCount", urgent);
        model.addAttribute("routineCount", routine);

        model.addAttribute("malignantCount", malignantCount);
        model.addAttribute("benignCount", benignCount);
        model.addAttribute("inconclusiveCount", inconclusiveCount);

        // extra info to show "X shown"
        model.addAttribute("filteredCount", diagnosesFiltered.size());

        model.addAttribute("previousScreenings", previousScreenings);
        model.addAttribute("selectedDate", finalSelectedDate);
        model.addAttribute("selectedDateIso", finalSelectedDate.toString());

        // keep current filters so JSP can highlight the active chip
        model.addAttribute("statusFilter", status);
        model.addAttribute("resultFilter", result);

        // -----------------------------------------
        // DATE PILLS (5 days before selectedDate + selectedDate)
        // (IMPORTANT: preserve filters in the pill links via query string in JSP)
        // -----------------------------------------
        DateTimeFormatter monthDayFmt = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        LocalDate start = finalSelectedDate.minusDays(5);

        List<DatePill> datePills = IntStream.rangeClosed(0, 5)
                .mapToObj(i -> {
                    LocalDate date = start.plusDays(i);

                    String label;
                    long diffToToday = ChronoUnit.DAYS.between(date, today);
                    if (diffToToday == 0)
                        label = "Today";
                    else if (diffToToday == 1)
                        label = "Yesterday";
                    else
                        label = date.format(monthDayFmt);

                    String display = date.format(displayFmt);
                    String param = date.toString();
                    boolean active = date.equals(finalSelectedDate);

                    return new DatePill(label, display, param, active);
                })
                .collect(Collectors.toList());

        model.addAttribute("datePills", datePills);

        return "doctor/doctor-dashboard";
    }

    // --- helpers ---
    private static boolean matchesStatus(Diagnosis d, String statusRaw) {
        String status = (statusRaw == null) ? "ALL" : statusRaw.toUpperCase();

        boolean reviewed = d.isReviewed();
        boolean hasFinal = d.getFinalResult() != null;

        return switch (status) {
            case "ALL" -> true;
            case "PENDING" -> !reviewed || !hasFinal; // not reviewed OR no final result
            case "REVIEWED" -> reviewed; // reviewed flag true
            case "PENDING_REVIEW" -> !reviewed; // specifically pending review
            case "PENDING_RESULT" -> reviewed && !hasFinal; // reviewed but no final result
            default -> true;
        };
    }

    private static boolean matchesResult(Diagnosis d, String resultRaw) {
        String result = (resultRaw == null) ? "ALL" : resultRaw.toUpperCase();
        if ("ALL".equals(result))
            return true;

        // Only match if finalResult exists
        if (d.getFinalResult() == null)
            return false;

        return d.getFinalResult().name().equals(result);
    }

    @GetMapping("/diagnosis/{id}")
    public String diagnosisDetails(@PathVariable("id") Integer id, Model model) {
        Diagnosis diagnosis = diagnosisService.findById(id);

        // Load patient's diagnosis history (optional but useful)
        List<Diagnosis> historyDiagnoses = diagnosisService.findByPatient(diagnosis.getPatient().getId());
        historyDiagnoses.sort((a, b) -> b.getDate().compareTo(a.getDate())); // newest first
        long totalScreenings = diagnosisService.countByPatientId(diagnosis.getPatient().getId());
        model.addAttribute("totalScreenings", totalScreenings);

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("patient", diagnosis.getPatient());
        model.addAttribute("historyDiagnoses", historyDiagnoses);

        return "doctor/doctor-diagnosis"; // -> /WEB-INF/jsp/doctor-diagnosis.jsp
    }

    private String resultsReadyMessage(String fullName) {
        return """
                Hello %s,

                Your mammography exam has been reviewed by your doctor.
                Your results are now ready, and a member of the medical team will contact you soon
                to explain them and answer any questions.

                You can also log in to Pink Alert to follow the status of your screening.

                — Pink Alert Medical Team
                """.formatted(fullName);
    }

    private String resultsVisibleMessage(String fullName) {
        return """
                Hello %s,

                Your mammography result is now available in your Pink Alert patient portal.

                Please log in to view your report, images, and the doctor’s clinical notes.

                — Pink Alert Medical Team
                """.formatted(fullName);
    }

    @PostMapping("/diagnosis/{id}/review")
    public String saveReview(
            @PathVariable Integer id,
            @RequestParam(value = "finalResult", required = false) String finalResultRaw,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "patientNotified", required = false, defaultValue = "false") boolean patientNotified) {

        // 1) Load current state BEFORE saving (to detect transitions)
        Diagnosis before = diagnosisService.findById(id);
        boolean wasNotified = Boolean.TRUE.equals(before.getPatientNotified());
        boolean hadFinalResultBefore = before.getFinalResult() != null; // or before.isReviewed()

        // 2) Save review (this updates
        // reviewed/finalResult/description/patientNotified)
        diagnosisService.saveDoctorReview(id, finalResultRaw, description, patientNotified);

        // 3) Load AFTER saving (optional, but safer)
        Diagnosis after = diagnosisService.findById(id);

        // Data needed for email
        String email = after.getPatient().getUser().getEmail();
        String fullName = after.getPatient().getUser().getFullName();

        // 4) Trigger emails ONLY on transitions

        // A) "Results ready" -> when doctor sets finalResult the first time (or first
        // time reviewed)
        boolean hasFinalResultNow = after.getFinalResult() != null; // or after.isReviewed()
        if (!hadFinalResultBefore && hasFinalResultNow) {
            System.out.println("Patient email is: " + email);
            Notification n = new Notification(
                    email,
                    "Pink Alert - Results ready",
                    resultsReadyMessage(fullName),
                    java.time.LocalDateTime.now().toString());
            try {
                notificationService.sendEmail(n);
            } catch (Exception e) {
                e.printStackTrace(); // or log
            }
        }

        // B) "Results visible" -> when checkbox changes false -> true
        boolean isNotifiedNow = Boolean.TRUE.equals(after.getPatientNotified());
        if (!wasNotified && isNotifiedNow) {
            Notification n = new Notification(
                    email,
                    "Pink Alert - Results available in your portal",
                    resultsVisibleMessage(fullName),
                    java.time.LocalDateTime.now().toString());
            try {
                notificationService.sendEmail(n);
            } catch (Exception e) {
                e.printStackTrace(); // or log
            }
        }

        return "redirect:/doctor/diagnosis/" + id;
    }

    // Helper DTO for date buttons
    public static class DatePill {
        private final String label;
        private final String display;
        private final String param;
        private final boolean active;

        public DatePill(String label, String display, String param, boolean active) {
            this.label = label;
            this.display = display;
            this.param = param;
            this.active = active;
        }

        public String getLabel() {
            return label;
        }

        public String getDisplay() {
            return display;
        }

        public String getParam() {
            return param;
        }

        public boolean isActive() {
            return active;
        }
    }
}
