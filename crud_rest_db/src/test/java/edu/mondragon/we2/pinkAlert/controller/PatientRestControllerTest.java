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

    // ==============================
    // TESTS PARA DASHBOARD
    // ==============================

 
    @Test
    void testDashboard_UserNotPatient_RedirectsToLogin() {
        // Given - Usuario logueado pero no es paciente
        HttpSession session = mock(HttpSession.class);
        User adminUser = createUser(1, "admin", Role.ADMIN);
        when(session.getAttribute("loggedUser")).thenReturn(adminUser);

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then
        assertThat(result).isEqualTo("redirect:/login");
    }

    @Test
    void testDashboard_PatientWithoutLinkedPatient_ShowsError() {
        // Given - Usuario paciente pero sin perfil de paciente vinculado
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patient", Role.PATIENT);
        patientUser.setPatient(null); // Sin paciente vinculado

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then
        assertThat(result).isEqualTo("patient/patient-dashboard");
        verify(model).addAttribute("error", "No Patient profile linked to this user.");
    }

   /*  @Test
    void testDashboard_ValidPatient_ShowsDashboardWithDiagnoses() {
        // Given - Usuario paciente válido con diagnósticos
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patientuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patientUser.linkPatient(patient);

        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT, LocalDate.now().minusDays(3)),
                createDiagnosis(2, false, false, null, LocalDate.now().minusDays(1)),
                createDiagnosis(3, true, false, null, LocalDate.now().minusDays(2))
        );

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);
        when(diagnosisService.findByPatient(1)).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then
        assertThat(result).isEqualTo("patient/patient-dashboard");

        // Verificar que se agregan los atributos básicos
        verify(model).addAttribute("user", patientUser);
        verify(model).addAttribute("patient", patient);

        // Verificar estadísticas
        verify(model).addAttribute("previousScreenings", 3L);
        verify(model).addAttribute("totalCount", 3);
        verify(model).addAttribute("urgentCount", 2L); // IDs 1 y 3 son urgent
        verify(model).addAttribute("reviewedCount", 1L); // Solo ID 1 está revisado
        verify(model).addAttribute("pendingCount", 2L); // IDs 2 y 3 no revisados
        verify(model).addAttribute("upcomingCount", 0L);

        // Verificar que se procesan los diagnósticos
        verify(model).addAttribute(eq("diagnoses"), anyList());
    }
 */
    @Test
    void testDashboard_DiagnosesSortedByUrgencyAndDate() {
        // Given - Diagnósticos desordenados para probar el sorting
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patient", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patientUser.linkPatient(patient);

        LocalDate today = LocalDate.now();
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, false, true, FinalResult.BENIGN, today.minusDays(3)),  // No urgente, viejo
                createDiagnosis(2, true, false, null, today.minusDays(1)),                // Urgente, reciente
                createDiagnosis(3, true, true, FinalResult.MALIGNANT, today.minusDays(5)), // Urgente, viejo
                createDiagnosis(4, false, false, null, today.minusDays(2))                 // No urgente, medio
        );

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);
        when(diagnosisService.findByPatient(1)).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);
        ArgumentCaptor<List<Map<String, Object>>> viewCaptor = ArgumentCaptor.forClass(List.class);

        // When
        patientController.dashboard(model, session);

        // Then - Verificar que se captura la lista de diagnósticos procesados
        verify(model).addAttribute(eq("diagnoses"), viewCaptor.capture());

        List<Map<String, Object>> processedDiagnoses = viewCaptor.getValue();
        
        // Verificar orden: urgent primero, luego por fecha descendente dentro de cada grupo
        // Orden esperado: ID 2 (urgent, más reciente), ID 3 (urgent, más viejo), 
        // ID 4 (no urgent, más reciente), ID 1 (no urgent, más viejo)
        assertThat(processedDiagnoses).hasSize(4);
        
        // Podemos verificar que el primero es urgente (ID 2)
        // Nota: Los IDs en el Map son de tipo Integer
        assertThat(processedDiagnoses.get(0).get("id")).isEqualTo(2);
        assertThat((Boolean) processedDiagnoses.get(0).get("urgent")).isTrue();
    }

