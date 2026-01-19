package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisServiceTest extends EasyMockSupport {

    private DiagnosisRepository diagnosisRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private DiagnosisService diagnosisService;
    
    private Diagnosis diagnosis;
    private Doctor doctor;
    private Patient patient;
    private final Integer DIAGNOSIS_ID = 1;
    private final Integer DOCTOR_ID = 1;
    private final Integer PATIENT_ID = 1;

    @BeforeEach
    void setUp() {
        diagnosisRepository = EasyMock.mock(DiagnosisRepository.class);
        doctorRepository = EasyMock.mock(DoctorRepository.class);
        patientRepository = EasyMock.mock(PatientRepository.class);
        diagnosisService = new DiagnosisService(diagnosisRepository, doctorRepository, patientRepository);
        
        doctor = new Doctor();
        doctor.setId(DOCTOR_ID);
        
        patient = new Patient();
        patient.setId(PATIENT_ID);
        
        diagnosis = new Diagnosis(
            "/images/img1.jpg",
            "/images/img2.jpg",
            "/images/img3.jpg",
            "/images/img4.jpg",
            LocalDate.now(),
            "Test diagnosis",
            false,
            doctor,
            patient
        );
        diagnosis.setId(DIAGNOSIS_ID);
    }

    @Test
    void testFindAllSortedByUrgency() {
        Diagnosis urgentDiagnosis = new Diagnosis();
        urgentDiagnosis.setId(2);
        urgentDiagnosis.setUrgent(true);
        
        EasyMock.expect(diagnosisRepository.findAllByOrderByUrgentDescDateDesc())
                .andReturn(List.of(urgentDiagnosis, diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        List<Diagnosis> result = diagnosisService.findAllSortedByUrgency();
        
        assertEquals(2, result.size());
        assertTrue(result.get(0).isUrgent());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByDateSortedByUrgency() {
        LocalDate date = LocalDate.now();
        
        EasyMock.expect(diagnosisRepository.findByDateOrderByUrgentDesc(date))
                .andReturn(List.of(diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        List<Diagnosis> result = diagnosisService.findByDateSortedByUrgency(date);
        
        assertEquals(1, result.size());
        assertEquals(date, result.get(0).getDate());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindAll() {
        EasyMock.expect(diagnosisRepository.findAll()).andReturn(List.of(diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        List<Diagnosis> result = diagnosisService.findAll();
        
        assertEquals(1, result.size());
        assertEquals(diagnosis, result.get(0));
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByIdFound() {
        EasyMock.expect(diagnosisRepository.findById(DIAGNOSIS_ID)).andReturn(Optional.of(diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        Diagnosis result = diagnosisService.findById(DIAGNOSIS_ID);
        
        assertNotNull(result);
        assertEquals(DIAGNOSIS_ID, result.getId());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByIdNotFound() {
        EasyMock.expect(diagnosisRepository.findById(99)).andReturn(Optional.empty());
        EasyMock.replay(diagnosisRepository);
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> diagnosisService.findById(99)
        );
        
        assertTrue(ex.getMessage().contains("Diagnosis not found"));
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testCreateDiagnosis() {
        Diagnosis newDiagnosis = new Diagnosis(
            "/images/new1.jpg",
            "/images/new2.jpg",
            "/images/new3.jpg",
            "/images/new4.jpg",
            LocalDate.now(),
            "New diagnosis",
            true,
            null,
            null
        );
        
        EasyMock.expect(doctorRepository.findById(DOCTOR_ID)).andReturn(Optional.of(doctor));
        EasyMock.expect(patientRepository.findById(PATIENT_ID)).andReturn(Optional.of(patient));
        EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class)))
                .andAnswer(() -> {
                    Diagnosis d = (Diagnosis) EasyMock.getCurrentArguments()[0];
                    d.setId(2);
                    return d;
                });
        
        EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);
        
        Diagnosis result = diagnosisService.create(newDiagnosis, DOCTOR_ID, PATIENT_ID);
        
        assertNotNull(result.getId());
        assertEquals(2, result.getId());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
        EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
    }

    @Test
    void testCreateWithDoctorNotFound() {
        Diagnosis newDiagnosis = new Diagnosis();
        
        EasyMock.expect(doctorRepository.findById(99)).andReturn(Optional.empty());
        EasyMock.replay(doctorRepository);
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> diagnosisService.create(newDiagnosis, 99, PATIENT_ID)
        );
        
        assertTrue(ex.getMessage().contains("Doctor not found"));
        EasyMock.verify(doctorRepository);
    }

    @Test
    void testFindByDoctor() {
        EasyMock.expect(diagnosisRepository.findByDoctor_Id(DOCTOR_ID)).andReturn(List.of(diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        List<Diagnosis> result = diagnosisService.findByDoctor(DOCTOR_ID);
        
        assertEquals(1, result.size());
        assertEquals(DOCTOR_ID, result.get(0).getDoctor().getId());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByPatient() {
        EasyMock.expect(diagnosisRepository.findByPatient_Id(PATIENT_ID)).andReturn(List.of(diagnosis));
        EasyMock.replay(diagnosisRepository);
        
        List<Diagnosis> result = diagnosisService.findByPatient(PATIENT_ID);
        
        assertEquals(1, result.size());
        assertEquals(PATIENT_ID, result.get(0).getPatient().getId());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testCreateForDoctorAndPatient() {
        Diagnosis newDiagnosis = new Diagnosis(
            "/images/test1.jpg",
            "/images/test2.jpg",
            "/images/test3.jpg",
            "/images/test4.jpg",
            LocalDate.now(),
            "Test diagnosis",
            false,
            null,
            null
        );
        
        EasyMock.expect(doctorRepository.findById(DOCTOR_ID)).andReturn(Optional.of(doctor));
        EasyMock.expect(patientRepository.findById(PATIENT_ID)).andReturn(Optional.of(patient));
        EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class)))
                .andAnswer(() -> {
                    Diagnosis d = (Diagnosis) EasyMock.getCurrentArguments()[0];
                    d.setId(3);
                    return d;
                });
        
        EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);
        
        Diagnosis result = diagnosisService.createForDoctorAndPatient(newDiagnosis, DOCTOR_ID, PATIENT_ID);
        
        assertNotNull(result.getId());
        assertEquals(3, result.getId());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
        EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
    }

    @Test
    void testSaveDiagnosis() {
        EasyMock.expect(diagnosisRepository.saveAndFlush(diagnosis)).andReturn(diagnosis);
        EasyMock.replay(diagnosisRepository);
        
        Diagnosis result = diagnosisService.save(diagnosis);
        
        assertNotNull(result);
        assertEquals(diagnosis, result);
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testDeleteById() {
        diagnosisRepository.deleteById(DIAGNOSIS_ID);
        EasyMock.expectLastCall().once();
        EasyMock.replay(diagnosisRepository);
        
        diagnosisService.delete(DIAGNOSIS_ID);
        
        EasyMock.verify(diagnosisRepository);
    }
}