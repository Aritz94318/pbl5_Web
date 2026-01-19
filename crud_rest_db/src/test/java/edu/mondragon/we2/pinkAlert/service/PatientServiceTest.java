package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PatientServiceTest extends EasyMockSupport {

    private PatientRepository patientRepository;
    private PatientService patientService;
    private Patient patient;

    @BeforeEach
    void setUp() {
        patientRepository = EasyMock.mock(PatientRepository.class);
        patientService = new PatientService(patientRepository);
        patient = new Patient(LocalDate.of(1990, 1, 1));
        patient.setId(1);
    }

    @Test
    void testFindAll() {
        EasyMock.expect(patientRepository.findAll()).andReturn(List.of(patient));
        EasyMock.replay(patientRepository);
        
        List<Patient> result = patientService.findAll();
        
        assertEquals(1, result.size());
        assertEquals(patient, result.get(0));
        EasyMock.verify(patientRepository);
    }

    @Test
    void testFindByIdFound() {
        EasyMock.expect(patientRepository.findById(1)).andReturn(Optional.of(patient));
        EasyMock.replay(patientRepository);
        
        Patient result = patientService.findById(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        EasyMock.verify(patientRepository);
    }

    @Test
    void testFindByIdNotFound() {
        EasyMock.expect(patientRepository.findById(99)).andReturn(Optional.empty());
        EasyMock.replay(patientRepository);
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> patientService.findById(99)
        );
        
        assertTrue(ex.getMessage().contains("Patient not found"));
        EasyMock.verify(patientRepository);
    }

    @Test
    void testCreatePatient() {
        Patient newPatient = new Patient(LocalDate.of(1995, 5, 15));
        
        EasyMock.expect(patientRepository.save(EasyMock.anyObject(Patient.class)))
                .andAnswer(() -> {
                    Patient p = (Patient) EasyMock.getCurrentArguments()[0];
                    p.setId(1);
                    return p;
                });
        EasyMock.replay(patientRepository);
        
        Patient result = patientService.create(newPatient);
        
        assertNotNull(result.getId());
        assertEquals(1, result.getId());
        EasyMock.verify(patientRepository);
    }

    @Test
    void testUpdatePatient() {
        Patient updatedPatient = new Patient(LocalDate.of(2000, 10, 20));
        
        EasyMock.expect(patientRepository.findById(1)).andReturn(Optional.of(patient));
        EasyMock.expect(patientRepository.save(patient)).andReturn(patient);
        EasyMock.replay(patientRepository);
        
        Patient result = patientService.update(1, updatedPatient);
        
        assertNotNull(result);
        assertEquals(updatedPatient.getBirthDate(), result.getBirthDate());
        EasyMock.verify(patientRepository);
    }

    @Test
    void testDeleteById() {
        patientRepository.deleteById(1);
        EasyMock.expectLastCall().once();
        EasyMock.replay(patientRepository);
        
        patientService.delete(1);
        
        EasyMock.verify(patientRepository);
    }

    @Test
    void testDeleteByEntity() {
        patientRepository.delete(patient);
        EasyMock.expectLastCall().once();
        EasyMock.replay(patientRepository);
        
        patientService.delete(patient);
        
        EasyMock.verify(patientRepository);
    }
}