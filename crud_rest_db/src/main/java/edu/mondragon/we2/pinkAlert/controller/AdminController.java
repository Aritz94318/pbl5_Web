package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;

    public AdminController(PatientRepository patientRepository,
                           DiagnosisRepository diagnosisRepository,
                           UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // -----------------------
        // BASIC COUNTS (cards)
        // -----------------------
        long totalPatients = patientRepository.count();
        long totalUsers = userRepository.count(); // optional if you want
        long totalScreenings = diagnosisRepository.count();
        long urgentCases = diagnosisRepository.countByUrgentTrue();

        // Completion: "reviewed = true" (you added Reviewed)
        long completed = diagnosisRepository.countByReviewedTrue();

        double completionRate = (totalScreenings == 0)
                ? 0.0
                : round1((completed * 100.0) / totalScreenings);

        // Positive rate (simple heuristic based on your getStatus logic)
        // Better later: store a real "result" enum in DB.
        long positive = diagnosisRepository.findAll().stream()
                .filter(d -> "Positive".equalsIgnoreCase(d.getStatus()))
                .count();

        double positiveRate = (completed == 0)
                ? 0.0
                : round1((positive * 100.0) / completed);

        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalScreenings", totalScreenings);
        model.addAttribute("urgentCases", urgentCases);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("positiveRate", positiveRate);

        // -----------------------
        // PIE DATA
        // -----------------------
        List<Diagnosis> all = diagnosisRepository.findAll();

        long negativeCount = all.stream().filter(d -> "Negative".equalsIgnoreCase(d.getStatus())).count();
        long positiveCount = all.stream().filter(d -> "Positive".equalsIgnoreCase(d.getStatus())).count();
        long pendingCount = all.stream().filter(d -> !d.isReviewed()).count();

        // If you don’t truly have "inconclusive", keep it 0 for now
        // (Later you can add a "result" column to Diagnosis)
        long inconclusiveCount = 0;

        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inconclusiveCount", inconclusiveCount);

        // -----------------------
        // TIMELINE DATA (last 7 days)
        // -----------------------
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        // group total by date
        Map<LocalDate, Long> totalByDate = all.stream()
                .filter(d -> d.getDate() != null && !d.getDate().isBefore(start) && !d.getDate().isAfter(today))
                .collect(Collectors.groupingBy(Diagnosis::getDate, Collectors.counting()));

        // group completed by date
        Map<LocalDate, Long> completedByDate = all.stream()
                .filter(d -> d.getDate() != null && d.isReviewed()
                        && !d.getDate().isBefore(start) && !d.getDate().isAfter(today))
                .collect(Collectors.groupingBy(Diagnosis::getDate, Collectors.counting()));

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

        List<String> labels = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> completes = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add("'" + d.format(labelFmt) + "'");
            totals.add(totalByDate.getOrDefault(d, 0L));
            completes.add(completedByDate.getOrDefault(d, 0L));
        }

        model.addAttribute("timelineLabelsJs", String.join(",", labels));
        model.addAttribute("timelineTotalJs", totals.stream().map(String::valueOf).collect(Collectors.joining(",")));
        model.addAttribute("timelineCompletedJs", completes.stream().map(String::valueOf).collect(Collectors.joining(",")));

        return "admin-dashboard"; // /WEB-INF/jsp/admin-dashboard.jsp
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
