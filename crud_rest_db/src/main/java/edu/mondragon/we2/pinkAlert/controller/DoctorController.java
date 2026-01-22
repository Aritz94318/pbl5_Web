package edu.mondragon.we2.pinkAlert.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import edu.mondragon.we2.pinkAlert.service.NotificationService;
import edu.mondragon.we2.pinkAlert.model.FinalResult;
import edu.mondragon.we2.pinkAlert.model.Notification;
import org.slf4j.Logger;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);
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

        List<Diagnosis> diagnosesAll = diagnosisService.findByDateSortedByUrgency(finalSelectedDate);

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

        List<Diagnosis> diagnosesFiltered = diagnosesAll.stream()
                .filter(d -> matchesStatus(d, status))
                .filter(d -> matchesResult(d, result))
                .collect(Collectors.toList());

        Map<Integer, Long> previousScreenings = diagnosesAll.stream()
                .collect(Collectors.groupingBy(d -> d.getPatient().getId(), Collectors.counting()));

        model.addAttribute("diagnoses", diagnosesFiltered);

        model.addAttribute("totalCount", total);
        model.addAttribute("urgentCount", urgent);
        model.addAttribute("routineCount", routine);

        model.addAttribute("malignantCount", malignantCount);
        model.addAttribute("benignCount", benignCount);
        model.addAttribute("inconclusiveCount", inconclusiveCount);
        model.addAttribute("filteredCount", diagnosesFiltered.size());

        model.addAttribute("previousScreenings", previousScreenings);
        model.addAttribute("selectedDate", finalSelectedDate);
        model.addAttribute("selectedDateIso", finalSelectedDate.toString());

        model.addAttribute("statusFilter", status);
        model.addAttribute("resultFilter", result);

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

    private static boolean matchesStatus(Diagnosis d, String statusRaw) {
        String status = (statusRaw == null) ? "ALL" : statusRaw.toUpperCase();

        boolean reviewed = d.isReviewed();
        boolean hasFinal = d.getFinalResult() != null;

        return switch (status) {
            case "ALL" -> true;
            case "PENDING" -> !reviewed || !hasFinal; 
            case "REVIEWED" -> reviewed;
            case "PENDING_REVIEW" -> !reviewed; 
            case "PENDING_RESULT" -> reviewed && !hasFinal; 
            default -> true;
        };
    }

    private static boolean matchesResult(Diagnosis d, String resultRaw) {
        String result = (resultRaw == null) ? "ALL" : resultRaw.toUpperCase();
        if ("ALL".equals(result))
            return true;

        if (d.getFinalResult() == null)
            return false;

        return d.getFinalResult().name().equals(result);
    }

    @GetMapping("/diagnosis/{id}")
    public String diagnosisDetails(@PathVariable("id") Integer id, Model model) {
        Diagnosis diagnosis = diagnosisService.findById(id);

        List<Diagnosis> historyDiagnoses = diagnosisService.findByPatient(diagnosis.getPatient().getId());
        historyDiagnoses.sort((a, b) -> b.getDate().compareTo(a.getDate())); 
        long totalScreenings = diagnosisService.countByPatientId(diagnosis.getPatient().getId());
        model.addAttribute("totalScreenings", totalScreenings);

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("patient", diagnosis.getPatient());
        model.addAttribute("historyDiagnoses", historyDiagnoses);

        return "doctor/doctor-diagnosis";
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

        Diagnosis before = diagnosisService.findById(id);
        boolean wasNotified = Boolean.TRUE.equals(before.getPatientNotified());
        boolean hadFinalResultBefore = before.getFinalResult() != null;
        diagnosisService.saveDoctorReview(id, finalResultRaw, description, patientNotified);

        Diagnosis after = diagnosisService.findById(id);
        String email = after.getPatient().getUser().getEmail();
        String fullName = after.getPatient().getUser().getFullName();
        boolean hasFinalResultNow = after.getFinalResult() != null;
        if (!hadFinalResultBefore && hasFinalResultNow) {
            log.info("Patient email is: {}", email);
            Notification n = new Notification(
                    email,
                    "Pink Alert - Results ready",
                    resultsReadyMessage(fullName),
                    java.time.LocalDateTime.now().toString());
            try {
                notificationService.sendEmail(n);
            } catch (Exception e) {
                log.error("Error enviando notificación para paciente: {}", fullName, e);
       
            }
        }
        boolean isNotifiedNow = Boolean.TRUE.equals(after.getPatientNotified());
        if (!wasNotified && isNotifiedNow) {
            Notification n = new Notification(
                    email,
                    "Pink Alert - Results available in your portal",
                    resultsVisibleMessage(fullName),
                    java.time.LocalDateTime.now().toString());
            try {
                notificationService.sendEmail(n);
            }catch (Exception e) {
    log.error("Error enviando notificación para paciente: {}", fullName, e);

}
        }

        return "redirect:/doctor/diagnosis/" + id;
    }

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
