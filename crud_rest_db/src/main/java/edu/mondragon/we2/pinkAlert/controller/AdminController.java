package edu.mondragon.we2.pinkAlert.controller;

import jakarta.transaction.Transactional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

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
import edu.mondragon.we2.pinkAlert.service.SimulationService;
import edu.mondragon.we2.pinkAlert.service.UserService;
import edu.mondragon.we2.pinkAlert.model.AiPrediction;
import edu.mondragon.we2.pinkAlert.model.FinalResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/admin")
public class AdminController {

        private final PatientRepository patientRepository;
        private final DiagnosisRepository diagnosisRepository;
        private final DoctorRepository doctorRepository;
        private final UserRepository userRepository;
        private final UserService userService;
        private final AiClientService aiClientService;
        private final SimulationService simulationService;
        @Value("${pinkalert.storage.dir}")
        private String storageDir;

        public AdminController(PatientRepository patientRepository,
                        DiagnosisRepository diagnosisRepository,
                        UserRepository userRepository,
                        UserService userService,
                        DoctorRepository doctorRepository,
                        AiClientService aiClientService, DiagnosisService diagnosisService,
                        DoctorService doctorService, SimulationService simulationService) {

                this.patientRepository = patientRepository;
                this.diagnosisRepository = diagnosisRepository;
                this.userRepository = userRepository;
                this.userService = userService;
                this.doctorRepository = doctorRepository;
                this.aiClientService = aiClientService;
                this.simulationService = simulationService;
        }

        private static boolean isFinalized(Diagnosis d) {
                if (d == null)
                        return false;
                if (!d.isReviewed())
                        return false;
                FinalResult fr = d.getFinalResult();
                if (fr == null)
                        return false;
                return fr != FinalResult.PENDING;
        }

        private static FinalResult mapAiToFinal(AiPrediction ai) {
                if (ai == null)
                        return null;
                return switch (ai) {
                        case MALIGNANT -> FinalResult.MALIGNANT;
                        case BENIGN -> FinalResult.BENIGN;
                        default -> null;
                };
        }

        @GetMapping("/users")
        public String users(
                        @RequestParam(name = "role", required = false) Role role,
                        Model model) {

                List<User> users = (role == null)
                                ? userService.findAll()
                                : userService.findByRole(role);

                model.addAttribute("users", users);
                model.addAttribute("roleFilter", role);
                model.addAttribute("roles", Role.values());

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
                        @RequestParam(name = "rawPassword", required = false) String hash,
                        @RequestParam(name = "doctorPhone", required = false) String doctorPhone,
                        @RequestParam(name = "patientPhone", required = false) String patientPhone,
                        @RequestParam(name = "patientBirthDate", required = false) String patientBirthDate) {

                if (hash == null || hash.isBlank())
                        hash = "123";

                if (user.getRole() == Role.DOCTOR) {
                        Doctor doc = new Doctor();
                        doc.setPhone(doctorPhone);
                        doc = doctorRepository.save(doc);

                        user.setDoctor(doc);
                        user.setPatient(null);

                } else if (user.getRole() == Role.PATIENT) {
                        LocalDate bd = (patientBirthDate == null || patientBirthDate.isBlank())
                                        ? LocalDate.of(2000, 1, 1)
                                        : LocalDate.parse(patientBirthDate);

                        Patient p = new Patient();
                        p.setBirthDate(bd);
                        p.setPhone(patientPhone);
                        p = patientRepository.save(p);

                        user.linkPatient(p);
                        user.setDoctor(null);

                } else {
                        user.unlinkDoctor();
                        user.unlinkPatient();
                }

                userService.createUser(user, hash);
                return "redirect:/admin/users";
        }

        @GetMapping("/users/{id}/edit")
        public String editUser(
                        @PathVariable Integer id,
                        @RequestParam(name = "roleFilter", required = false) Role role,
                        Model model) {

                model.addAttribute("user", userService.get(id));
                model.addAttribute("roles", Role.values());

                model.addAttribute("roleFilter", role);

                return "admin/user-form";
        }

