package edu.mondragon.we2.pinkAlert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.mondragon.we2.pinkAlert.controller.DiagnosisRestController;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DiagnosisRestControllerTest extends EasyMockSupport {

        private DiagnosisRepository diagnosisRepository;
        private DoctorRepository doctorRepository;
        private PatientRepository patientRepository;

        private DiagnosisService diagnosisService;
        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        private Diagnosis diagnosis;
        private Doctor doctor;
        private Patient patient;

        @BeforeEach
        void setUp() {
                diagnosisRepository = mock(DiagnosisRepository.class);
                doctorRepository = mock(DoctorRepository.class);
                patientRepository = mock(PatientRepository.class);

                diagnosisService = new DiagnosisService(diagnosisRepository, doctorRepository, patientRepository);

                DiagnosisRestController controller = new DiagnosisRestController(diagnosisService);

                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
                objectMapper = new ObjectMapper().findAndRegisterModules();

                doctor = new Doctor();
                doctor.setId(1);

                patient = new Patient();
                patient.setId(2);

                diagnosis = new Diagnosis();
                diagnosis.setId(10);
                diagnosis.setDescription("Test diagnosis");
                diagnosis.setDate(LocalDate.of(2025, 1, 1));
                diagnosis.setImagePath("image.png");
        }

        @Test
        void testGetAll() throws Exception {
                EasyMock.expect(diagnosisRepository.findAll()).andReturn(List.of(diagnosis));

                EasyMock.replay(diagnosisRepository);
                mockMvc.perform(get("/diagnoses").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$[0].description").value("Test diagnosis"));

                EasyMock.verify(diagnosisRepository);
        }

        @Test
        void testGetById() throws Exception {
                EasyMock.expect(diagnosisRepository.findById(10)).andReturn(Optional.of(diagnosis));

                EasyMock.replay(diagnosisRepository);

                mockMvc.perform(get("/diagnoses/10").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Test diagnosis"));

                EasyMock.verify(diagnosisRepository);
        }

        @Test
        void testCreateDiagnosis() throws Exception {
                DiagnosisRestController.DiagnosisRequest request = new DiagnosisRestController.DiagnosisRequest();
                request.setDescription("Test diagnosis");
                request.setDate(LocalDate.of(2025, 1, 1));
                request.setImagePath("image.png");
                request.setDoctorId(1);
                request.setPatientId(2);

                EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
                EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));
                EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andReturn(diagnosis);

                EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

                mockMvc.perform(post("/diagnoses").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Test diagnosis"));

                EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
        }

        @Test
        void testDeleteDiagnosis() throws Exception {
                diagnosisRepository.deleteById(10);
                EasyMock.expectLastCall().once();

                EasyMock.replay(diagnosisRepository);

                mockMvc.perform(delete("/diagnoses/10")).andExpect(status().isNoContent());

                EasyMock.verify(diagnosisRepository);
        }

        @Test
        void testUpdateDiagnosis() throws Exception {
                DiagnosisRestController.DiagnosisRequest request = new DiagnosisRestController.DiagnosisRequest();
                request.setDescription("Updated diagnosis");
                request.setDate(LocalDate.of(2025, 2, 1));
                request.setImagePath("updated.png");
                request.setDoctorId(1);
                request.setPatientId(2);

                Diagnosis updatedDiagnosis = new Diagnosis();
                updatedDiagnosis.setId(10);
                updatedDiagnosis.setDescription("Updated diagnosis");
                updatedDiagnosis.setDate(LocalDate.of(2025, 2, 1));
                updatedDiagnosis.setImagePath("updated.png");

                EasyMock.expect(diagnosisRepository.findById(10)).andReturn(Optional.of(diagnosis));
                EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
                EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));
                EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andReturn(updatedDiagnosis);

                EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

                mockMvc.perform(put("/diagnoses/10").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Updated diagnosis")).andExpect(jsonPath("$.imagePath").value("updated.png"));

                EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
        }

        @Test
        void testCreateForDoctorAndPatient() throws Exception {
                DiagnosisRestController.DiagnosisRequest request = new DiagnosisRestController.DiagnosisRequest();
                request.setDescription("Diagnosis via path");
                request.setDate(LocalDate.of(2025, 3, 1));
                request.setImagePath("path.png");

                Diagnosis createdDiagnosis = new Diagnosis();
                createdDiagnosis.setId(20);
                createdDiagnosis.setDescription("Diagnosis via path");
                createdDiagnosis.setDate(LocalDate.of(2025, 3, 1));
                createdDiagnosis.setImagePath("path.png");

                EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
                EasyMock.expect(patientRepository.findById(2)).andReturn(Optional.of(patient));
                EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andReturn(createdDiagnosis);

                EasyMock.replay(diagnosisRepository, doctorRepository, patientRepository);

                mockMvc.perform(post("/diagnoses/doctors/1/patients/2/diagnoses").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Diagnosis via path")).andExpect(jsonPath("$.imagePath").value("path.png"));

                EasyMock.verify(diagnosisRepository, doctorRepository, patientRepository);
        }

}
