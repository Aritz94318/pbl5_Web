package edu.mondragon.we2.pinkAlert.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {
    private MockMvc mockMvc;
    
    @Mock
    private DiagnosisService diagnosisService;
    
    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        doctorController = new DoctorController(diagnosisService);
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController).build();
    }

    @Test
    void testDashboard_Default() throws Exception {
  
        LocalDate today = LocalDate.now();
        List<Diagnosis> emptyList = Collections.emptyList();
        
        when(diagnosisService.findByDateSortedByUrgency(today)).thenReturn(emptyList);
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor-dashboard"));
    }

    @Test
    void testDashboard_WithDate() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        List<Diagnosis> diagnoses = new ArrayList<>();
        
        when(diagnosisService.findByDateSortedByUrgency(testDate)).thenReturn(diagnoses);

        mockMvc.perform(get("/doctor/dashboard")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk());
    }

    @Test
    void testDashboard_WithFilters() throws Exception {
 
        LocalDate today = LocalDate.now();
      
        Diagnosis d1 = createDiagnosis(1);
        d1.setReviewed(true);
        d1.setFinalResult(FinalResult.MALIGNANT);
        
        Diagnosis d2 = createDiagnosis(2);
        d2.setReviewed(false);
        d2.setFinalResult(null);
        
        List<Diagnosis> diagnoses = Arrays.asList(d1, d2);
        
        when(diagnosisService.findByDateSortedByUrgency(today)).thenReturn(diagnoses);

        mockMvc.perform(get("/doctor/dashboard")
                .param("status", "REVIEWED")
                .param("result", "MALIGNANT"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/doctor/dashboard")
                .param("status", "PENDING")
                .param("result", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void testDiagnosisDetails() throws Exception {
      
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(anyInt())).thenReturn(new ArrayList<>());
        when(diagnosisService.countByPatientId(anyInt())).thenReturn(1L);

        mockMvc.perform(get("/doctor/diagnosis/{id}", diagnosisId))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor-diagnosis"));
    }

    @Test
    void testSaveReview() throws Exception {
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        diagnosis.setReviewed(false);
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        mockMvc.perform(post("/doctor/diagnosis/{id}/review", diagnosisId)
                .param("finalResult", "MALIGNANT")
                .param("description", "Test review")
                .param("patientNotified", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/diagnosis/" + diagnosisId));
    }

    @Test
    void testSaveReview_WithNullParams() throws Exception {
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);

        mockMvc.perform(post("/doctor/diagnosis/{id}/review", diagnosisId))
                .andExpect(status().is3xxRedirection());
    }

   
    @Test
    void testDatePill() {
        DoctorController.DatePill pill = new DoctorController.DatePill("Today", "01/01/2024", "2024-01-01", true);
        
        assertThat(pill.getLabel()).isEqualTo("Today");
        assertThat(pill.getDisplay()).isEqualTo("01/01/2024");
        assertThat(pill.getParam()).isEqualTo("2024-01-01");
        assertThat(pill.isActive()).isTrue();
    }

    @Test
    void testDashboard_DifferentDatePills() throws Exception {
    
        LocalDate testDate = LocalDate.of(2024, 1, 20);
        when(diagnosisService.findByDateSortedByUrgency(testDate)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/doctor/dashboard")
                .param("date", "2024-01-20"))
                .andExpect(status().isOk());
    }

    @Test
    void testDashboard_WithDiagnoses() throws Exception {
    
        LocalDate today = LocalDate.now();
        List<Diagnosis> diagnoses = new ArrayList<>();
        Diagnosis d1 = createDiagnosis(1);
        d1.setUrgent(true);
        d1.setReviewed(true);
        d1.setFinalResult(FinalResult.MALIGNANT);
        diagnoses.add(d1);
        Diagnosis d2 = createDiagnosis(2);
        d2.setUrgent(false);
        d2.setReviewed(false);
        d2.setFinalResult(null);
        diagnoses.add(d2);
        Diagnosis d3 = createDiagnosis(3);
        d3.setUrgent(true);
        d3.setReviewed(true);
        d3.setFinalResult(FinalResult.BENIGN);
        diagnoses.add(d3);
        Diagnosis d4 = createDiagnosis(4);
        d4.setUrgent(false);
        d4.setReviewed(true);
        d4.setFinalResult(FinalResult.INCONCLUSIVE);
        diagnoses.add(d4);
        
        when(diagnosisService.findByDateSortedByUrgency(today)).thenReturn(diagnoses);
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().isOk());
    }
    private Diagnosis createDiagnosis(Integer id) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setDate(LocalDate.now());
        Patient patient = new Patient();
        patient.setId(id);
        
        User user = new User();
        user.setId(id);
        user.setFullName("Test Patient " + id);
        user.setEmail("patient" + id + "@test.com");
        
        patient.setUser(user);
        diagnosis.setPatient(patient);
        
        return diagnosis;
    }
}