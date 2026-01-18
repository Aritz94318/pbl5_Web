package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.AiClientService;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.DoctorService;
import edu.mondragon.we2.pinkAlert.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
        private final AiClientService aiClientService;
        private final DiagnosisService diagnosisService;
        private final DoctorService doctorService;

        public AdminController(PatientRepository patientRepository,
                        DiagnosisRepository diagnosisRepository,
                        UserRepository userRepository,
                        UserService userService,
                        DoctorRepository doctorRepository,
                        AiClientService aiClientService, DiagnosisService diagnosisService,
                        DoctorService doctorService) {
                this.patientRepository = patientRepository;
                this.diagnosisRepository = diagnosisRepository;
                this.userRepository = userRepository;
                this.userService = userService;
                this.doctorRepository = doctorRepository;
                this.aiClientService = aiClientService;
                this.diagnosisService = diagnosisService;
                this.doctorService = doctorService;
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
        // USERS CRUD (UNCHANGED)
        // ---------------------------

        @GetMapping("/users")
        public String users(Model model) {
                model.addAttribute("users", userService.findAll());
                return "admin/users";
        }

        @GetMapping("/users/new")
        public String newUser(Model model) {
                model.addAttribute("user", new User());
                model.addAttribute("roles", Role.values());
                return "admin/user-form";
        }

        @PostMapping("/users")
        public String createUser(@ModelAttribute("user") User user,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword) {

                if (rawPassword == null || rawPassword.isBlank()) {
                        rawPassword = "123";
                }

                if (user.getRole() == Role.DOCTOR) {
                        Doctor doc = doctorRepository.save(new Doctor());
                        user.setDoctor(doc);
                        user.setPatient(null);
                } else if (user.getRole() == Role.PATIENT) {
                        Patient p = patientRepository.save(new Patient(LocalDate.of(2000, 1, 1)));
                        user.linkPatient(p);
                        user.setDoctor(null);
                } else {
                        user.unlinkDoctor();
                        user.unlinkPatient();
                }

                userService.createUser(user, rawPassword);
                return "redirect:/admin/users";
        }

        @GetMapping("/users/{id}/edit")
        public String editUser(@PathVariable Integer id, Model model) {
                model.addAttribute("user", userService.get(id));
                model.addAttribute("roles", Role.values());
                return "admin/user-form";
        }

        @PostMapping("/users/{id}")
        public String updateUser(@PathVariable Integer id,
                        @ModelAttribute("user") User posted,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword) {

                User existing = userService.get(id);

                existing.setUsername(posted.getUsername());
                existing.setEmail(posted.getEmail());
                existing.setFullName(posted.getFullName());

                Role oldRole = existing.getRole();
                Role newRole = posted.getRole();

                if (rawPassword != null && !rawPassword.isBlank()) {
                        userService.setPassword(existing, rawPassword);
                }

                if (oldRole != newRole) {

                        existing.setRole(Role.ADMIN);
                        existing.setDoctor(null);
                        existing.unlinkPatient();
                        userRepository.saveAndFlush(existing);

                        if (newRole == Role.ADMIN) {
                                existing.setRole(Role.ADMIN);
                                existing.setDoctor(null);
                                existing.unlinkPatient();
                        } else if (newRole == Role.DOCTOR) {
                                existing.setRole(Role.DOCTOR);
                                existing.unlinkPatient();
                                if (existing.getDoctor() == null) {
                                        Doctor doc = doctorRepository.save(new Doctor());
                                        existing.setDoctor(doc);
                                }
                        } else if (newRole == Role.PATIENT) {
                                existing.setRole(Role.PATIENT);
                                existing.setDoctor(null);
                                if (existing.getPatient() == null) {
                                        Patient p = patientRepository.save(new Patient(LocalDate.of(2000, 1, 1)));
                                        existing.linkPatient(p);
                                }
                        }

                        userRepository.saveAndFlush(existing);
                        return "redirect:/admin/users";
                }
                userService.save(existing);

                return "redirect:/admin/users";
        }

        @PostMapping("/users/{id}/delete")
        public String deleteUser(@PathVariable Integer id) {
                userService.deleteUserCompletely(id);

                return "redirect:/admin/users";
        }

        @GetMapping("/doctors")
        public String doctors(Model model) {
                model.addAttribute("users", userService.findByRole(Role.DOCTOR));
                model.addAttribute("title", "Doctors");
                return "admin/role-list";
        }

        @GetMapping("/patients")
        public String patients(Model model) {
                model.addAttribute("users", userService.findByRole(Role.PATIENT));
                model.addAttribute("title", "Patients");
                return "admin/role-list";
        }

        // Provisional
        @GetMapping("/simulation")
        public String simulationPage(Model model) {
                model.addAttribute("numPatients", 2);
                model.addAttribute("numDoctors", 1);
                model.addAttribute("numMachines", 2);
                return "admin/simulation";
        }

        @PostMapping("/simulation/modify")
        public ResponseEntity<Void>  modify(@RequestParam int numPatients,
                        @RequestParam int numDoctors,
                        @RequestParam int numMachines) {

                RestTemplate rt = new RestTemplate();
                String url = "http://localhost:8081/Simulation/modify"; // tu servidor de simulación

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                // JSON que mandas
                String json = """
                                {
                                        "numPatients": %d,
                                        "numDoctors": %d,
                                        "numMachines": %d
                                }
                                """.formatted(numPatients, numDoctors, numMachines);

                HttpEntity<String> request = new HttpEntity<>(json, headers);

                rt.exchange(url, HttpMethod.PUT, request, String.class);

                return ResponseEntity.ok().build();

        }
        
        @PostMapping("/simulation/start")
        public ResponseEntity<Void>  start() {

                RestTemplate rt = new RestTemplate();
                String url = "http://localhost:8081/Simulation/start"; // tu servidor de simulación

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);


                HttpEntity<String> request = new HttpEntity<>("start", headers);

                rt.exchange(url, HttpMethod.POST, request, String.class);

                return ResponseEntity.ok().build();

        }

        private static double round1(double v) {
                return Math.round(v * 10.0) / 10.0;
        }


        // ---------------------------
        // MACHINE SIMULATOR - NEW DIAGNOSIS
        // ---------------------------

        @GetMapping("/diagnoses/new")
        public String newDiagnosisForm(Model model) {
                model.addAttribute("today", LocalDate.now().toString());
                return "admin/diagnosis-form";
        }

        @GetMapping(value = "/patients/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        public List<Map<String, Object>> suggestPatients(@RequestParam(name = "q", required = false) String q) {
                String query = (q == null) ? "" : q.trim().toLowerCase();
                if (query.isBlank())
                        return List.of();

                List<Patient> all = patientRepository.findAll();

                return all.stream()
                                .map(p -> {
                                        String name = null;
                                        if (p.getUser() != null)
                                                name = p.getUser().getFullName();
                                        if (name == null || name.isBlank())
                                                name = "Patient PT-" + p.getId();

                                        Map<String, Object> item = new HashMap<>();
                                        item.put("id", p.getId());
                                        item.put("label", name);
                                        return item;
                                })
                                .filter(item -> ((String) item.get("label")).toLowerCase().startsWith(query))
                                .limit(10)
                                .collect(Collectors.toList());
        }

        @PostMapping("/diagnoses")

        public String createDiagnosis(
                        @RequestParam("patientId") Integer patientId,
                        @RequestParam("dicomUrl") String dicomUrl,
                        @RequestParam("dicomUrl2") String dicomUrl2,
                        @RequestParam("dicomUrl3") String dicomUrl3,
                        @RequestParam("dicomUrl4") String dicomUrl4,
                        @RequestParam("date") String dateStr,
                        @RequestParam(name = "description", required = false) String description,
                        @RequestParam(name = "email", required = false) String email,
                        Model model) {

                if (patientId == null) {
                        model.addAttribute("error", "You must select a patient.");
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                }

                Patient patient = patientRepository.findById(patientId)
                                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

                List<String> dicomUrls = List.of(
                                dicomUrl,
                                dicomUrl2,
                                dicomUrl3,
                                dicomUrl4);

                if (dicomUrls.stream().anyMatch(u -> u == null || u.isBlank())) {
                        model.addAttribute("error", "All 4 DICOM URLs are required.");
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                }

                boolean invalidDriveUrl = dicomUrls.stream().anyMatch(u -> !u.contains("drive.google.com") ||
                                !u.contains("uc?export=download&id="));

                if (invalidDriveUrl) {
                        model.addAttribute("error",
                                        "All DICOM URLs must be public Google Drive direct-download links (uc?export=download&id=...).");
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                }

                // Email fallback: if not provided, try to use patient user email
                if (email == null || email.isBlank()) {
                        if (patient.getUser() != null && patient.getUser().getEmail() != null) {
                                email = patient.getUser().getEmail();
                        }
                }
                if (email == null || email.isBlank()) {
                        model.addAttribute("error",
                                        "Email is required (add it in the form or link patient user email).");
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                }

                if (description == null || description.trim().isEmpty()) {
                        description = "Pending AI analysis"; // or "" if you prefer, but keep NOT NULL
                }

                LocalDate date = LocalDate.parse(dateStr);

                // Create Diagnosis
                Diagnosis diag = new Diagnosis();
                diag.setPatient(patient);
                diag.setDoctor(null);
                diag.setReviewed(false);
                diag.setUrgent(false); // AI will update this later via webhook
                diag.setDescription(description);
                diag.setDate(date);

                // Store the DICOM URL in ImagePath (or make a dedicated column)
                diag.setImagePath(dicomUrl);
                diag.setImage2Path(dicomUrl2);
                diag.setImage3Path(dicomUrl3);
                diag.setImage4Path(dicomUrl4);
                diagnosisService.save(diag);

                // Call AI
                AiPredictUrlRequest payload = new AiPredictUrlRequest(String.valueOf(diag.getId()), email, dicomUrl,dicomUrl2,dicomUrl3,dicomUrl4);

                aiClientService.sendPredictUrl(payload);

                return "redirect:/admin/dashboard";
        }

        private static String getBaseUrl(HttpServletRequest request) {
                // Example: https://yourdomain.com (no trailing slash)
                String scheme = request.getScheme();
                String host = request.getServerName();
                int port = request.getServerPort();

                boolean isDefaultPort = (scheme.equals("http") && port == 80)
                                || (scheme.equals("https") && port == 443);

                StringBuilder sb = new StringBuilder();
                sb.append(scheme).append("://").append(host);
                if (!isDefaultPort)
                        sb.append(":").append(port);

                // include contextPath if you deploy under a subpath
                sb.append(request.getContextPath());

                return sb.toString();
        }

        private static boolean looksLikeDicom(MultipartFile file) {
                try (InputStream is = file.getInputStream()) {
                        byte[] header = is.readNBytes(132);
                        if (header.length < 132)
                                return false;

                        // Standard DICOM: "DICM" at offset 128
                        return header[128] == 'D' && header[129] == 'I' && header[130] == 'C' && header[131] == 'M';
                } catch (Exception e) {
                        return false;
                }
        }
}
