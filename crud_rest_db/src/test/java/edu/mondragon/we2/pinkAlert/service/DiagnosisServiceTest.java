 package edu.mondragon.we2.pinkAlert.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @InjectMocks
    private DiagnosisService diagnosisService;
    
    private Diagnosis diagnosis;
    private Doctor doctor;
    private Patient patient;
    
    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1);
        
        patient = new Patient();
        patient.setId(2);
        
        diagnosis = new Diagnosis();
        diagnosis.setId(10);
        diagnosis.setDate(LocalDate.now());
        diagnosis.setDescription("Initial description");
        diagnosis.setUrgent(false);
        diagnosis.setReviewed(false);
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);
    }

    
    @Test
    void testFindAllSortedByUrgency() {
        // Arrange
        List<Diagnosis> expected = Arrays.asList(diagnosis);
        when(diagnosisRepository.findAllByOrderByUrgentDescDateDesc()).thenReturn(expected);
        
        // Act
        List<Diagnosis> result = diagnosisService.findAllSortedByUrgency();
        
        // Assert
        assertEquals(expected, result);
        verify(diagnosisRepository).findAllByOrderByUrgentDescDateDesc();
    }

    @Test
    void testFindByDateSortedByUrgency() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Diagnosis> expected = Arrays.asList(diagnosis);
        when(diagnosisRepository.findByDateOrderByUrgentDesc(date)).thenReturn(expected);
        
        // Act
        List<Diagnosis> result = diagnosisService.findByDateSortedByUrgency(date);
        
        // Assert
        assertEquals(expected, result);
        verify(diagnosisRepository).findByDateOrderByUrgentDesc(date);
    }

    @Test
    void testFindAll() {
        // Arrange
        List<Diagnosis> expected = Arrays.asList(diagnosis);
        when(diagnosisRepository.findAll()).thenReturn(expected);
        
        // Act
        List<Diagnosis> result = diagnosisService.findAll();
        
        // Assert
        assertEquals(expected, result);
        verify(diagnosisRepository).findAll();
    }

    @Test
    void testFindById() {
        // Arrange
        when(diagnosisRepository.findById(10)).thenReturn(Optional.of(diagnosis));
        
        // Act
        Diagnosis result = diagnosisService.findById(10);
        
        // Assert
        assertEquals(diagnosis, result);
        verify(diagnosisRepository).findById(10);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(diagnosisRepository.findById(99)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diagnosisService.findById(99);
        });
        
        assertTrue(exception.getMessage().contains("Diagnosis not found with id 99"));
        verify(diagnosisRepository).findById(99);
    }

    @Test
    void testCreate() {
        // Arrange
        Diagnosis newDiagnosis = new Diagnosis();
        newDiagnosis.setDate(LocalDate.now());
        newDiagnosis.setDescription("New diagnosis");
        
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });
        
        // Act
        Diagnosis result = diagnosisService.create(newDiagnosis, 1, 2);
        
        // Assert
        assertNotNull(result.getId());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
        assertEquals("New diagnosis", result.getDescription());
        
        verify(doctorRepository).findById(1);
        verify(patientRepository).findById(2);
        verify(diagnosisRepository).save(newDiagnosis);
    }

    @Test
    void testCreate_DoctorNotFound() {
        // Arrange
        when(doctorRepository.findById(99)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diagnosisService.create(diagnosis, 99, 2);
        });
        
        assertTrue(exception.getMessage().contains("Doctor not found with id 99"));
        verify(doctorRepository).findById(99);
        verify(patientRepository, never()).findById(anyInt());
    }

    @Test
    void testCreate_PatientNotFound() {
        // Arrange
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(99)).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diagnosisService.create(diagnosis, 1, 99);
        });
        
        assertTrue(exception.getMessage().contains("Patient not found with id 99"));
        verify(doctorRepository).findById(1);
        verify(patientRepository).findById(99);
    }



    @Test
    void testDelete() {
        diagnosisService.delete(10);
        
        verify(diagnosisRepository).deleteById(10);
    }

    @Test
    void testFindByDoctor() {

        List<Diagnosis> expected = Arrays.asList(diagnosis);
        when(diagnosisRepository.findByDoctor_Id(1)).thenReturn(expected);
        
        List<Diagnosis> result = diagnosisService.findByDoctor(1);
        
        assertEquals(expected, result);
        verify(diagnosisRepository).findByDoctor_Id(1);
    }

    @Test
    void testFindByPatient() {
      
        List<Diagnosis> expected = Arrays.asList(diagnosis);
        when(diagnosisRepository.findByPatient_Id(2)).thenReturn(expected); 
        List<Diagnosis> result = diagnosisService.findByPatient(2);
        assertEquals(expected, result);
        verify(diagnosisRepository).findByPatient_Id(2);
    }

    @Test
    void testCreateForDoctorAndPatient() {
       
        Diagnosis newDiagnosis = new Diagnosis();
        newDiagnosis.setDescription("Test diagnosis");
        
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });
        Diagnosis result = diagnosisService.createForDoctorAndPatient(newDiagnosis, 1, 2);
        assertNotNull(result.getId());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
        
        verify(doctorRepository).findById(1);
        verify(patientRepository).findById(2);
        verify(diagnosisRepository).save(newDiagnosis);
    }

    @Test
    void testCreateForDoctorAndPatient_DuplicateMethod() {
        Diagnosis newDiagnosis = new Diagnosis();
        
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenReturn(newDiagnosis);
        
        Diagnosis result = diagnosisService.createForDoctorAndPatient(newDiagnosis, 1, 2);
        
        assertSame(newDiagnosis, result);
        verify(doctorRepository).findById(1);
        verify(patientRepository).findById(2);
        verify(diagnosisRepository).save(newDiagnosis);
    }
    
    @Test
    void testConstructor() {
        DiagnosisService service = new DiagnosisService(diagnosisRepository, doctorRepository, patientRepository);
        assertNotNull(service);
    }
    
 
} 