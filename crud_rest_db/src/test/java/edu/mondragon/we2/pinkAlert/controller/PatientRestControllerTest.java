package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.PatientService;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PatientRestControllerTest extends EasyMockSupport {

        private PatientRepository patientRepository;
        private DiagnosisRepository diagnosisRepository;
        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        private Patient patient;

        @BeforeEach
        void setUp() {
                patientRepository = mock(PatientRepository.class);
                diagnosisRepository = mock(DiagnosisRepository.class);

                PatientService patientService = new PatientService(patientRepository);
                DiagnosisService diagnosisService = new DiagnosisService(diagnosisRepository, null, patientRepository);

                mockMvc = MockMvcBuilders.standaloneSetup(new PatientRestController(patientService, diagnosisService)).build();

                objectMapper = new ObjectMapper();

                patient = new Patient();
                patient.setId(1);
                patient.setName("Test Patient");
        }

        @Test
        void testGetAllPatients() throws Exception {
                EasyMock.expect(patientRepository.findAll()).andReturn(List.of(patient));

                EasyMock.replay(patientRepository);

                mockMvc.perform(get("/patients").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Test Patient"));

                EasyMock.verify(patientRepository);
        }

        @Test
        void testGetPatientById() throws Exception {
                EasyMock.expect(patientRepository.findById(1)).andReturn(Optional.of(patient));

                EasyMock.replay(patientRepository);

                mockMvc.perform(get("/patients/1")).andExpect(status().isOk());

                EasyMock.verify(patientRepository);
        }

        @Test
        void testCreatePatient() throws Exception {
                EasyMock.expect(patientRepository.save(EasyMock.anyObject())).andReturn(patient);

                EasyMock.replay(patientRepository);

                mockMvc.perform(post("/patients").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(patient))).andExpect(status().isOk());

                EasyMock.verify(patientRepository);
        }

        @Test
        void testDeletePatient() throws Exception {
                patientRepository.deleteById(1);
                EasyMock.expectLastCall();

                EasyMock.replay(patientRepository);

                mockMvc.perform(delete("/patients/1")).andExpect(status().isNoContent());

                EasyMock.verify(patientRepository);
        }

        @Test
        void testGetDiagnosesByPatient() throws Exception {
                Diagnosis diagnosis = new Diagnosis();

                EasyMock.expect(patientRepository.findById(1)).andReturn(Optional.of(patient));
                EasyMock.expect(diagnosisRepository.findByPatient_Id(1)).andReturn(List.of(diagnosis));

                EasyMock.replay(patientRepository, diagnosisRepository);

                mockMvc.perform(get("/patients/1/diagnoses")).andExpect(status().isOk());

                EasyMock.verify(patientRepository, diagnosisRepository);
        }
}
