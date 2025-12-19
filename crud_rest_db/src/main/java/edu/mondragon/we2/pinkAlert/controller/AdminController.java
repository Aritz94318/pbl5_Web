package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.UserService;

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
        private final UserRepository userRepository;
        private final UserService userService;

        public AdminController(PatientRepository patientRepository,
                        DiagnosisRepository diagnosisRepository,
                        UserRepository userRepository,
                        UserService userService) {
                this.patientRepository = patientRepository;
                this.diagnosisRepository = diagnosisRepository;
                this.userRepository = userRepository;
                this.userService = userService;
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
                // If you have a password field in the form named rawPassword, hash it safely:
                if (rawPassword != null && !rawPassword.isBlank()) {
                        userService.createUser(user, rawPassword);
                } else {
                        // fallback: requires user.passwordHash already set
                        userService.save(user);
                }
                return "redirect:/admin/users";
        }

        // Edit User (form)
        @GetMapping("/users/{id}/edit")
        public String editUser(@PathVariable Integer id, Model model) {
                model.addAttribute("user", userService.get(id));
                model.addAttribute("roles", Role.values());
                return "admin/user-form";
        }

        // Update User (submit)
        @PostMapping("/users/{id}")
        public String updateUser(@PathVariable Integer id,
                        @ModelAttribute("user") User user,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword) {
                user.setId(id);

                if (rawPassword != null && !rawPassword.isBlank()) {
                        userService.createUser(user, rawPassword); // hashes then saves
                } else {
                        userService.save(user);
                }

                return "redirect:/admin/users";
        }

        // Delete User
        @PostMapping("/users/{id}/delete")
        public String deleteUser(@PathVariable Integer id) {
                userService.delete(id);
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
