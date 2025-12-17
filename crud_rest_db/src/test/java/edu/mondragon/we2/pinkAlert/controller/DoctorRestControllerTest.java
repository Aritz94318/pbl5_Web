package edu.mondragon.we2.pinkAlert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.*;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class DoctorRestControllerTest extends EasyMockSupport {

    private DoctorRepository doctorRepository;
    private DiagnosisRepository diagnosisRepository;
    private PatientRepository patientRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        diagnosisRepository = mock(DiagnosisRepository.class);
        patientRepository = mock(PatientRepository.class);

        DoctorService doctorService = new DoctorService(doctorRepository);
        DiagnosisService diagnosisService =
        new DiagnosisService(diagnosisRepository, doctorRepository, patientRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(new DoctorRestController(doctorService, diagnosisService)).build();

        objectMapper = new ObjectMapper();

        doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr Test");
    }

    @Test
    void testGetAllDoctors() throws Exception {
        EasyMock.expect(doctorRepository.findAll()).andReturn(List.of(doctor));

        EasyMock.replay(doctorRepository);

        mockMvc.perform(get("/doctors").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Dr Test"));

        EasyMock.verify(doctorRepository);
    }

    @Test
    void testGetDoctorById() throws Exception {
        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));

        EasyMock.replay(doctorRepository);

        mockMvc.perform(get("/doctors/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Dr Test"));

        EasyMock.verify(doctorRepository);
    }

    @Test
    void testCreateDoctor() throws Exception {
        EasyMock.expect(doctorRepository.save(EasyMock.anyObject())).andReturn(doctor);

        EasyMock.replay(doctorRepository);

        mockMvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(doctor))).andExpect(status().isOk());

        EasyMock.verify(doctorRepository);
    }

    @Test
    void testDeleteDoctor() throws Exception {
        doctorRepository.deleteById(1);
        EasyMock.expectLastCall();

        EasyMock.replay(doctorRepository);

        mockMvc.perform(delete("/doctors/1")).andExpect(status().isNoContent());

        EasyMock.verify(doctorRepository);
    }

    @Test
    void testGetDiagnosesByDoctor() throws Exception {
        Diagnosis diagnosis = new Diagnosis();

        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
        EasyMock.expect(diagnosisRepository.findByDoctor_Id(1)).andReturn(List.of(diagnosis));

        EasyMock.replay(doctorRepository, diagnosisRepository);

        mockMvc.perform(get("/doctors/1/diagnoses")).andExpect(status().isOk());

        EasyMock.verify(doctorRepository, diagnosisRepository);
    }

    @Test
    void testGetPatientsOfDoctor() throws Exception {
        Patient patient = new Patient();
        patient.setId(2);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setPatient(patient);

        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
        EasyMock.expect(diagnosisRepository.findByDoctor_Id(1)).andReturn(List.of(diagnosis));

        EasyMock.replay(doctorRepository, diagnosisRepository);

        mockMvc.perform(get("/doctors/1/patients") .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(2));

        EasyMock.verify(doctorRepository, diagnosisRepository);
    }
}
