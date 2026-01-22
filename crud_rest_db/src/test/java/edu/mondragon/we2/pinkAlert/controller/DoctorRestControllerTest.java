package edu.mondragon.we2.pinkAlert.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.DoctorService;

@ExtendWith(MockitoExtension.class)
class DoctorRestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DoctorService doctorService;

    @Mock
    private DiagnosisService diagnosisService;

    @InjectMocks
    private DoctorRestController doctorRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllDoctors() throws Exception {

        Doctor doctor1 = new Doctor();
        doctor1.setId(1);
        doctor1.setPhone("111111111");
        
        Doctor doctor2 = new Doctor();
        doctor2.setId(2);
        doctor2.setPhone("222222222");
        
        List<Doctor> doctors = Arrays.asList(doctor1, doctor2);
        
        when(doctorService.findAll()).thenReturn(doctors);

        mockMvc.perform(get("/doctors")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(doctorService).findAll();
    }

    @Test
    void testGetDoctorById_Found() throws Exception {

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setPhone("111111111");
        
        when(doctorService.findById(1)).thenReturn(doctor);

        mockMvc.perform(get("/doctors/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(doctorService).findById(1);
    }

    @Test
    void testGetDoctorById_NotFound() throws Exception {
    
        when(doctorService.findById(999)).thenReturn(null);

        mockMvc.perform(get("/doctors/999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        
        verify(doctorService).findById(999);
    }


    @Test
    void testDeleteDoctor() throws Exception {
    
        doNothing().when(doctorService).delete(1);

        mockMvc.perform(delete("/doctors/1"))
                .andExpect(status().isNoContent());
        
        verify(doctorService).delete(1);
    }

    @Test
    void testGetDiagnosesByDoctor() throws Exception {
 
        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setId(1);
        
        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setId(2);
        
        List<Diagnosis> diagnoses = Arrays.asList(diagnosis1, diagnosis2);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(diagnoses);

        mockMvc.perform(get("/doctors/1/diagnoses")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testGetDiagnosesByDoctor_NoDiagnoses() throws Exception {
   
        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/doctors/1/diagnoses")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testGetPatientsOfDoctor() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        Patient patient1 = new Patient();
        patient1.setId(1);
        patient1.setPhone("111111111");
        
        Patient patient2 = new Patient();
        patient2.setId(2);
        patient2.setPhone("222222222");
        
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setPatient(patient1);
        
        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setPatient(patient2);
        
        Diagnosis diagnosis3 = new Diagnosis();
        diagnosis3.setPatient(patient1); 
        
        List<Diagnosis> diagnoses = Arrays.asList(diagnosis1, diagnosis2, diagnosis3);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(diagnoses);

        mockMvc.perform(get("/doctors/1/patients")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testGetPatientsOfDoctor_NoPatients() throws Exception {
      
        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/doctors/1/patients")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testGetPatientsOfDoctor_WithNullPatient() throws Exception {

        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        Patient patient = new Patient();
        patient.setId(1);
        
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setPatient(patient);
        
        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setPatient(null); 
        List<Diagnosis> diagnoses = Arrays.asList(diagnosis1, diagnosis2);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(diagnoses);

        mockMvc.perform(get("/doctors/1/patients")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testUpdateDoctor_NotFound() throws Exception {
        Doctor doctorUpdates = new Doctor();
        doctorUpdates.setPhone("999999999");
        
        when(doctorService.update(eq(999), any(Doctor.class))).thenReturn(null);
        mockMvc.perform(put("/doctors/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorUpdates)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        
        verify(doctorService).update(eq(999), any(Doctor.class));
    }


    @Test
    void testGetAllDoctors_EmptyList() throws Exception {
   
        when(doctorService.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/doctors")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(doctorService).findAll();
    }

  
    @Test
    void testGetPatientsOfDoctor_DuplicatePatients() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        
        Patient patient = new Patient();
        patient.setId(1);
        patient.setPhone("111111111");
        
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setId(1);
        diagnosis1.setPatient(patient);
        
        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setId(2);
        diagnosis2.setPatient(patient); 
        
        Diagnosis diagnosis3 = new Diagnosis();
        diagnosis3.setId(3);
        diagnosis3.setPatient(patient);
        
        List<Diagnosis> diagnoses = Arrays.asList(diagnosis1, diagnosis2, diagnosis3);
        
        when(doctorService.findById(1)).thenReturn(doctor);
        when(diagnosisService.findByDoctor(1)).thenReturn(diagnoses);
        mockMvc.perform(get("/doctors/1/patients")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) 
                .andExpect(jsonPath("$[0].id").value(1));
        
        verify(doctorService).findById(1);
        verify(diagnosisService).findByDoctor(1);
    }

    @Test
    void testRestController_ConstructorInjection() {
        DoctorRestController controller = new DoctorRestController(doctorService, diagnosisService);
        assertThat(controller).isNotNull();
    }
}