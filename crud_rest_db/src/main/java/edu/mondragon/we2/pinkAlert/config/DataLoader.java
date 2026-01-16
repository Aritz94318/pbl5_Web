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

            // No revisado no urgente
            Diagnosis diag = new Diagnosis();
            diag.setImagePath("1.jpg");
            diag.setImage2Path("1.jpg");
            diag.setImage3Path("1.jpg");
            diag.setImage4Path("1.jpg");
            diag.setDate(LocalDate.of(2026, 01, 13));
            diag.setDescription("Grade 3 breast cancer.");
            diag.setUrgent(false);
            diag.setReviewed(false);
            diag.setDoctor(doctor);
            diag.setPatient(patient);

            // Revisado no urgente
            Diagnosis diag2 = new Diagnosis();
            diag2.setImagePath("1.jpg");
            diag.setImage2Path("1.jpg");
            diag.setImage3Path("1.jpg");
            diag.setImage4Path("1.jpg");

            diag2.setDate(LocalDate.of(2026, 01, 13));
            diag2.setDescription("Grade 3 breast cancer.");
            diag2.setUrgent(false);
            diag2.setReviewed(true);
            diag2.setDoctor(doctor);
            diag2.setPatient(patient);

            // Revisado urgente
            Diagnosis diag3 = new Diagnosis();
            diag3.setImagePath("1.jpg");
            diag.setImage2Path("1.jpg");
            diag.setImage3Path("1.jpg");
            diag.setImage4Path("1.jpg");

            diag3.setDate(LocalDate.of(2026, 01, 13));
            diag3.setDescription("Grade 3 breast cancer.");
            diag3.setUrgent(true);
            diag3.setReviewed(true);
            diag3.setDoctor(doctor);
            diag3.setPatient(patient);

            // No revisado urgente
            Diagnosis diag4 = new Diagnosis();
            diag4.setImagePath("1.jpg");
            diag.setImage2Path("1.jpg");
            diag.setImage3Path("1.jpg");
            diag.setImage4Path("1.jpg");
            diag4.setDate(LocalDate.of(2026, 01, 13));
            diag4.setDescription("Grade 3 breast cancer.");
            diag4.setUrgent(true);
            diag4.setReviewed(false);
            diag4.setDoctor(doctor);
            diag4.setPatient(patient);

            diagnosisRepository.save(diag);
            diagnosisRepository.save(diag2);
            diagnosisRepository.save(diag3);
            diagnosisRepository.save(diag4);
        }
    }
}