        @Transactional
        @PostMapping("/users/{id}")
        public String updateUser(@PathVariable Integer id,
                        @ModelAttribute("user") User posted,
                        @RequestParam(name = "roleFilter", required = false) Role role,
                        @RequestParam(name = "rawPassword", required = false) String rawPassword,
                        @RequestParam(name = "doctorPhone", required = false) String doctorPhone,
                        @RequestParam(name = "patientPhone", required = false) String patientPhone,
                        @RequestParam(name = "patientBirthDate", required = false) String patientBirthDate) {

                User existing = userService.get(id);

                existing.setUsername(posted.getUsername());
                existing.setEmail(posted.getEmail());
                existing.setFullName(posted.getFullName());

                if (rawPassword != null && !rawPassword.isBlank()) {
                        userService.setPassword(existing, rawPassword);
                }

                Role oldRole = existing.getRole();
                Role newRole = posted.getRole();

                if (oldRole == newRole) {

                        if (newRole == Role.DOCTOR && existing.getDoctor() != null) {
                                existing.getDoctor().setPhone(doctorPhone);
                                doctorRepository.save(existing.getDoctor());
                        }

                        if (newRole == Role.PATIENT && existing.getPatient() != null) {
                                if (patientBirthDate != null && !patientBirthDate.isBlank()) {
                                        existing.getPatient().setBirthDate(LocalDate.parse(patientBirthDate));
                                }
                                existing.getPatient().setPhone(patientPhone);
                                patientRepository.save(existing.getPatient());
                        }

                        userService.save(existing);
                        return "redirect:/admin/users";
                }
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

                        Doctor doc = existing.getDoctor();
                        if (doc == null)
                                doc = new Doctor();
                        doc.setPhone(doctorPhone);
                        doc = doctorRepository.save(doc);

                        existing.setDoctor(doc);

                } else if (newRole == Role.PATIENT) {
                        existing.setRole(Role.PATIENT);
                        existing.setDoctor(null);

                        LocalDate bd = (patientBirthDate == null || patientBirthDate.isBlank())
                                        ? LocalDate.of(2000, 1, 1)
                                        : LocalDate.parse(patientBirthDate);

                        Patient p = existing.getPatient();
                        if (p == null)
                                p = new Patient();
                        p.setBirthDate(bd);
                        p.setPhone(patientPhone);
                        p = patientRepository.save(p);

                        existing.linkPatient(p);
                }

