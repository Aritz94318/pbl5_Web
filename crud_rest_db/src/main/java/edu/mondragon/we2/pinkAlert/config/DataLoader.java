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

            // --- Doctor ---
            Doctor doctor = doctorRepository.save(new Doctor());

            User doctorUser = new User();
            doctorUser.setEmail("doctor1@pinkalert.com");
            doctorUser.setUsername("doctor1");
            doctorUser.setFullName("Dr. Javier Martínez");
            doctorUser.setRole(Role.DOCTOR);
            doctorUser.setDoctor(doctor);
            doctorUser.setPatient(null);
            userService.createUser(doctorUser, "123");

            // --- Patient ---
            Patient patient = patientRepository.save(
                    new Patient(LocalDate.of(1999, 2, 1)));

            User patientUser = new User();
            patientUser.setEmail("patient1@pinkalert.com");
            patientUser.setUsername("patient1");
            patientUser.setFullName("Mikel Etxeberria");
            patientUser.setRole(Role.PATIENT);
            patientUser.setPatient(patient);
            patientUser.setDoctor(null);
            userService.createUser(patientUser, "123");

            // --- Admin ---
            User adminUser = new User();
            adminUser.setEmail("admin@pinkalert.com");
            adminUser.setUsername("admin");
            adminUser.setFullName("System Administrator");
            adminUser.setRole(Role.ADMIN);
            adminUser.setDoctor(null);
            adminUser.setPatient(null);
            userService.createUser(adminUser, "admin123");
        }

        // ========================
        // DIAGNOSES
        // ========================
        if (diagnosisRepository.count() == 0) {

            Doctor doctor = doctorRepository.findAll().get(0);

            // Create a patient
            Patient patient = patientRepository.save(
                    new Patient(LocalDate.of(2004, 10, 28)));

            // Create the linked User row for that patient
            User patientUser2 = new User();
            patientUser2.setEmail("patient2@pinkalert.com");
            patientUser2.setUsername("patient2");
            patientUser2.setFullName("Ekaitz Aramburu");
            patientUser2.setRole(Role.PATIENT);
            patientUser2.setDoctor(null);

            // ✅ LINK BOTH SIDES HERE
            patientUser2.linkPatient(patient);

            userService.createUser(patientUser2, "123");

            // Diagnosis now points to a fully linked patient
            Diagnosis diag = new Diagnosis();
            diag.setImagePath("1.jpg");
            diag.setDate(LocalDate.of(2025, 12, 29));
            diag.setDescription("Grade 3 breast cancer.");
            diag.setUrgent(true);
            diag.setReviewed(true);
            diag.setDoctor(doctor);
            diag.setPatient(patient);

            diagnosisRepository.save(diag);
        }
    }
}
