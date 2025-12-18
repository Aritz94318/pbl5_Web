package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
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

    private Doctor doctor;
    private Patient patient;
    private Diagnosis diagnosis;

    @BeforeEach
    void setUp() {
        diagnosisRepository = mock(DiagnosisRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        patientRepository = mock(PatientRepository.class);

        diagnosisService = new DiagnosisService(diagnosisRepository,doctorRepository,patientRepository);

        doctor = new Doctor();
        doctor.setId(1);

        patient = new Patient();
        patient.setId(2);

        diagnosis = new Diagnosis();
        diagnosis.setId(10);
        diagnosis.setDate(LocalDate.now());
        diagnosis.setDescription("Test diagnosis");
    }

    @Test
    void testFindAll() {
        EasyMock.expect(diagnosisRepository.findAll()).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        List<Diagnosis> result = diagnosisService.findAll();

        assertEquals(1, result.size());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindById() {
        EasyMock.expect(diagnosisRepository.findById(10)).andReturn(Optional.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        Diagnosis result = diagnosisService.findById(10);

        assertNotNull(result);
        assertEquals("Test diagnosis", result.getDescription());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByIdNotFound() {
        EasyMock.expect(diagnosisRepository.findById(99)).andReturn(Optional.empty());

        EasyMock.replay(diagnosisRepository);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> diagnosisService.findById(99));

        assertTrue(ex.getMessage().contains("Diagnosis not found"));

        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testCreateDiagnosis() {
        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
        EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));

        EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andAnswer(() -> {
                    Diagnosis d = (Diagnosis) EasyMock.getCurrentArguments()[0];
                    d.setId(10);
                    return d;
                });

        EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

        Diagnosis result = diagnosisService.create(diagnosis, 1, 2);

        assertNotNull(result);
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());
        assertEquals("Test diagnosis", result.getDescription());

        EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
    }

    @Test
    void testCreateDiagnosisDoctorNotFound() {
        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.empty());

        EasyMock.replay(doctorRepository);

        assertThrows(RuntimeException.class, () -> diagnosisService.create(diagnosis, 1, 2));

        EasyMock.verify(doctorRepository);
    }

    @Test
    void testCreateDiagnosisPatientNotFound() {
        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));

        EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.empty());

        EasyMock.replay(doctorRepository, patientRepository);

        assertThrows(RuntimeException.class, () -> diagnosisService.create(diagnosis, 1, 2));

        EasyMock.verify(doctorRepository, patientRepository);
    }

    @Test
    void testDeleteDiagnosis() {
        diagnosisRepository.deleteById(10);
        EasyMock.expectLastCall().once();

        EasyMock.replay(diagnosisRepository);

        diagnosisService.delete(10);

        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByDoctor() {
        EasyMock.expect(diagnosisRepository.findByDoctor_Id(1)).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        List<Diagnosis> result = diagnosisService.findByDoctor(1);

        assertEquals(1, result.size());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByPatient() {
        EasyMock.expect(diagnosisRepository.findByPatient_Id(2)).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        List<Diagnosis> result = diagnosisService.findByPatient(2);

        assertEquals(1, result.size());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testUpdateDiagnosis() {
        Diagnosis updated = new Diagnosis();
        updated.setDescription("Updated description");
        updated.setDate(LocalDate.of(2025, 1, 1));
        updated.setImagePath("image.png");

        EasyMock.expect(diagnosisRepository.findById(10)).andReturn(Optional.of(diagnosis));

        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));

        EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));

        EasyMock.expect(diagnosisRepository.save(diagnosis)).andReturn(diagnosis);

        EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

        Diagnosis result = diagnosisService.update(10, updated, 1, 2);

        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), result.getDate());
        assertEquals("image.png", result.getImagePath());
        assertEquals(doctor, result.getDoctor());
        assertEquals(patient, result.getPatient());

        EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
    }

    @Test
    void testCreateForDoctorAndPatient() {
        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));

        EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));

        EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andReturn(diagnosis);

        EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

        Diagnosis result = diagnosisService.createForDoctorAndPatient(diagnosis, 1, 2);

        assertNotNull(result);
        EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
    }

    @Test
    void testFindAllSortedByUrgency() {
        EasyMock.expect(diagnosisRepository.findAllByOrderByUrgentDescDateDesc()).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        List<Diagnosis> result = diagnosisService.findAllSortedByUrgency();

        assertEquals(1, result.size());
        EasyMock.verify(diagnosisRepository);
    }

    @Test
    void testFindByDateSortedByUrgency() {
        LocalDate date = LocalDate.now();

        EasyMock.expect(diagnosisRepository.findByDateOrderByUrgentDesc(date)).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        List<Diagnosis> result = diagnosisService.findByDateSortedByUrgency(date);

        assertEquals(1, result.size());
        EasyMock.verify(diagnosisRepository);
    }

}
