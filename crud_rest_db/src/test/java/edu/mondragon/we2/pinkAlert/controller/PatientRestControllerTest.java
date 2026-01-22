package edu.mondragon.we2.pinkAlert.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.UserService;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private DiagnosisService diagnosisService;

    @Captor
    private ArgumentCaptor<List<Diagnosis>> diagnosesCaptor;

    @Captor
    private ArgumentCaptor<List<Map<String, Object>>> diagnosesViewCaptor;

    private PatientController patientController;

    @BeforeEach
    void setUp() {
        patientController = new PatientController(userService, diagnosisService);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController)
                .setViewResolvers(new DummyViewResolver())
                .build();
    }

 
    @Test
    void testDashboard_UserNotPatient_RedirectsToLogin() {
        HttpSession session = mock(HttpSession.class);
        User adminUser = createUser(1, "admin", Role.ADMIN);
        when(session.getAttribute("loggedUser")).thenReturn(adminUser);

        Model model = mock(Model.class);

        String result = patientController.dashboard(model, session);
        assertThat(result).isEqualTo("redirect:/login");
    }

    @Test
    void testDashboard_PatientWithoutLinkedPatient_ShowsError() {
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patient", Role.PATIENT);
        patientUser.setPatient(null); 

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);

        Model model = mock(Model.class);
        String result = patientController.dashboard(model, session);
        assertThat(result).isEqualTo("patient/patient-dashboard");
        verify(model).addAttribute("error", "No Patient profile linked to this user.");
    }

    @Test
    void testDashboard_DiagnosesSortedByUrgencyAndDate() {
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patient", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patientUser.linkPatient(patient);

        LocalDate today = LocalDate.now();
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, false, true, FinalResult.BENIGN, today.minusDays(3)), 
                createDiagnosis(2, true, false, null, today.minusDays(1)),  
                createDiagnosis(3, true, true, FinalResult.MALIGNANT, today.minusDays(5)),
                createDiagnosis(4, false, false, null, today.minusDays(2))             
        );

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);
        when(diagnosisService.findByPatient(1)).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);
        ArgumentCaptor<List<Map<String, Object>>> viewCaptor = ArgumentCaptor.forClass(List.class);
        patientController.dashboard(model, session);

        verify(model).addAttribute(eq("diagnoses"), viewCaptor.capture());

        List<Map<String, Object>> processedDiagnoses = viewCaptor.getValue();
    
        assertThat(processedDiagnoses).hasSize(4);
    
        assertThat(processedDiagnoses.get(0).get("id")).isEqualTo(2);
        assertThat((Boolean) processedDiagnoses.get(0).get("urgent")).isTrue();
    }

    @Test
    void testDiagnosisDetails_ValidDiagnosis_ShowsDetails() {
 
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId, true, true, FinalResult.MALIGNANT, LocalDate.now());
        Patient patient = new Patient();
        patient.setId(1);
        diagnosis.setPatient(patient);

        List<Diagnosis> historyDiagnoses = Arrays.asList(
                diagnosis,
                createDiagnosis(2, false, true, FinalResult.BENIGN, LocalDate.now().minusDays(10))
        );

        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(1)).thenReturn(historyDiagnoses);

        Model model = mock(Model.class);
        String result = patientController.diagnosisDetails(diagnosisId, model);
        assertThat(result).isEqualTo("patient/patient-diagnosis");
        verify(model).addAttribute("diagnosis", diagnosis);
        verify(model).addAttribute(PatientController.ONE_PATIENT, patient);
        verify(model).addAttribute(eq("historyDiagnoses"), anyList());
    }

    @Test
    void testDiagnosisDetails_HistorySortedByDateDescending() {
   
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId, true, true, FinalResult.MALIGNANT, LocalDate.now());
        Patient patient = new Patient();
        patient.setId(1);
        diagnosis.setPatient(patient);

        List<Diagnosis> historyDiagnoses = Arrays.asList(
                createDiagnosis(3, false, true, FinalResult.BENIGN, LocalDate.now().minusDays(30)), 
                createDiagnosis(2, true, false, null, LocalDate.now().minusDays(10)),          
                diagnosis                                                               
        );

        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(1)).thenReturn(historyDiagnoses);

        Model model = mock(Model.class);
        ArgumentCaptor<List<Diagnosis>> historyCaptor = ArgumentCaptor.forClass(List.class);

        patientController.diagnosisDetails(diagnosisId, model);
        verify(model).addAttribute(eq("historyDiagnoses"), historyCaptor.capture());
        
        List<Diagnosis> sortedHistory = historyCaptor.getValue();
        assertThat(sortedHistory).hasSize(3);
        
        assertThat(sortedHistory.get(0).getId()).isEqualTo(1); 
        assertThat(sortedHistory.get(1).getId()).isEqualTo(2); 
        assertThat(sortedHistory.get(2).getId()).isEqualTo(3); 
    }



    @Test
    void testDashboard_SessionWithWrongObjectType() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loggedUser")).thenReturn("not-a-user-object");
        Model model = mock(Model.class);
        String result = patientController.dashboard(model, session);
        assertThat(result).isEqualTo("redirect:/login");
    }

    @Test
    void testDashboard_UserServiceReturnsDifferentUser() {
        HttpSession session = mock(HttpSession.class);
        User sessionUser = createUser(1, "sessionuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        sessionUser.linkPatient(patient);

        User dbUser = createUser(1, "dbuser", Role.PATIENT); 
        dbUser.linkPatient(patient);

        when(session.getAttribute("loggedUser")).thenReturn(sessionUser);
        when(userService.get(1)).thenReturn(dbUser);
        when(diagnosisService.findByPatient(1)).thenReturn(Collections.emptyList());

        Model model = mock(Model.class);

        String result = patientController.dashboard(model, session);

        assertThat(result).isEqualTo("patient/patient-dashboard");
        verify(model).addAttribute("user", dbUser); }

  

    private User createUser(Integer id, String username, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setFullName(username + " FullName");
        user.setRole(role);
        return user;
    }

    private Diagnosis createDiagnosis(Integer id, boolean urgent, boolean reviewed, 
                                     FinalResult finalResult, LocalDate date) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setUrgent(urgent);
        diagnosis.setReviewed(reviewed);
        diagnosis.setFinalResult(finalResult);
        diagnosis.setDate(date);
        return diagnosis;
    }

    private static class DummyViewResolver implements ViewResolver {
        @Override
        public View resolveViewName(String viewName, Locale locale) {
            return (model, request, response) -> {
                
            };
        }
    }
@Test
void testDashboard_MockMvc_ValidPatient() throws Exception {

    User patientUser = createUser(1, "patientuser", Role.PATIENT);
    Patient patient = new Patient();
    patient.setId(1);
    patientUser.linkPatient(patient);

    MockHttpSession session = new MockHttpSession();
    session.setAttribute("loggedUser", patientUser);

    when(userService.get(1)).thenReturn(patientUser);
    when(diagnosisService.findByPatient(1)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/patient/dashboard")
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("patient/patient-dashboard"));
}

}