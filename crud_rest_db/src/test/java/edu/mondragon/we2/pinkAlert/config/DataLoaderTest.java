package edu.mondragon.we2.pinkAlert.config;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private DiagnosisRepository diagnosisRepository;
    
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader(userService, userRepository, doctorRepository, 
                                  patientRepository, diagnosisRepository);
    }

    @Test
    void testRun_WhenNoUsersExist_CreatesAllUsers() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L); // Para saltar la segunda parte
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        // Act
        dataLoader.run();

        // Assert
        verify(doctorRepository).save(any(Doctor.class));
        verify(patientRepository, atLeastOnce()).save(any(Patient.class));
        verify(userService, times(3)).createUser(any(User.class), anyString());
    }

 
    @Test
    void testRun_WhenNoDiagnosesExist_CreatesDiagnosisPatient() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(1L); // Para saltar primera parte
        when(diagnosisRepository.count()).thenReturn(0L);
        
        Doctor mockDoctor = new Doctor("688152046");
        when(doctorRepository.findAll()).thenReturn(Collections.singletonList(mockDoctor));
        
        Patient mockPatient = new Patient(LocalDate.of(1975, 10, 28), "691457821");
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Act
        dataLoader.run();

        // Assert
        verify(patientRepository).save(any(Patient.class));
        verify(userService).createUser(any(User.class), eq("123"));
    }

    @Test
    void testRun_WhenDiagnosesExist_SkipsDiagnosisCreation() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(1L);

        // Act
        dataLoader.run();

        // Assert
        verify(doctorRepository, never()).findAll();
        verify(patientRepository, never()).save(any(Patient.class));
        verify(userService, never()).createUser(any(User.class), eq("123"));
    }

    @Test
    void testRun_CreatesDoctorUserWithCorrectRole() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        Doctor mockDoctor = new Doctor("688152046");
        when(doctorRepository.save(any(Doctor.class))).thenReturn(mockDoctor);
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        // Act
        dataLoader.run();

        // Assert
        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.DOCTOR && 
            user.getEmail().equals("javier.fuentes@pinkalert.com")
        ), eq("123"));
    }

    @Test
    void testRun_CreatesPatientUserWithCorrectRole() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        Patient mockPatient = new Patient(LocalDate.of(1999, 2, 14), "625153475");
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Act
        dataLoader.run();

        // Assert
        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.PATIENT && 
            user.getEmail().equals("maria.agirre@gmail.com")
        ), eq("123"));
    }

    @Test
    void testRun_CreatesAdminUserWithCorrectRole() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        // Act
        dataLoader.run();

        // Assert
        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.ADMIN && 
            user.getEmail().equals("admin@pinkalert.com")
        ), eq("admin123"));
    }

    @Test
    void testRun_CreatesSecondPatientForDiagnosis() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(0L);
        
        Doctor mockDoctor = new Doctor("688152046");
        when(doctorRepository.findAll()).thenReturn(Collections.singletonList(mockDoctor));
        
        Patient mockPatient = new Patient(LocalDate.of(1975, 10, 28), "691457821");
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        // Act
        dataLoader.run();

        // Assert
        verify(userService).createUser(argThat(user -> 
            user.getEmail().equals("maitediaz75@pinkalert.com") &&
            user.getPatient() != null
        ), eq("123"));
    }
}