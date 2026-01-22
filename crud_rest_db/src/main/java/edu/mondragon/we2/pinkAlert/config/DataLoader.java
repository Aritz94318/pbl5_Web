package edu.mondragon.we2.pinkAlert.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.UserService;

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

            Doctor doctor = doctorRepository.save(new Doctor("688152046"));

            User doctorUser = new User();
            doctorUser.setEmail("javier.fuentes@pinkalert.com");
            doctorUser.setUsername("javier.fuentes");
            doctorUser.setFullName("Dr. Javier Fuentes");
            doctorUser.setRole(Role.DOCTOR);
            doctorUser.setDoctor(doctor);
            doctorUser.setPatient(null);
            userService.createUser(doctorUser, "123");

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

            User adminUser = new User();
            adminUser.setEmail("admin@pinkalert.com");
            adminUser.setUsername("admin");
            adminUser.setFullName("System Administrator");
            adminUser.setRole(Role.ADMIN);
            adminUser.setDoctor(null);
            adminUser.setPatient(null);
            userService.createUser(adminUser, "admin123");
        }

        if (diagnosisRepository.count() == 0) {


            Patient patient = patientRepository.save(
                    new Patient(LocalDate.of(1975, 10, 28), "691457821"));

            User patientUser2 = new User();
            patientUser2.setEmail("maitediaz75@pinkalert.com");
            patientUser2.setUsername("maite.diaz");
            patientUser2.setFullName("Maite Díaz");
            patientUser2.setRole(Role.PATIENT);
            patientUser2.setDoctor(null);
            patientUser2.linkPatient(patient);
            userService.createUser(patientUser2, "123");

    
        }
    }
}
