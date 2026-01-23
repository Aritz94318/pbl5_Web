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
        if (userRepository.count() == 0) {
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
    }
}
