package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.UserService;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

        private final PatientRepository patientRepository;
        private final DiagnosisRepository diagnosisRepository;
        private final DoctorRepository doctorRepository;
        private final UserRepository userRepository;
        private final UserService userService;

        public AdminController(PatientRepository patientRepository,
                        DiagnosisRepository diagnosisRepository,
                        UserRepository userRepository,
                        UserService userService,
                        DoctorRepository doctorRepository) {
                this.patientRepository = patientRepository;
                this.diagnosisRepository = diagnosisRepository;
                this.userRepository = userRepository;
                this.userService = userService;
                this.doctorRepository = doctorRepository;
        }

        @GetMapping("/dashboard")
        public String dashboard(Model model) {

                long totalPatients = patientRepository.count();
                long totalUsers = userRepository.count();
                long totalScreenings = diagnosisRepository.count();
                long urgentCases = diagnosisRepository.countByUrgentTrue();

                long completed = diagnosisRepository.countByReviewedTrue();

                double completionRate = (totalScreenings == 0)
                                ? 0.0
                                : round1((completed * 100.0) / totalScreenings);

                List<Diagnosis> all = diagnosisRepository.findAll();

                long positive = all.stream()
                                .filter(d -> d.getStatus() != null && "Positive".equalsIgnoreCase(d.getStatus()))
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

                long negativeCount = all.stream().filter(d -> "Negative".equalsIgnoreCase(d.getStatus())).count();
                long positiveCount = all.stream().filter(d -> "Positive".equalsIgnoreCase(d.getStatus())).count();
                long pendingCount = all.stream().filter(d -> !d.isReviewed()).count();
                long inconclusiveCount = 0;

                model.addAttribute("negativeCount", negativeCount);
                model.addAttribute("positiveCount", positiveCount);
                model.addAttribute("pendingCount", pendingCount);
                model.addAttribute("inconclusiveCount", inconclusiveCount);

                LocalDate today = LocalDate.now();
                LocalDate start = today.minusDays(6);

                Map<LocalDate, Long> totalByDate = all.stream()
                                .filter(d -> d.getDate() != null && !d.getDate().isBefore(start)
                                                && !d.getDate().isAfter(today))
                                .collect(Collectors.groupingBy(Diagnosis::getDate, Collectors.counting()));

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
                model.addAttribute("timelineTotalJs",
                                totals.stream().map(String::valueOf).collect(Collectors.joining(",")));
                model.addAttribute("timelineCompletedJs",
                                completes.stream().map(String::valueOf).collect(Collectors.joining(",")));

                return "admin-dashboard";
        }

        // ---------------------------
        // USERS CRUD
        // ---------------------------

        // View Users
        @GetMapping("/users")
        public String users(Model model) {
                model.addAttribute("users", userService.findAll());
                return "admin/users";
        }

        // Add User (form)
        @GetMapping("/users/new")
        public String newUser(Model model) {
                model.addAttribute("user", new User());
                model.addAttribute("roles", Role.values());
                return "admin/user-form";
        }

        // Create User (submit)
        @PostMapping("/users")
        public String createUser(@ModelAttribute("user") User user,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword) {

                if (rawPassword == null || rawPassword.isBlank()) {
                        // PasswordHash is NOT NULL -> give default if form left empty
                        rawPassword = "123";
                }

                // Satisfy DB triggers: role must match FK fields
                if (user.getRole() == Role.DOCTOR) {
                        Doctor doc = doctorRepository.save(new Doctor());
                        user.setDoctor(doc);
                        user.setPatient(null);
                } else if (user.getRole() == Role.PATIENT) {
                        // Needs birthDate (not in form) -> default for now
                        Patient p = patientRepository.save(new Patient(LocalDate.of(2000, 1, 1)));
                        user.linkPatient(p); // sets BOTH sides
                        user.setDoctor(null);
                } else { // ADMIN
                        user.unlinkDoctor();
                        user.unlinkPatient();
                }

                userService.createUser(user, rawPassword);
                return "redirect:/admin/users";
        }

        // Edit User (form)
        @GetMapping("/users/{id}/edit")
        public String editUser(@PathVariable Integer id, Model model) {
                model.addAttribute("user", userService.get(id));
                model.addAttribute("roles", Role.values());
                return "admin/user-form";
        }

        @Transactional
        @PostMapping("/users/{id}")
        public String updateUser(@PathVariable Integer id,
                        @ModelAttribute("user") User posted,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword) {

                User existing = userService.get(id);

                // basic fields
                existing.setUsername(posted.getUsername());
                existing.setEmail(posted.getEmail());
                existing.setFullName(posted.getFullName());

                Role oldRole = existing.getRole();
                Role newRole = posted.getRole();

                // password (optional)
                if (rawPassword != null && !rawPassword.isBlank()) {
                        userService.setPassword(existing, rawPassword);
                }

                if (oldRole != newRole) {

                        // -----------------------------
                        // STEP 1: make row VALID (neutral)
                        // -----------------------------
                        existing.setRole(Role.ADMIN); // ADMIN allows both null per trigger
                        existing.setDoctor(null);
                        existing.unlinkPatient(); // clears PatientID
                        userRepository.saveAndFlush(existing); // ✅ trigger sees a valid state

                        // -----------------------------
                        // STEP 2: apply new role + links
                        // -----------------------------
                        if (newRole == Role.ADMIN) {
                                existing.setRole(Role.ADMIN);
                                existing.setDoctor(null);
                                existing.unlinkPatient();
                        } else if (newRole == Role.DOCTOR) {
                                existing.setRole(Role.DOCTOR);
                                existing.unlinkPatient(); // must not have PatientID
                                if (existing.getDoctor() == null) {
                                        Doctor doc = doctorRepository.save(new Doctor());
                                        existing.setDoctor(doc); // must have DoctorID
                                }
                        } else if (newRole == Role.PATIENT) {
                                existing.setRole(Role.PATIENT);
                                existing.setDoctor(null); // must not have DoctorID
                                if (existing.getPatient() == null) {
                                        Patient p = patientRepository.save(new Patient(LocalDate.of(2000, 1, 1)));
                                        existing.linkPatient(p); // must have PatientID
                                }
                        }

                        userRepository.saveAndFlush(existing); // ✅ trigger sees correct final state
                        return "redirect:/admin/users";
                }

                // no role change -> normal save
                userRepository.save(existing);
                return "redirect:/admin/users";
        }

        @Transactional
        @PostMapping("/users/{id}/delete")
        public String deleteUser(@PathVariable Integer id) {
                User u = userService.get(id);

                // keep references if you want to delete linked rows afterwards
                Doctor doctorToDelete = u.getDoctor();
                Patient patientToDelete = u.getPatient();

                // STEP 1: make the Users row valid for the trigger
                u.setRole(Role.ADMIN); // ADMIN requires both null -> valid neutral state
                u.setDoctor(null);
                u.unlinkPatient();

                userRepository.saveAndFlush(u); // ✅ trigger sees valid state

                // STEP 2: delete the user
                userRepository.delete(u);
                userRepository.flush();

                // OPTIONAL: delete orphaned doctor/patient rows
                // (do this only if you really want to remove them)
                if (doctorToDelete != null) {
                        doctorRepository.delete(doctorToDelete);
                }
                if (patientToDelete != null) {
                        patientRepository.delete(patientToDelete);
                }

                return "redirect:/admin/users";
        }

        // Manage Doctors
        @GetMapping("/doctors")
        public String doctors(Model model) {
                model.addAttribute("users", userService.findByRole(Role.DOCTOR));
                model.addAttribute("title", "Doctors");
                return "admin/role-list";
        }

        // Manage Patients
        @GetMapping("/patients")
        public String patients(Model model) {
                model.addAttribute("users", userService.findByRole(Role.PATIENT));
                model.addAttribute("title", "Patients");
                return "admin/role-list";
        }

        private static double round1(double v) {
                return Math.round(v * 10.0) / 10.0;
        }
}
