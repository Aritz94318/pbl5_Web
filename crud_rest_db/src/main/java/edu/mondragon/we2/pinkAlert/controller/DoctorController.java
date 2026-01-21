package edu.mondragon.we2.pinkalert.controller;

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

import edu.mondragon.we2.pinkalert.model.Diagnosis;
import edu.mondragon.we2.pinkalert.service.DiagnosisService;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DiagnosisService diagnosisService;

    public DoctorController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            Model model) {

        LocalDate today = LocalDate.now();

        if (selectedDate == null) {
            selectedDate = today;
        }

        final LocalDate finalSelectedDate = selectedDate;

        List<Diagnosis> diagnoses = diagnosisService.findByDateSortedByUrgency(finalSelectedDate);

        int total = diagnoses.size();
        long urgent = diagnoses.stream().filter(Diagnosis::isUrgent).count();
        long routine = total - urgent;

        Map<Integer, Long> previousScreenings = diagnoses.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPatient().getId(),
                        Collectors.counting()));

        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("totalCount", total);
        model.addAttribute("urgentCount", urgent);
        model.addAttribute("routineCount", routine);
        model.addAttribute("previousScreenings", previousScreenings);
        model.addAttribute("selectedDate", finalSelectedDate);



        DateTimeFormatter monthDayFmt = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        List<DatePill> datePills = IntStream.rangeClosed(0, 5)
                .mapToObj(offset -> {
                    LocalDate date = today.minusDays(5 - offset);

                    long daysDiff = ChronoUnit.DAYS.between(date, today);

                    String label;
                    if (daysDiff == 0)
                        label = "Today";
                    else if (daysDiff == 1)
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

    @GetMapping("/diagnosis/{id}")
    public String diagnosisDetails(@PathVariable("id") Integer id, Model model) {
        Diagnosis diagnosis = diagnosisService.findById(id);

        List<Diagnosis> historyDiagnoses = diagnosisService.findByPatient(diagnosis.getPatient().getId());
        historyDiagnoses.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("patient", diagnosis.getPatient());
        model.addAttribute("historyDiagnoses", historyDiagnoses);

        return "doctor/doctor-diagnosis"; 
    }

    @PostMapping("/diagnosis/{id}/review")
    public String saveReview(
            @PathVariable Integer id,
            @RequestParam(value = "finalResult", required = false) String finalResultRaw,
            @RequestParam(value = "description", required = false) String description) {
        diagnosisService.saveDoctorReview(id, finalResultRaw, description);
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