/* 
    @Test
    void testDashboard_EmptyDiagnosesList() {
        // Given - Paciente sin diagnósticos
        HttpSession session = mock(HttpSession.class);
        User patientUser = createUser(1, "patient", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patientUser.linkPatient(patient);

        when(session.getAttribute("loggedUser")).thenReturn(patientUser);
        when(userService.get(1)).thenReturn(patientUser);
        when(diagnosisService.findByPatient(1)).thenReturn(Collections.emptyList());

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then
        assertThat(result).isEqualTo("patient/patient-dashboard");
        
        // Verificar estadísticas con lista vacía
        verify(model).addAttribute("previousScreenings", 0L);
        verify(model).addAttribute("totalCount", 0);
        verify(model).addAttribute("urgentCount", 0L);
        verify(model).addAttribute("reviewedCount", 0L);
        verify(model).addAttribute("pendingCount", 0L);
        verify(model).addAttribute("upcomingCount", 0L);
        verify(model).addAttribute(eq("diagnoses"), eq(Collections.emptyList()));
    }

 */

    // ==============================
    // TESTS PARA DIAGNOSIS DETAILS
    // ==============================

    @Test
    void testDiagnosisDetails_ValidDiagnosis_ShowsDetails() {
        // Given
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

        // When
        String result = patientController.diagnosisDetails(diagnosisId, model);

        // Then
        assertThat(result).isEqualTo("patient/patient-diagnosis");
        verify(model).addAttribute("diagnosis", diagnosis);
        verify(model).addAttribute("patient", patient);
        verify(model).addAttribute(eq("historyDiagnoses"), anyList());
    }

    @Test
    void testDiagnosisDetails_HistorySortedByDateDescending() {
        // Given
        Integer diagnosisId = 1;
        Diagnosis diagnosis = createDiagnosis(diagnosisId, true, true, FinalResult.MALIGNANT, LocalDate.now());
        Patient patient = new Patient();
        patient.setId(1);
        diagnosis.setPatient(patient);

        // Historial desordenado
        List<Diagnosis> historyDiagnoses = Arrays.asList(
                createDiagnosis(3, false, true, FinalResult.BENIGN, LocalDate.now().minusDays(30)), // Más viejo
                createDiagnosis(2, true, false, null, LocalDate.now().minusDays(10)),               // Medio
                diagnosis                                                                           // Más reciente (actual)
        );

        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(1)).thenReturn(historyDiagnoses);

        Model model = mock(Model.class);
        ArgumentCaptor<List<Diagnosis>> historyCaptor = ArgumentCaptor.forClass(List.class);

        // When
        patientController.diagnosisDetails(diagnosisId, model);

        // Then - Verificar que se ordena por fecha descendente
        verify(model).addAttribute(eq("historyDiagnoses"), historyCaptor.capture());
        
        List<Diagnosis> sortedHistory = historyCaptor.getValue();
        assertThat(sortedHistory).hasSize(3);
        
        // Verificar orden: más reciente primero
        assertThat(sortedHistory.get(0).getId()).isEqualTo(1); // Diagnóstico actual (más reciente)
        assertThat(sortedHistory.get(1).getId()).isEqualTo(2); // Medio
        assertThat(sortedHistory.get(2).getId()).isEqualTo(3); // Más viejo
    }


    

    // ==============================
    // TESTS PARA CASOS ESPECIALES
    // ==============================

    @Test
    void testDashboard_SessionWithWrongObjectType() {
        // Given - Sesión con objeto que no es User
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loggedUser")).thenReturn("not-a-user-object");

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then
        assertThat(result).isEqualTo("redirect:/login");
    }

    @Test
    void testDashboard_UserServiceReturnsDifferentUser() {
        // Given - Usuario en sesión es diferente al que devuelve el servicio
        HttpSession session = mock(HttpSession.class);
        User sessionUser = createUser(1, "sessionuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        sessionUser.linkPatient(patient);

        User dbUser = createUser(1, "dbuser", Role.PATIENT); // Mismo ID, diferente nombre
        dbUser.linkPatient(patient);

        when(session.getAttribute("loggedUser")).thenReturn(sessionUser);
        when(userService.get(1)).thenReturn(dbUser);
        when(diagnosisService.findByPatient(1)).thenReturn(Collections.emptyList());

        Model model = mock(Model.class);

        // When
        String result = patientController.dashboard(model, session);

        // Then - Debería usar el usuario de la base de datos
        assertThat(result).isEqualTo("patient/patient-dashboard");
        verify(model).addAttribute("user", dbUser); // No sessionUser
    }

    // ==============================
    // MÉTODOS DE AYUDA
    // ==============================

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

    // ViewResolver dummy para pruebas de MockMvc
    private static class DummyViewResolver implements ViewResolver {
        @Override
        public View resolveViewName(String viewName, Locale locale) {
            return (model, request, response) -> {
                // No hacer nada, solo para que MockMvc funcione
            };
        }
    }
    // ==============================
// TESTS CON MOCKMVC
// ==============================

@Test
void testDashboard_MockMvc_ValidPatient() throws Exception {
    // Given
    User patientUser = createUser(1, "patientuser", Role.PATIENT);
    Patient patient = new Patient();
    patient.setId(1);
    patientUser.linkPatient(patient);

    MockHttpSession session = new MockHttpSession();
    session.setAttribute("loggedUser", patientUser);

    when(userService.get(1)).thenReturn(patientUser);
    when(diagnosisService.findByPatient(1)).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc.perform(get("/patient/dashboard")
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("patient/patient-dashboard"));
}

}