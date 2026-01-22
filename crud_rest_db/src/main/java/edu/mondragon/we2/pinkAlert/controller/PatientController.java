package edu.mondragon.we2.pinkAlert.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final UserService userService;
    private final DiagnosisService diagnosisService;

    public PatientController(UserService userService, DiagnosisService diagnosisService) {
        this.userService = userService;
        this.diagnosisService = diagnosisService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        Object logged = session.getAttribute("loggedUser");
        if (!(logged instanceof User))
            return "redirect:/login";

        User sessionUser = (User) logged;
        if (sessionUser.getRole() != Role.PATIENT)
            return "redirect:/login";

        User user = userService.get(sessionUser.getId());
        Patient patient = user.getPatient();

        if (patient == null) {
            model.addAttribute("error", "No Patient profile linked to this user.");
            return "patient/patient-dashboard";
        }

        List<Diagnosis> diagnoses = diagnosisService.findByPatient(patient.getId());
        // -----------------------------
        // Last appointment (latest diagnosis date)
        // -----------------------------
        DateTimeFormatter lastFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

        Diagnosis lastAppointment = diagnoses.stream()
                .filter(d -> d.getDate() != null)
                .max((a, b) -> a.getDate().compareTo(b.getDate()))
                .orElse(null);

        if (lastAppointment != null) {
            model.addAttribute("lastAppointmentDate", lastAppointment.getDate().format(lastFmt));
            model.addAttribute("lastAppointmentId", lastAppointment.getId());
        } else {
            model.addAttribute("lastAppointmentDate", null);
            model.addAttribute("lastAppointmentId", null);
        }

        diagnoses.sort(
                (a, b) -> {
                    int urgentCmp = Boolean.compare(b.isUrgent(), a.isUrgent());
                    if (urgentCmp != 0)
                        return urgentCmp;
                    return b.getDate().compareTo(a.getDate());
                });

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);

        List<Map<String, Object>> diagnosesView = diagnoses.stream()
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", d.getId());
                    m.put("urgent", d.isUrgent());
                    m.put("reviewed", d.isReviewed());
                    m.put("patientNotified", d.getPatientNotified());
                    m.put("finalResult", d.getFinalResult() != null ? d.getFinalResult().name() : null); // optional
                    m.put("dateDisplay", d.getDate() != null ? d.getDate().format(fmt) : "");
                    return m;
                })
                .toList();

        model.addAttribute("diagnoses", diagnosesView);

        int total = diagnoses.size();
        long urgent = diagnoses.stream().filter(Diagnosis::isUrgent).count();
        long reviewed = diagnoses.stream().filter(Diagnosis::isReviewed).count();
        long pending = diagnoses.stream().filter(d -> !d.isReviewed()).count();

        model.addAttribute("previousScreenings", (long) total);

        model.addAttribute("totalCount", total);
        model.addAttribute("urgentCount", urgent);
        model.addAttribute("reviewedCount", reviewed);
        model.addAttribute("pendingCount", pending);

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);

        return "patient/patient-dashboard";
    }

    @GetMapping("/diagnosis/{id}")
    public String diagnosisDetails(@PathVariable("id") Integer id, Model model) {
        Diagnosis diagnosis = diagnosisService.findById(id);

        List<Diagnosis> historyDiagnoses = diagnosisService.findByPatient(diagnosis.getPatient().getId());
        historyDiagnoses.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        model.addAttribute("diagnosis", diagnosis);
        model.addAttribute("patient", diagnosis.getPatient());
        model.addAttribute("historyDiagnoses", historyDiagnoses);

        return "patient/patient-diagnosis";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {

        Object logged = session.getAttribute("loggedUser");
        if (!(logged instanceof User))
            return "redirect:/login";

        User sessionUser = (User) logged;
        if (sessionUser.getRole() != Role.PATIENT)
            return "redirect:/login";

        User user = userService.get(sessionUser.getId());
        Patient patient = user.getPatient();

        if (patient == null) {
            model.addAttribute("error", "No Patient profile linked to this user.");
            return "patient/patient-profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);

        return "patient/patient-profile";
    }

}
