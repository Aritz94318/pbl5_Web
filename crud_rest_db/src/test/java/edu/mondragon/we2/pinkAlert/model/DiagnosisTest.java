package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DiagnosisTest {

    private Diagnosis diagnosis;

    @BeforeEach
    void setUp() {
        diagnosis = new Diagnosis();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(diagnosis);
        assertNull(diagnosis.getId());
        assertNull(diagnosis.getImagePath());
        assertNull(diagnosis.getImage2Path());
        assertNull(diagnosis.getImage3Path());
        assertNull(diagnosis.getImage4Path());
        assertNull(diagnosis.getDate());
        assertNull(diagnosis.getDescription());
        assertFalse(diagnosis.isUrgent());
        assertFalse(diagnosis.isReviewed());
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
        assertNull(diagnosis.getDoctor());
        assertNull(diagnosis.getPatient());
    }

    @Test
    void testParameterizedConstructor() {
        String imagePath = "/images/img1.jpg";
        String image2Path = "/images/img2.jpg";
        String image3Path = "/images/img3.jpg";
        String image4Path = "/images/img4.jpg";
        LocalDate date = LocalDate.now();
        String description = "Test Diagnosis";
        boolean urgent = true;
        Doctor doctor = new Doctor();
        Patient patient = new Patient();

        Diagnosis paramDiagnosis = new Diagnosis(
            imagePath, image2Path, image3Path, image4Path,
            date, description, urgent, doctor, patient
        );

        assertEquals(imagePath, paramDiagnosis.getImagePath());
        assertEquals(image2Path, paramDiagnosis.getImage2Path());
        assertEquals(image3Path, paramDiagnosis.getImage3Path());
        assertEquals(image4Path, paramDiagnosis.getImage4Path());
        assertEquals(date, paramDiagnosis.getDate());
        assertEquals(description, paramDiagnosis.getDescription());
        assertTrue(paramDiagnosis.isUrgent());
        assertEquals(doctor, paramDiagnosis.getDoctor());
        assertEquals(patient, paramDiagnosis.getPatient());
        assertEquals(BigDecimal.ZERO, paramDiagnosis.getProbability());
        assertFalse(paramDiagnosis.isReviewed());
    }

    @Test
    void testSetterAndGetters() {
        Integer id = 1;
        String imagePath = "/images/img1.jpg";
        String image2Path = "/images/img2.jpg";
        String image3Path = "/images/img3.jpg";
        String image4Path = "/images/img4.jpg";
        LocalDate date = LocalDate.now();
        String description = "Test Diagnosis";
        boolean urgent = true;
        boolean reviewed = true;
        BigDecimal probability = new BigDecimal("0.85");
        Doctor doctor = new Doctor();
        Patient patient = new Patient();

        diagnosis.setId(id);
        diagnosis.setImagePath(imagePath);
        diagnosis.setImage2Path(image2Path);
        diagnosis.setImage3Path(image3Path);
        diagnosis.setImage4Path(image4Path);
        diagnosis.setDate(date);
        diagnosis.setDescription(description);
        diagnosis.setUrgent(urgent);
        diagnosis.setReviewed(reviewed);
        diagnosis.setProbability(probability);
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);

        assertEquals(id, diagnosis.getId());
        assertEquals(imagePath, diagnosis.getImagePath());
        assertEquals(image2Path, diagnosis.getImage2Path());
        assertEquals(image3Path, diagnosis.getImage3Path());
        assertEquals(image4Path, diagnosis.getImage4Path());
        assertEquals(date, diagnosis.getDate());
        assertEquals(description, diagnosis.getDescription());
        assertTrue(diagnosis.isUrgent());
        assertTrue(diagnosis.isReviewed());
        assertEquals(probability, diagnosis.getProbability());
        assertEquals(doctor, diagnosis.getDoctor());
        assertEquals(patient, diagnosis.getPatient());
    }

    @Test
    void testSetProbabilityWithNull() {
        diagnosis.setProbability(null);
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
    }

    @Test
    void testGetStatusPendingReview() {
        diagnosis.setReviewed(false);
        assertEquals("Pending Review", diagnosis.getStatus());
    }

    @Test
    void testGetStatusMalignant() {
        diagnosis.setReviewed(true);
        diagnosis.setUrgent(true);
        assertEquals("Malignant", diagnosis.getStatus());
    }

    @Test
    void testGetStatusBenignant() {
        diagnosis.setReviewed(true);
        diagnosis.setUrgent(false);
        assertEquals("Benignant", diagnosis.getStatus());
    }

    @Test
    void testSettersWithNullValues() {
        diagnosis.setId(null);
        diagnosis.setImagePath(null);
        diagnosis.setImage2Path(null);
        diagnosis.setImage3Path(null);
        diagnosis.setImage4Path(null);
        diagnosis.setDate(null);
        diagnosis.setDescription(null);
        diagnosis.setDoctor(null);
        diagnosis.setPatient(null);

        assertAll(
            () -> assertNull(diagnosis.getId()),
            () -> assertNull(diagnosis.getImagePath()),
            () -> assertNull(diagnosis.getImage2Path()),
            () -> assertNull(diagnosis.getImage3Path()),
            () -> assertNull(diagnosis.getImage4Path()),
            () -> assertNull(diagnosis.getDate()),
            () -> assertNull(diagnosis.getDescription()),
            () -> assertNull(diagnosis.getDoctor()),
            () -> assertNull(diagnosis.getPatient())
        );
    }
}