                userRepository.saveAndFlush(existing);
                return "redirect:/admin/users" + (role != null ? "?role=" + role.name() : "");
        }

        @Transactional
        @PostMapping("/users/{id}/delete")
        public String deleteUser(@PathVariable Integer id,
                        @RequestParam(name = "roleFilter", required = false) Role role) {
                User u = userService.get(id);

                Doctor doctorToDelete = u.getDoctor();
                Patient patientToDelete = u.getPatient();

                u.setRole(Role.ADMIN);
                u.setDoctor(null);
                u.unlinkPatient();

                userRepository.saveAndFlush(u);

                userRepository.delete(u);
                userRepository.flush();

                if (doctorToDelete != null) {
                        doctorRepository.delete(doctorToDelete);
                }
                if (patientToDelete != null) {
                        patientRepository.delete(patientToDelete);
                }

                return "redirect:/admin/users" + (role != null ? "?role=" + role.name() : "");
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

        @GetMapping("/simulation")
        public String simulationPage(Model model) {
                model.addAttribute("numPatients", 1);
                model.addAttribute("numDoctors", 1);
                model.addAttribute("numMachines", 1);
                return "admin/simulation";
        }

        @PostMapping("/simulation/modify")
        public ResponseEntity<Void> modify(@RequestParam int numPatients, @RequestParam int numDoctors,
                        @RequestParam int numMachines) throws IOException, ProcessingException {
                simulationService.modify(numPatients, numDoctors, numMachines);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/simulation/start")
        public ResponseEntity<Void> start() {

                simulationService.start();
                return ResponseEntity.ok().build();
        }

        private static double round1(double v) {
                return Math.round(v * 10.0) / 10.0;
        }

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
                        Model model) {

                try {
                        if (patientId == null) {
                                model.addAttribute("error", "You must select a patient.");
                                model.addAttribute("today", LocalDate.now().toString());
                                return "admin/diagnosis-form";
                        }

                        Patient patient = patientRepository.findById(patientId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Patient not found: " + patientId));

                        List<String> dicomUrls = List.of(dicomUrl, dicomUrl2, dicomUrl3, dicomUrl4);

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

                        String email = (patient.getUser() != null) ? patient.getUser().getEmail() : null;

                        if (email == null || email.isBlank()) {
                                model.addAttribute("error",
                                                "Selected patient has no email linked. Please edit the user and add an email.");
                                model.addAttribute("today", LocalDate.now().toString());
                                return "admin/diagnosis-form";
                        }

                        if (description == null || description.trim().isEmpty()) {
                                description = "Pending final diagnosis";
                        }

                        LocalDate date = LocalDate.parse(dateStr);

                        Doctor doctor = doctorRepository.findAll().stream().findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "No doctors exist. Create one doctor first."));

                        Diagnosis diag = new Diagnosis();
                        diag.setAiPrediction(AiPrediction.PENDING);
                        diag.setPatient(patient);
                        diag.setDoctor(doctor);
                        diag.setReviewed(false);
                        diag.setDescription(description);
                        diag.setDate(date);

                        diag.setImagePath(dicomUrl);
                        diag.setImage2Path(dicomUrl2);
                        diag.setImage3Path(dicomUrl3);
                        diag.setImage4Path(dicomUrl4);

                        diag = diagnosisRepository.saveAndFlush(diag);

                        String previewsDir = "previews";
                        Path baseDir = Paths.get(storageDir)
                                        .toAbsolutePath()
                                        .normalize();

                        Files.createDirectories(baseDir);

                        Path previewsPath = Files.createDirectories(baseDir.resolve("previews"));
                        Path tmpDicomPath = Files.createDirectories(baseDir.resolve("dicom"));

                        Files.createDirectories(tmpDicomPath);

                        List<String> urls = dicomUrls;
                        for (int i = 1; i <= 4; i++) {
                                String u = urls.get(i - 1);

                                Path dicomFile = tmpDicomPath.resolve("diag_" + diag.getId() + "_" + i + ".dcm");
                                downloadToFile(u, dicomFile);

                                File outPng = previewsPath.resolve("diag_" + diag.getId() + "_" + i + ".png").toFile();
                                File f = dicomFile.toFile();
                                System.out.println("DICOM exists: " + Files.exists(dicomFile));
                                System.out.println("DICOM size: " + Files.size(dicomFile));
                                System.out.println("Ruta absoluta: " + f.getAbsolutePath());
                                System.out.println("Ruta canonical (real): " + f.getCanonicalPath());
                                edu.mondragon.we2.pinkAlert.utils.DicomToPngConverter.convert(dicomFile.toFile(),
                                                outPng);

                                String publicPreviewPath = previewsDir + "/diag_" + diag.getId() + "_" + i + ".png";

                                if (i == 1)
                                        diag.setPreviewPath(publicPreviewPath);
                                if (i == 2)
                                        diag.setPreview2Path(publicPreviewPath);
                                if (i == 3)
                                        diag.setPreview3Path(publicPreviewPath);
                                if (i == 4)
                                        diag.setPreview4Path(publicPreviewPath);
                        }

                        diagnosisRepository.save(diag);

                        AiPredictUrlRequest payload = new AiPredictUrlRequest(
                                        String.valueOf(diag.getId()), email, dicomUrl, dicomUrl2, dicomUrl3, dicomUrl4);
                        aiClientService.sendPredictUrl(payload);

                        return "redirect:/admin/dashboard";

                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        model.addAttribute("error", "Operation was interrupted. Please try again.");
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                } catch (Exception e) {
                        model.addAttribute("error", "Failed to create diagnosis: " + e.getMessage());
                        model.addAttribute("today", LocalDate.now().toString());
                        return "admin/diagnosis-form";
                }
        }

        private static Path downloadToFile(String url, Path target) throws IOException, InterruptedException {
                Files.createDirectories(target.getParent());

                HttpClient client = HttpClient.newBuilder()
                                .followRedirects(HttpClient.Redirect.ALWAYS)
                                .build();

                HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .GET()
                                .build();

                HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

                if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                        throw new IOException("Failed to download " + url + " status=" + resp.statusCode());
                }

                Files.write(target, resp.body());
                return target;

        }

        @GetMapping("/dashboard")
        public String dashboard(Model model) {

                long totalScreenings = diagnosisRepository.count();
                List<Diagnosis> all = diagnosisRepository.findAll();

                addBasicStats(model, all, totalScreenings);
                addAiStats(model, all);
                addDailyStats(model, all);

                return "dashboard";
        }

        private void addBasicStats(Model model, List<Diagnosis> all, long totalScreenings) {

                long completed = all.stream()
                                .filter(AdminController::isFinalized)
                                .count();

                long positiveCount = all.stream()
                                .filter(AdminController::isFinalized)
                                .filter(d -> d.getFinalResult() == FinalResult.MALIGNANT)
                                .count();

                long pendingCount = all.stream()
                                .filter(d -> !isFinalized(d))
                                .count();

                double completionRate = totalScreenings == 0
                                ? 0.0
                                : round1((completed * 100.0) / totalScreenings);

                double positiveRate = completed == 0
                                ? 0.0
                                : round1((positiveCount * 100.0) / completed);

                model.addAttribute("completedCount", completed);
                model.addAttribute("pendingCount", pendingCount);
                model.addAttribute("completionRate", completionRate);
                model.addAttribute("positiveRate", positiveRate);
        }

        private void addAiStats(Model model, List<Diagnosis> all) {

                long aiAgreeCount = 0;
                long aiMismatchCount = 0;
                long aiMissingCount = 0;
                long aiNotComparableCount = 0;

                for (Diagnosis d : all) {
                        AiPrediction ai = d.getAiPrediction();
                        FinalResult doctor = d.getFinalResult();

                        if (ai == null) {
                                aiMissingCount++;
                                continue;
                        }

                        if (!isFinalized(d) || doctor == FinalResult.INCONCLUSIVE) {
                                aiNotComparableCount++;
                                continue;
                        }

                        FinalResult aiAsFinal = mapAiToFinal(ai);
                        if (aiAsFinal == null) {
                                aiNotComparableCount++;
                                continue;
                        }

                        if (aiAsFinal == doctor)
                                aiAgreeCount++;
                        else
                                aiMismatchCount++;
                }

                model.addAttribute("aiAgreeCount", aiAgreeCount);
                model.addAttribute("aiMismatchCount", aiMismatchCount);
                model.addAttribute("aiMissingCount", aiMissingCount);
                model.addAttribute("aiNotComparableCount", aiNotComparableCount);
        }

        private void addDailyStats(Model model, List<Diagnosis> all) {

                LocalDate today = LocalDate.now();
                LocalDate start = today.minusDays(6);

                Map<LocalDate, Long> totalByDate = all.stream()
                                .filter(d -> d.getDate() != null)
                                .filter(d -> !d.getDate().isBefore(start) && !d.getDate().isAfter(today))
                                .collect(Collectors.groupingBy(Diagnosis::getDate, Collectors.counting()));

                Map<LocalDate, Long> completedByDate = all.stream()
                                .filter(AdminController::isFinalized)
                                .filter(d -> d.getDate() != null)
                                .filter(d -> !d.getDate().isBefore(start) && !d.getDate().isAfter(today))
                                .collect(Collectors.groupingBy(Diagnosis::getDate, Collectors.counting()));

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

                List<String> labels = new ArrayList<>();
                List<Long> totals = new ArrayList<>();
                List<Long> completes = new ArrayList<>();

                for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
                        labels.add("'" + d.format(fmt) + "'");
                        totals.add(totalByDate.getOrDefault(d, 0L));
                        completes.add(completedByDate.getOrDefault(d, 0L));
                }

                model.addAttribute("labelsJs", String.join(",", labels));
                model.addAttribute("totalsJs", totals.stream().map(String::valueOf).collect(Collectors.joining(",")));
                model.addAttribute("completesJs",
                                completes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
}
