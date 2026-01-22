package edu.mondragon.we2.pinkAlert.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDate;

class DiagnosisTest {

    private Diagnosis diagnosis;
    private Doctor doctor;
    private Patient patient;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        // Crear objetos necesarios para las pruebas
        doctor = new Doctor("12345678A");
        doctor.setId(1);
        
        patient = new Patient(LocalDate.of(1990, 5, 15), "87654321B");
        patient.setId(1);
        
        testDate = LocalDate.of(2024, 1, 15);
        
        // Crear un diagnóstico de prueba
        diagnosis = new Diagnosis();
    }

    // ===== CONSTRUCTORES =====
    
    @Test
    void testDefaultConstructor() {
        Diagnosis d = new Diagnosis();
        assertNotNull(d);
        assertNull(d.getId());
        assertNull(d.getImagePath());
        assertNull(d.getDate());
        assertNull(d.getDescription());
        assertFalse(d.isUrgent());
        assertFalse(d.isReviewed());
        assertEquals(BigDecimal.ZERO, d.getProbability());
        assertNull(d.getDoctor());
        assertNull(d.getPatient());
        assertNull(d.getFinalResult());
        assertNull(d.getAiPrediction());
    }

    @Test
    void testParameterizedConstructor() {
        Diagnosis d = new Diagnosis("image.jpg", testDate, "Test description", true, doctor, patient);
        
        assertEquals("image.jpg", d.getImagePath());
        assertEquals(testDate, d.getDate());
        assertEquals("Test description", d.getDescription());
        assertTrue(d.isUrgent());
        assertSame(doctor, d.getDoctor());
        assertSame(patient, d.getPatient());
        assertEquals(BigDecimal.ZERO, d.getProbability());
        assertFalse(d.isReviewed());
    }

    // ===== GETTERS Y SETTERS BÁSICOS =====
    
    @Test
    void testIdGetterAndSetter() {
        diagnosis.setId(100);
        assertEquals(100, diagnosis.getId());
        
        diagnosis.setId(null);
        assertNull(diagnosis.getId());
    }

    @Test
    void testImagePathGetterAndSetter() {
        diagnosis.setImagePath("path/to/image.jpg");
        assertEquals("path/to/image.jpg", diagnosis.getImagePath());
        
        diagnosis.setImagePath(null);
        assertNull(diagnosis.getImagePath());
    }

    @Test
    void testImage2PathGetterAndSetter() {
        diagnosis.setImage2Path("path/to/image2.jpg");
        assertEquals("path/to/image2.jpg", diagnosis.getImage2Path());
        
        diagnosis.setImage2Path("");
        assertEquals("", diagnosis.getImage2Path());
    }

    @Test
    void testImage3PathGetterAndSetter() {
        diagnosis.setImage3Path("path/to/image3.jpg");
        assertEquals("path/to/image3.jpg", diagnosis.getImage3Path());
        
        diagnosis.setImage3Path("   ");
        assertEquals("   ", diagnosis.getImage3Path());
    }

    @Test
    void testImage4PathGetterAndSetter() {
        diagnosis.setImage4Path("path/to/image4.jpg");
        assertEquals("path/to/image4.jpg", diagnosis.getImage4Path());
        
        diagnosis.setImage4Path(null);
        assertNull(diagnosis.getImage4Path());
    }

    @Test
    void testDateGetterAndSetter() {
        LocalDate date = LocalDate.of(2024, 12, 31);
        diagnosis.setDate(date);
        assertEquals(date, diagnosis.getDate());
        
        diagnosis.setDate(null);
        assertNull(diagnosis.getDate());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        diagnosis.setDescription("This is a test description");
        assertEquals("This is a test description", diagnosis.getDescription());
        
        diagnosis.setDescription("");
        assertEquals("", diagnosis.getDescription());
    }

    @Test
    void testUrgentGetterAndSetter() {
        diagnosis.setUrgent(true);
        assertTrue(diagnosis.isUrgent());
        
        diagnosis.setUrgent(false);
        assertFalse(diagnosis.isUrgent());
    }

    @Test
    void testReviewedGetterAndSetter() {
        diagnosis.setReviewed(true);
        assertTrue(diagnosis.isReviewed());
        
        diagnosis.setReviewed(false);
        assertFalse(diagnosis.isReviewed());
    }

    @Test
    void testProbabilityGetterAndSetter() {
        // Test con valor normal
        BigDecimal probability = new BigDecimal("0.87654321");
        diagnosis.setProbability(probability);
        assertEquals(probability, diagnosis.getProbability());
        
        // Test con null (debería asignar BigDecimal.ZERO)
        diagnosis.setProbability(null);
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
        
        // Test con BigDecimal.ZERO
        diagnosis.setProbability(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
        
        // Test con valor negativo
        diagnosis.setProbability(new BigDecimal("-0.5"));
        assertEquals(new BigDecimal("-0.5"), diagnosis.getProbability());
    }

    // ===== GETTERS Y SETTERS DE RELACIONES =====
    
    @Test
    void testDoctorGetterAndSetter() {
        diagnosis.setDoctor(doctor);
        assertSame(doctor, diagnosis.getDoctor());
        
        diagnosis.setDoctor(null);
        assertNull(diagnosis.getDoctor());
    }

    @Test
    void testPatientGetterAndSetter() {
        diagnosis.setPatient(patient);
        assertSame(patient, diagnosis.getPatient());
        
        diagnosis.setPatient(null);
        assertNull(diagnosis.getPatient());
    }

    // ===== ENUMS =====
    
    @Test
    void testFinalResultGetterAndSetter() {
        diagnosis.setFinalResult(FinalResult.MALIGNANT);
        assertEquals(FinalResult.MALIGNANT, diagnosis.getFinalResult());
        
        diagnosis.setFinalResult(FinalResult.BENIGN);
        assertEquals(FinalResult.BENIGN, diagnosis.getFinalResult());
        
        diagnosis.setFinalResult(FinalResult.INCONCLUSIVE);
        assertEquals(FinalResult.INCONCLUSIVE, diagnosis.getFinalResult());
        
        diagnosis.setFinalResult(FinalResult.PENDING);
        assertEquals(FinalResult.PENDING, diagnosis.getFinalResult());
        
        diagnosis.setFinalResult(null);
        assertNull(diagnosis.getFinalResult());
    }

    @Test
    void testAiPredictionGetterAndSetter() {
        diagnosis.setAiPrediction(AiPrediction.MALIGNANT);
        assertEquals(AiPrediction.MALIGNANT, diagnosis.getAiPrediction());
        
        diagnosis.setAiPrediction(AiPrediction.BENIGN);
        assertEquals(AiPrediction.BENIGN, diagnosis.getAiPrediction());
        
        diagnosis.setAiPrediction(null);
        assertNull(diagnosis.getAiPrediction());
    }

    // ===== CAMPOS PREVIEW =====
    
    @Test
    void testPreviewPathGetterAndSetter() {
        diagnosis.setPreviewPath("preview1.jpg");
        assertEquals("preview1.jpg", diagnosis.getPreviewPath());
        
        diagnosis.setPreviewPath(null);
        assertNull(diagnosis.getPreviewPath());
    }

    @Test
    void testPreview2PathGetterAndSetter() {
        diagnosis.setPreview2Path("preview2.jpg");
        assertEquals("preview2.jpg", diagnosis.getPreview2Path());
        
        diagnosis.setPreview2Path("");
        assertEquals("", diagnosis.getPreview2Path());
    }

    @Test
    void testPreview3PathGetterAndSetter() {
        diagnosis.setPreview3Path("preview3.jpg");
        assertEquals("preview3.jpg", diagnosis.getPreview3Path());
        
        diagnosis.setPreview3Path("   ");
        assertEquals("   ", diagnosis.getPreview3Path());
    }

    @Test
    void testPreview4PathGetterAndSetter() {
        diagnosis.setPreview4Path("preview4.jpg");
        assertEquals("preview4.jpg", diagnosis.getPreview4Path());
        
        diagnosis.setPreview4Path(null);
        assertNull(diagnosis.getPreview4Path());
    }

    // ===== MÉTODO getStatus() =====
    
    @Test
    void testGetStatus_NotReviewed() {
        diagnosis.setReviewed(false);
        assertEquals("Pending Review", diagnosis.getStatus());
    }

    @Test
    void testGetStatus_ReviewedNoFinalResult() {
        diagnosis.setReviewed(true);
        diagnosis.setFinalResult(null);
        assertEquals("Pending Result", diagnosis.getStatus());
    }

    @Test
    void testGetStatus_Malignant() {
        diagnosis.setReviewed(true);
        diagnosis.setFinalResult(FinalResult.MALIGNANT);
        assertEquals("Malignant", diagnosis.getStatus());
    }

    @Test
    void testGetStatus_Benign() {
        diagnosis.setReviewed(true);
        diagnosis.setFinalResult(FinalResult.BENIGN);
        assertEquals("Benign", diagnosis.getStatus());
    }

    @Test
    void testGetStatus_Inconclusive() {
        diagnosis.setReviewed(true);
        diagnosis.setFinalResult(FinalResult.INCONCLUSIVE);
        assertEquals("Inconclusive", diagnosis.getStatus());
    }

    @Test
    void testGetStatus_Pending() {
        diagnosis.setReviewed(true);
        diagnosis.setFinalResult(FinalResult.PENDING);
        assertEquals("Pending", diagnosis.getStatus());
    }

    // ===== PRUEBAS DE COMBINACIÓN =====
    
    @Test
    void testAllFieldsTogether() {
        // Configurar todos los campos
        diagnosis.setId(999);
        diagnosis.setImagePath("img1.jpg");
        diagnosis.setImage2Path("img2.jpg");
        diagnosis.setImage3Path("img3.jpg");
        diagnosis.setImage4Path("img4.jpg");
        diagnosis.setPreviewPath("prev1.jpg");
        diagnosis.setPreview2Path("prev2.jpg");
        diagnosis.setPreview3Path("prev3.jpg");
        diagnosis.setPreview4Path("prev4.jpg");
        diagnosis.setDate(LocalDate.of(2024, 6, 15));
        diagnosis.setDescription("Comprehensive test description");
        diagnosis.setUrgent(true);
        diagnosis.setReviewed(true);
        diagnosis.setProbability(new BigDecimal("0.99999999"));
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);
        diagnosis.setFinalResult(FinalResult.INCONCLUSIVE);
        diagnosis.setAiPrediction(AiPrediction.MALIGNANT);
        
        // Verificar todos los campos
        assertEquals(999, diagnosis.getId());
        assertEquals("img1.jpg", diagnosis.getImagePath());
        assertEquals("img2.jpg", diagnosis.getImage2Path());
        assertEquals("img3.jpg", diagnosis.getImage3Path());
        assertEquals("img4.jpg", diagnosis.getImage4Path());
        assertEquals("prev1.jpg", diagnosis.getPreviewPath());
        assertEquals("prev2.jpg", diagnosis.getPreview2Path());
        assertEquals("prev3.jpg", diagnosis.getPreview3Path());
        assertEquals("prev4.jpg", diagnosis.getPreview4Path());
        assertEquals(LocalDate.of(2024, 6, 15), diagnosis.getDate());
        assertEquals("Comprehensive test description", diagnosis.getDescription());
        assertTrue(diagnosis.isUrgent());
        assertTrue(diagnosis.isReviewed());
        assertEquals(new BigDecimal("0.99999999"), diagnosis.getProbability());
        assertSame(doctor, diagnosis.getDoctor());
        assertSame(patient, diagnosis.getPatient());
        assertEquals(FinalResult.INCONCLUSIVE, diagnosis.getFinalResult());
        assertEquals(AiPrediction.MALIGNANT, diagnosis.getAiPrediction());
        assertEquals("Inconclusive", diagnosis.getStatus());
    }

    @Test
    void testEdgeCases() {
        // Strings muy largos
        String longString = "A".repeat(600);
        diagnosis.setPreviewPath(longString);
        assertEquals(longString, diagnosis.getPreviewPath());
        
        // Probability en límites
        diagnosis.setProbability(new BigDecimal("1.00000000")); // Máximo
        assertEquals(new BigDecimal("1.00000000"), diagnosis.getProbability());
        
        diagnosis.setProbability(new BigDecimal("0.00000001")); // Mínimo positivo
        assertEquals(new BigDecimal("0.00000001"), diagnosis.getProbability());
        
        // Fecha límite
        diagnosis.setDate(LocalDate.MIN);
        assertEquals(LocalDate.MIN, diagnosis.getDate());
        
        diagnosis.setDate(LocalDate.MAX);
        assertEquals(LocalDate.MAX, diagnosis.getDate());
    }

    @Test
    void testTransientAnnotationOnGetStatus() throws Exception {
        // Verificar que @Transient está en getStatus()
        var method = Diagnosis.class.getMethod("getStatus");
        var annotations = method.getAnnotations();
        
        boolean hasTransient = false;
        for (var annotation : annotations) {
            if (annotation.annotationType().getName().contains("Transient")) {
                hasTransient = true;
                break;
            }
        }
        assertTrue(hasTransient, "getStatus() debe tener anotación @Transient");
    }

    @Test
    void testEntityAndTableAnnotations() {
        // Verificar anotaciones de clase
        var classAnnotations = Diagnosis.class.getAnnotations();
        
        boolean hasEntity = false;
        boolean hasTable = false;
        
        for (var annotation : classAnnotations) {
            if (annotation instanceof jakarta.persistence.Entity) {
                hasEntity = true;
            }
            if (annotation instanceof jakarta.persistence.Table) {
                hasTable = true;
                var tableAnnotation = (jakarta.persistence.Table) annotation;
                assertEquals("Diagnosis", tableAnnotation.name());
            }
        }
        
        assertTrue(hasEntity, "Debe tener anotación @Entity");
        assertTrue(hasTable, "Debe tener anotación @Table");
    }

    @Test
    void testColumnAnnotations() throws Exception {
        // Verificar algunas anotaciones @Column importantes
        var probabilityField = Diagnosis.class.getDeclaredField("probability");
        var columnAnnotation = probabilityField.getAnnotation(jakarta.persistence.Column.class);
        
        assertNotNull(columnAnnotation);
        assertEquals(10, columnAnnotation.precision());
        assertEquals(8, columnAnnotation.scale());
        assertFalse(columnAnnotation.nullable());
        
        // Verificar anotación @Id
        var idField = Diagnosis.class.getDeclaredField("id");
        var idAnnotation = idField.getAnnotation(jakarta.persistence.Id.class);
        assertNotNull(idAnnotation);
        
        // Verificar @GeneratedValue
        var generatedValueAnnotation = idField.getAnnotation(jakarta.persistence.GeneratedValue.class);
        assertNotNull(generatedValueAnnotation);
    }

    @Test
    void testManyToOneAnnotations() throws Exception {
        // Verificar anotaciones @ManyToOne
        var doctorField = Diagnosis.class.getDeclaredField("doctor");
        var manyToOneAnnotation = doctorField.getAnnotation(jakarta.persistence.ManyToOne.class);
        assertNotNull(manyToOneAnnotation);
        assertEquals(jakarta.persistence.FetchType.LAZY, manyToOneAnnotation.fetch());
        
        var patientField = Diagnosis.class.getDeclaredField("patient");
        var patientManyToOne = patientField.getAnnotation(jakarta.persistence.ManyToOne.class);
        assertNotNull(patientManyToOne);
        assertEquals(jakarta.persistence.FetchType.LAZY, patientManyToOne.fetch());
    }

    @Test
    void testJoinColumnAnnotations() throws Exception {
        // Verificar anotaciones @JoinColumn
        var doctorField = Diagnosis.class.getDeclaredField("doctor");
        var joinColumnAnnotation = doctorField.getAnnotation(jakarta.persistence.JoinColumn.class);
        assertNotNull(joinColumnAnnotation);
        assertEquals("DoctorID", joinColumnAnnotation.name());
        assertTrue(joinColumnAnnotation.nullable()); // nullable = true
        
        var patientField = Diagnosis.class.getDeclaredField("patient");
        var patientJoinColumn = patientField.getAnnotation(jakarta.persistence.JoinColumn.class);
        assertNotNull(patientJoinColumn);
        assertEquals("PatientID", patientJoinColumn.name());
        assertFalse(patientJoinColumn.nullable()); // nullable = false
    }

    @Test
    void testEnumAnnotations() throws Exception {
        // Verificar anotaciones @Enumerated
        var finalResultField = Diagnosis.class.getDeclaredField("finalResult");
        var enumeratedAnnotation = finalResultField.getAnnotation(jakarta.persistence.Enumerated.class);
        assertNotNull(enumeratedAnnotation);
        assertEquals(jakarta.persistence.EnumType.STRING, enumeratedAnnotation.value());
    }

    // ===== PRUEBAS DE IGUALDAD Y HASHCODE (si existen) =====
    
    @Test
    void testEqualsAndHashCode() {
        Diagnosis d1 = new Diagnosis();
        d1.setId(1);
        
        Diagnosis d2 = new Diagnosis();
        d2.setId(1);
        
        Diagnosis d3 = new Diagnosis();
        d3.setId(2);
        
        // Si la clase no sobrescribe equals(), usa Object.equals()
        // Por defecto, dos objetos diferentes no son iguales aunque tengan el mismo ID
        assertNotEquals(d1, d2); // Diferentes instancias
        assertNotEquals(d1, d3); // Diferentes IDs
        
        // Hashcodes también deberían ser diferentes
        assertNotEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1.hashCode(), d3.hashCode());
    }

    @Test
    void testToString() {
        // El método toString() por defecto de Object
        String toString = diagnosis.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Diagnosis"));
        assertTrue(toString.contains("@"));
    }

    // ===== PRUEBAS DE MUTACIÓN =====
    
    @Test
    void testMutateAllFields() {
        // Estado inicial
        assertNull(diagnosis.getId());
        assertNull(diagnosis.getImagePath());
        assertNull(diagnosis.getDate());
        assertNull(diagnosis.getDescription());
        assertFalse(diagnosis.isUrgent());
        assertFalse(diagnosis.isReviewed());
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
        assertNull(diagnosis.getDoctor());
        assertNull(diagnosis.getPatient());
        assertNull(diagnosis.getFinalResult());
        assertNull(diagnosis.getAiPrediction());
        
        // Mutar todos los campos
        diagnosis.setId(100);
        diagnosis.setImagePath("new.jpg");
        diagnosis.setImage2Path("new2.jpg");
        diagnosis.setImage3Path("new3.jpg");
        diagnosis.setImage4Path("new4.jpg");
        diagnosis.setPreviewPath("prev.jpg");
        diagnosis.setPreview2Path("prev2.jpg");
        diagnosis.setPreview3Path("prev3.jpg");
        diagnosis.setPreview4Path("prev4.jpg");
        diagnosis.setDate(LocalDate.now());
        diagnosis.setDescription("Updated");
        diagnosis.setUrgent(true);
        diagnosis.setReviewed(true);
        diagnosis.setProbability(new BigDecimal("0.5"));
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);
        diagnosis.setFinalResult(FinalResult.BENIGN);
        diagnosis.setAiPrediction(AiPrediction.BENIGN);
        
        // Verificar mutación
        assertEquals(100, diagnosis.getId());
        assertEquals("new.jpg", diagnosis.getImagePath());
        assertEquals("new2.jpg", diagnosis.getImage2Path());
        assertEquals("new3.jpg", diagnosis.getImage3Path());
        assertEquals("new4.jpg", diagnosis.getImage4Path());
        assertEquals("prev.jpg", diagnosis.getPreviewPath());
        assertEquals("prev2.jpg", diagnosis.getPreview2Path());
        assertEquals("prev3.jpg", diagnosis.getPreview3Path());
        assertEquals("prev4.jpg", diagnosis.getPreview4Path());
        assertEquals(LocalDate.now(), diagnosis.getDate());
        assertEquals("Updated", diagnosis.getDescription());
        assertTrue(diagnosis.isUrgent());
        assertTrue(diagnosis.isReviewed());
        assertEquals(new BigDecimal("0.5"), diagnosis.getProbability());
        assertSame(doctor, diagnosis.getDoctor());
        assertSame(patient, diagnosis.getPatient());
        assertEquals(FinalResult.BENIGN, diagnosis.getFinalResult());
        assertEquals(AiPrediction.BENIGN, diagnosis.getAiPrediction());
        
        // Mutar de nuevo
        diagnosis.setUrgent(false);
        diagnosis.setReviewed(false);
        diagnosis.setFinalResult(null);
        diagnosis.setAiPrediction(null);
        
        assertFalse(diagnosis.isUrgent());
        assertFalse(diagnosis.isReviewed());
        assertNull(diagnosis.getFinalResult());
        assertNull(diagnosis.getAiPrediction());
    }

    // ===== PRUEBAS DE NULL SAFETY =====
    
    @Test
    void testNullSafetyInProbabilitySetter() {
        // Verificar que setProbability maneja null correctamente
        diagnosis.setProbability(new BigDecimal("0.75"));
        assertEquals(new BigDecimal("0.75"), diagnosis.getProbability());
        
        diagnosis.setProbability(null);
        assertEquals(BigDecimal.ZERO, diagnosis.getProbability());
        
        // Verificar que no lanza NullPointerException
        assertDoesNotThrow(() -> diagnosis.setProbability(null));
    }

    // ===== PRUEBAS DE VALORES POR DEFECTO =====
    
    @Test
    void testDefaultValues() {
        Diagnosis d = new Diagnosis();
        
        // Valores por defecto del constructor
        assertEquals(BigDecimal.ZERO, d.getProbability());
        assertFalse(d.isReviewed());
        
        // Verificar que los campos numéricos tienen valores por defecto
        // (los wrappers como Integer son null por defecto)
        assertNull(d.getId());
    }

    // ===== PRUEBAS DE BORDE DE ENUMS =====
    
    @Test
    void testAllEnumValues() {
        // Probar que todos los valores de FinalResult son accesibles
        for (FinalResult result : FinalResult.values()) {
            diagnosis.setFinalResult(result);
            assertEquals(result, diagnosis.getFinalResult());
        }
        
        // Probar que todos los valores de AiPrediction son accesibles
        for (AiPrediction prediction : AiPrediction.values()) {
            diagnosis.setAiPrediction(prediction);
            assertEquals(prediction, diagnosis.getAiPrediction());
        }
    }
}