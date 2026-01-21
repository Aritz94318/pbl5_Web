package edu.mondragon.we2.pinkalert.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.mondragon.we2.pinkalert.model.*;
import edu.mondragon.we2.pinkalert.repository.*;
import edu.mondragon.we2.pinkalert.service.UserService;

import java.time.LocalDate;

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
    void testRun_WhenNoUsersExist_CreatesAllUsers(){
    
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L); 
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        dataLoader.run();

        verify(doctorRepository).save(any(Doctor.class));
        verify(patientRepository, atLeastOnce()).save(any(Patient.class));
        verify(userService, times(3)).createUser(any(User.class), anyString());
    }

 
    @Test
    void testRun_WhenDiagnosesExist_SkipsDiagnosisCreation()  {
       
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(1L);

        dataLoader.run();

        verify(doctorRepository, never()).findAll();
        verify(patientRepository, never()).save(any(Patient.class));
        verify(userService, never()).createUser(any(User.class), eq("123"));
    }

    @Test
    void testRun_CreatesDoctorUserWithCorrectRole() {
      
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        Doctor mockDoctor = new Doctor("688152046");
        when(doctorRepository.save(any(Doctor.class))).thenReturn(mockDoctor);
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        dataLoader.run();

        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.DOCTOR && 
            user.getEmail().equals("javier.fuentes@pinkalert.com")
        ), eq("123"));
    }

    @Test
    void testRun_CreatesPatientUserWithCorrectRole()  {
      
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        Patient mockPatient = new Patient(LocalDate.of(1999, 2, 14), "625153475");
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        dataLoader.run();

        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.PATIENT && 
            user.getEmail().equals("maria.agirre@gmail.com")
        ), eq("123"));
    }

    @Test
    void testRun_CreatesAdminUserWithCorrectRole() {
       
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(new Doctor("688152046"));
        when(patientRepository.save(any(Patient.class))).thenReturn(
            new Patient(LocalDate.of(1999, 2, 14), "625153475")
        );

        dataLoader.run();

        verify(userService).createUser(argThat(user -> 
            user.getRole() == Role.ADMIN && 
            user.getEmail().equals("admin@pinkalert.com")
        ), eq("admin123"));
    }

  
}