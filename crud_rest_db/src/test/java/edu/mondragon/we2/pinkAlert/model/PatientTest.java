package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(patient);
        assertNull(patient.getId());
        assertNull(patient.getBirthDate());
        assertNull(patient.getDiagnoses());
        assertNull(patient.getUser());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Patient paramPatient = new Patient(birthDate);
        
        assertEquals(birthDate, paramPatient.getBirthDate());
        assertNull(paramPatient.getId());
        assertNull(paramPatient.getDiagnoses());
        assertNull(paramPatient.getUser());
    }

    @Test
    void testSetterAndGetters() {
        Integer id = 1;
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        List<Diagnosis> diagnoses = new ArrayList<>();
        User user = new User();

        patient.setId(id);
        patient.setBirthDate(birthDate);
        patient.setDiagnoses(diagnoses);
        patient.setUser(user);

        assertEquals(id, patient.getId());
        assertEquals(birthDate, patient.getBirthDate());
        assertEquals(diagnoses, patient.getDiagnoses());
        assertEquals(user, patient.getUser());
    }

    @Test
    void testGetAgeWithNullBirthDate() {
        patient.setBirthDate(null);
        assertEquals(0, patient.getAge());
    }

    @Test
    void testGetAgeWithFutureBirthDate() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        patient.setBirthDate(futureDate);
        assertEquals(-1, patient.getAge()); // Negative age for future dates
    }

    @Test
    void testGetAgeWithPastBirthDate() {
        LocalDate pastDate = LocalDate.now().minusYears(25);
        patient.setBirthDate(pastDate);
        assertEquals(25, patient.getAge());
    }

    @Test
    void testGetAgeWithBirthdayToday() {
        LocalDate today = LocalDate.now();
        patient.setBirthDate(today);
        assertEquals(0, patient.getAge());
    }

    @Test
    void testSettersWithNullValues() {
        patient.setId(null);
        patient.setBirthDate(null);
        patient.setDiagnoses(null);
        patient.setUser(null);

        assertNull(patient.getId());
        assertNull(patient.getBirthDate());
        assertNull(patient.getDiagnoses());
        assertNull(patient.getUser());
    }
}