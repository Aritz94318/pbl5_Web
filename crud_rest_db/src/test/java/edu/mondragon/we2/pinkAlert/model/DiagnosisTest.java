package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisTest {

    static Stream<Arguments> statusProvider() {
        return Stream.of(
                Arguments.of("Cancer grade II detected", "Positive"),
                Arguments.of("False positive screening", "Negative"),
                Arguments.of("Unclear image", "Pending"),
                Arguments.of(null, "Pending"));
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void testGetStatus(String description, String expectedStatus) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDescription(description);

        assertEquals(expectedStatus, diagnosis.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        Diagnosis diagnosis = new Diagnosis();

        Doctor doctor = new Doctor("Dr Test");
        Patient patient = new Patient("Patient Test", LocalDate.of(2000, 1, 1));

        diagnosis.setId(1);
        diagnosis.setImagePath("image.jpg");
        diagnosis.setDate(LocalDate.now());
        diagnosis.setDescription("Test description");
        diagnosis.setUrgent(true);
        diagnosis.setReviewed(true);
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);

        assertEquals(1, diagnosis.getId());
        assertEquals("image.jpg", diagnosis.getImagePath());
        assertEquals("Test description", diagnosis.getDescription());
        assertTrue(diagnosis.isUrgent());
        assertTrue(diagnosis.isReviewed());
        assertEquals(doctor, diagnosis.getDoctor());
        assertEquals(patient, diagnosis.getPatient());
    }
}
