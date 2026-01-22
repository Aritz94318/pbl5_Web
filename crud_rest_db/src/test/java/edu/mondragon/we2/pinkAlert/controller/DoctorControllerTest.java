package edu.mondragon.we2.pinkAlert.controller;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DoctorControllerTest extends EasyMockSupport {

    private DiagnosisRepository diagnosisRepository;
    private DiagnosisService diagnosisService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        diagnosisRepository = mock(DiagnosisRepository.class);

        diagnosisService = new DiagnosisService(diagnosisRepository,null,null);

        DoctorController controller = new DoctorController(diagnosisService);

        InternalResourceViewResolver viewResolver =new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
    }

    @Test
    void testDashboard() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setUrgent(true);

        Patient patient = new Patient();
        patient.setId(1);
        diagnosis.setPatient(patient);

        EasyMock.expect(diagnosisRepository.findByDateOrderByUrgentDesc(EasyMock.anyObject(LocalDate.class))).andReturn(List.of(diagnosis));

        EasyMock.replay(diagnosisRepository);

        mockMvc.perform(get("/doctor/dashboard")).andExpect(status().isOk()).andExpect(view().name("doctor/doctor-dashboard")).andExpect(model().attributeExists("diagnoses","totalCount", "urgentCount","routineCount","previousScreenings","selectedDate","datePills"));

        EasyMock.verify(diagnosisRepository);
    }
}
