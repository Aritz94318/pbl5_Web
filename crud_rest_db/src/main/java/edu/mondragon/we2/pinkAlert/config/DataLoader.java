package edu.mondragon.we2.pinkAlert.config;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.UserService;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;

    public DataLoader(
            UserService userService,
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DiagnosisRepository diagnosisRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
    }

    @Override
    public void run(String... args) {

        // ========================
        // USERS
        // ========================
        if (userRepository.count() == 0) {

            Doctor d1 = doctorRepository.findById(1)
                    .orElseGet(() -> doctorRepository.save(
                            new Doctor("Javier")));

            Patient p1 = patientRepository.findById(1)
                    .orElseGet(() -> patientRepository.save(
                            new Patient("Mikel", LocalDate.of(1999, 2, 1))));

            User doctorUser = new User();
            doctorUser.setEmail("doctor1@pinkalert.com");
            doctorUser.setUsername("doctor1");
            doctorUser.setRole(Role.DOCTOR);
            doctorUser.setDoctor(d1);
            doctorUser.setPatient(null);
            userService.createUser(doctorUser, "123");

            User patientUser = new User();
            patientUser.setEmail("patient1@pinkalert.com");
            patientUser.setUsername("patient1");
            patientUser.setRole(Role.PATIENT);
            patientUser.setPatient(p1);
            patientUser.setDoctor(null);
            userService.createUser(patientUser, "123");

            User adminUser = new User();
            adminUser.setEmail("admin@pinkalert.com");
            adminUser.setUsername("admin");
            adminUser.setRole(Role.ADMIN);
            adminUser.setDoctor(null);
            adminUser.setPatient(null);
            userService.createUser(adminUser, "admin123");
        }

        // ========================
        // DIAGNOSES
        // ========================
        if (diagnosisRepository.count() == 0) {

            Doctor d1 = doctorRepository.findById(1)
                    .orElseGet(() -> doctorRepository.save(
                            new Doctor("Javier")));

            Patient p3 = patientRepository.findById(3)
                    .orElseGet(() -> patientRepository.save(
                            new Patient("Ekaitz", LocalDate.of(2004, 10, 28))));

            Diagnosis diag = new Diagnosis();
            diag.setImagePath("1.jpg");
            diag.setDate(LocalDate.of(2025, 12, 15));
            diag.setDescription("Grade 3 breast cancer.");
            diag.setUrgent(true);
            diag.setReviewed(true);
            diag.setDoctor(d1);
            diag.setPatient(p3);

            diagnosisRepository.save(diag);
        }
    }
}
