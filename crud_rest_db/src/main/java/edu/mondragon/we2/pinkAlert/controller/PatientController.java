package edu.mondragon.we2.pinkAlert.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;

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
        if (!(logged instanceof User)) {
            return "redirect:/login";
        }

        User sessionUser = (User) logged;

        if (sessionUser.getRole() != Role.PATIENT) {
            return "redirect:/login";
        }

        // ✅ use your existing method: get(id)
        User user = userService.get(sessionUser.getId());

        Patient patient = user.getPatient(); // <-- this requires User has getPatient()
        if (patient == null) {
            model.addAttribute("error", "No Patient profile linked to this user.");
            return "patient/patient-dashboard";
        }

        // 1) Fetch ONLY diagnoses for the logged patient
        List<Diagnosis> diagnoses = diagnosisService.findByPatient(patient.getId());
        diagnoses.sort((a, b) -> Boolean.compare(b.isUrgent(), a.isUrgent())); // urgent first (optional)
        diagnoses.sort((a, b) -> b.getDate().compareTo(a.getDate()));          // newest first

        int total = diagnoses.size();
        long urgent = diagnoses.stream().filter(Diagnosis::isUrgent).count();
        long reviewed = diagnoses.stream().filter(Diagnosis::isReviewed).count();
        long pending = diagnoses.stream().filter(d -> !d.isReviewed()).count();

        // 2) Count previous screenings (for THIS patient)
        long previousScreenings = total;
        model.addAttribute("previousScreenings", previousScreenings);
        model.addAttribute("upcomingCount", 0L);

        // 3) Send to JSP
        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("totalCount", total);
        model.addAttribute("urgentCount", urgent);
        model.addAttribute("reviewedCount", reviewed);
        model.addAttribute("pendingCount", pending);

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);



        return "patient/patient-dashboard";
    }
}
