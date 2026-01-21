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

            // // --- Doctor ---
            Doctor doctor = doctorRepository.save(new Doctor("688152046"));

            User doctorUser = new User();
            doctorUser.setEmail("javier.fuentes@pinkalert.com");
            doctorUser.setUsername("javier.fuentes");
            doctorUser.setFullName("Dr. Javier Fuentes");
            doctorUser.setRole(Role.DOCTOR);
            doctorUser.setDoctor(doctor);
            doctorUser.setPatient(null);
            userService.createUser(doctorUser, "123");

            // // --- Patient ---
            Patient patient = patientRepository.save(
                    new Patient(LocalDate.of(1999, 2, 14), "625153475"));

            User patientUser = new User();
            patientUser.setEmail("maria.agirre@gmail.com");
            patientUser.setUsername("maria.agirre");
            patientUser.setFullName("María Agirre");
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
                    new Patient(LocalDate.of(1975, 10, 28), "691457821"));

            // Create the linked User row for that patient
            User patientUser2 = new User();
            patientUser2.setEmail("maitediaz75@pinkalert.com");
            patientUser2.setUsername("maite.diaz");
            patientUser2.setFullName("Maite Díaz");
            patientUser2.setRole(Role.PATIENT);
            patientUser2.setDoctor(null);
            patientUser2.linkPatient(patient);
            userService.createUser(patientUser2, "123");

            // // Diagnosis now points to a fully linked patient

            // // No revisado no urgente
            // Diagnosis diag = new Diagnosis();
            // diag.setImagePath("2.dcm");
            // diag.setImage2Path("1.jpg");
            // diag.setImage3Path("1.jpg");
            // diag.setImage4Path("1.jpg");
            // diag.setDate(LocalDate.of(2026, 01, 19));
            // diag.setDescription("Grade 3 breast cancer.");
            // diag.setUrgent(false);
            // diag.setReviewed(false);
            // diag.setDoctor(doctor);
            // diag.setPatient(patient);

            // Revisado no urgente
            // Diagnosis diag2 = new Diagnosis();
            // diag2.setImagePath("2.dcm");
            // diag2.setImage2Path("1.jpg");
            // diag2.setImage3Path("1.jpg");
            // diag2.setImage4Path("1.jpg");

            // diag2.setDate(LocalDate.of(2026, 01, 19));
            // diag2.setDescription("Grade 3 breast cancer.");
            // diag2.setUrgent(false);
            // diag2.setReviewed(true);
            // diag2.setDoctor(doctor);
            // diag2.setPatient(patient);

            // // Revisado urgente
            // Diagnosis diag3 = new Diagnosis();
            // diag3.setImagePath("2.dcm");
            // diag3.setImage2Path("1.jpg");
            // diag3.setImage3Path("1.jpg");
            // diag3.setImage4Path("1.jpg");

            // diag3.setDate(LocalDate.of(2026, 01, 19));
            // diag3.setDescription("Grade 3 breast cancer.");
            // diag3.setUrgent(true);
            // diag3.setReviewed(true);
            // diag3.setDoctor(doctor);
            // diag3.setPatient(patient);

            // // No revisado urgente
            // Diagnosis diag4 = new Diagnosis();
            // diag4.setImagePath("2.dcm");
            // diag4.setImage2Path("1.jpg");
            // diag4.setImage3Path("1.jpg");
            // diag4.setImage4Path("1.jpg");
            // diag4.setDate(LocalDate.of(2026, 01, 19));
            // diag4.setDescription("Grade 3 breast cancer.");
            // diag4.setUrgent(true);
            // diag4.setReviewed(false);
            // diag4.setDoctor(doctor);
            // diag4.setPatient(patient);

            // diagnosisRepository.save(diag);
            // diagnosisRepository.save(diag2);
            // diagnosisRepository.save(diag3);
            // diagnosisRepository.save(diag4);
        }
    }
}
