// package edu.mondragon.we2.pinkAlert.controller;

// import edu.mondragon.we2.pinkAlert.model.Diagnosis;
// import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
// import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
// import edu.mondragon.we2.pinkAlert.repository.UserRepository;
// import org.easymock.EasyMock;
// import org.easymock.EasyMockSupport;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// import java.time.LocalDate;
// import java.util.List;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// class AdminControllerTest extends EasyMockSupport {

//     private PatientRepository patientRepository;
//     private DiagnosisRepository diagnosisRepository;
//     private UserRepository userRepository;

//     private MockMvc mockMvc;

//     @BeforeEach
//     void setUp() {
//         patientRepository = mock(PatientRepository.class);
//         diagnosisRepository = mock(DiagnosisRepository.class);
//         userRepository = mock(UserRepository.class);

//         AdminController controller =new AdminController(patientRepository, diagnosisRepository, userRepository);

//         mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
//     }

//     @Test
//     void testDashboard() throws Exception {

//         Diagnosis d1 = new Diagnosis();
//         d1.setDate(LocalDate.now());
//         d1.setReviewed(true);

//         Diagnosis d2 = new Diagnosis();
//         d2.setDate(LocalDate.now());
//         d2.setReviewed(false);

//         EasyMock.expect(patientRepository.count()).andReturn(5L);
//         EasyMock.expect(userRepository.count()).andReturn(10L);
//         EasyMock.expect(diagnosisRepository.count()).andReturn(2L);
//         EasyMock.expect(diagnosisRepository.countByUrgentTrue()).andReturn(1L);
//         EasyMock.expect(diagnosisRepository.countByReviewedTrue()).andReturn(1L);
//         EasyMock.expect(diagnosisRepository.findAll()).andReturn(List.of(d1, d2)).anyTimes();


//         EasyMock.replay(patientRepository, userRepository, diagnosisRepository);

//         mockMvc.perform(get("/admin/dashboard").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(view().name("admin-dashboard")).andExpect(model().attribute("totalPatients", 5L)).andExpect(model().attribute("totalUsers", 10L)).andExpect(model().attribute("totalScreenings", 2L)).andExpect(model().attribute("urgentCases", 1L)).andExpect(model().attribute("completionRate", 50.0)).andExpect(model().attribute("positiveRate", 0.0)).andExpect(model().attributeExists("negativeCount","positiveCount","pendingCount","inconclusiveCount","timelineLabelsJs","timelineTotalJs","timelineCompletedJs"));

//         EasyMock.verify(patientRepository, userRepository, diagnosisRepository);
//     }
// }
