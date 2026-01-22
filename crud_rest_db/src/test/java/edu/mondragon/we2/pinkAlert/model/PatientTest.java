package edu.mondragon.we2.pinkAlert.model;
// package edu.mondragon.we2.pinkAlert.model;



// import org.junit.jupiter.api.Test;


// import java.time.LocalDate;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// class PatientTest {

//     @Test
//     void testGettersAndSetters() {
//         Patient patient = new Patient();

//         patient.setId(1);
//         patient.setName("Alice");
//         patient.setBirthDate(LocalDate.of(2000, 1, 1));

//         assertEquals(1, patient.getId());
//         assertEquals("Alice", patient.getName());
//         assertEquals(LocalDate.of(2000, 1, 1), patient.getBirthDate());
//     }

//     @Test
//     void testGetAge() {
//         Patient patient = new Patient("Bob", LocalDate.now().minusYears(30));

//         assertEquals(30, patient.getAge());
//     }

//     @Test
//     void testGetAgeWhenBirthDateIsNull() {
//         Patient patient = new Patient();

//         assertEquals(0, patient.getAge());
//     }

//     @Test
//     void testDiagnosesList() {
//         Patient patient = new Patient();

//         Diagnosis diagnosis = new Diagnosis();
//         patient.setDiagnoses(List.of(diagnosis));

//         assertEquals(1, patient.getDiagnoses().size());
//     }
// }
