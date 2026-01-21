package edu.mondragon.we2.pinkAlert.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.UserService;

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
    
    @InjectMocks
    private DataLoader dataLoader;
    
    private Doctor mockDoctor;
    private Patient mockPatient;
    private User mockDoctorUser;
    private User mockPatientUser;
    private User mockAdminUser;

    @BeforeEach
    void setUp() {
        // Configurar mocks básicos
        mockDoctor = new Doctor("688152046");
        mockDoctor.setId(1);
        
        mockPatient = new Patient(LocalDate.of(1999, 2, 14), "625153475");
        mockPatient.setId(1);
        
        mockDoctorUser = new User();
        mockDoctorUser.setId(1);
        mockDoctorUser.setEmail("javier.fuentes@pinkalert.com");
        mockDoctorUser.setUsername("javier.fuentes");
        mockDoctorUser.setFullName("Dr. Javier Fuentes");
        mockDoctorUser.setRole(Role.DOCTOR);
        mockDoctorUser.setDoctor(mockDoctor);
        
        mockPatientUser = new User();
        mockPatientUser.setId(2);
        mockPatientUser.setEmail("maria.agirre@gmail.com");
        mockPatientUser.setUsername("maria.agirre");
        mockPatientUser.setFullName("María Agirre");
        mockPatientUser.setRole(Role.PATIENT);
        mockPatientUser.setPatient(mockPatient);
        
        mockAdminUser = new User();
        mockAdminUser.setId(3);
        mockAdminUser.setEmail("admin@pinkalert.com");
        mockAdminUser.setUsername("admin");
        mockAdminUser.setFullName("System Administrator");
        mockAdminUser.setRole(Role.ADMIN);
    }

    @Test
    void testDataLoaderImplementsCommandLineRunner() {
        // Arrange & Act
        boolean implementsInterface = CommandLineRunner.class.isAssignableFrom(DataLoader.class);
        
        // Assert
        assertTrue(implementsInterface, "DataLoader debe implementar CommandLineRunner");
    }

    @Test
    void testRun_WhenUsersExist_DoesNotCreateUsers() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(3L); // Ya existen usuarios
        when(diagnosisRepository.count()).thenReturn(4L); // Ya existen diagnósticos
        
        // Act
        dataLoader.run();
        
        // Assert
        verify(userRepository, times(1)).count();
        verify(doctorRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
        verify(userService, never()).createUser(any(), any());
        verify(diagnosisRepository, times(1)).count();
    }

    @Test
    void testConstructor_InjectsAllDependencies() {
        // Arrange
        DataLoader loader = new DataLoader(userService, userRepository, 
            doctorRepository, patientRepository, diagnosisRepository);
        
        // Act & Assert - No debería lanzar excepción
        assertNotNull(loader);
        
        // Podemos verificar que los campos se asignan correctamente usando reflection
        try {
            java.lang.reflect.Field userServiceField = DataLoader.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            assertSame(userService, userServiceField.get(loader));
        } catch (Exception e) {
            fail("Error al acceder a campos por reflection: " + e.getMessage());
        }
    }

    @Test
    void testDataLoader_EmptyConstructorNotAvailable() {
        // Verificar que no hay constructor por defecto
        try {
            DataLoader.class.getDeclaredConstructor();
            fail("No debería tener constructor por defecto");
        } catch (NoSuchMethodException e) {
            // Esto es esperado
            assertTrue(true);
        }
    }

    @Test
    void testRun_ExceptionHandling() throws Exception {
        // Arrange
        when(userRepository.count()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert - Debería propagar la excepción
        assertThrows(RuntimeException.class, () -> dataLoader.run());
    }

}