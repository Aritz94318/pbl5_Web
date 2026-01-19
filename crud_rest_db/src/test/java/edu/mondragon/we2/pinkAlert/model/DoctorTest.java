package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(doctor);
        assertNull(doctor.getId());
        assertNull(doctor.getDiagnoses());
    }

    @Test
    void testSetterAndGetters() {
        Integer id = 1;
        List<Diagnosis> diagnoses = new ArrayList<>();
        
        // Add some test diagnoses
        Diagnosis diagnosis1 = new Diagnosis();
        Diagnosis diagnosis2 = new Diagnosis();
        diagnoses.add(diagnosis1);
        diagnoses.add(diagnosis2);

        doctor.setId(id);
        doctor.setDiagnoses(diagnoses);

        assertEquals(id, doctor.getId());
        assertEquals(diagnoses, doctor.getDiagnoses());
        assertEquals(2, doctor.getDiagnoses().size());
    }

    @Test
    void testSetDiagnosesWithNull() {
        doctor.setDiagnoses(null);
        assertNull(doctor.getDiagnoses());
    }

    @Test
    void testSetDiagnosesWithEmptyList() {
        doctor.setDiagnoses(new ArrayList<>());
        assertNotNull(doctor.getDiagnoses());
        assertTrue(doctor.getDiagnoses().isEmpty());
    }

    @Test
    void testSetterWithNullId() {
        doctor.setId(null);
        assertNull(doctor.getId());
    }
}