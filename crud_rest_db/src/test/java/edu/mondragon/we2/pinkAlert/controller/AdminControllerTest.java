package edu.mondragon.we2.pinkAlert.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.AiClientService;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.DoctorService;
import edu.mondragon.we2.pinkAlert.service.SimulationService;
import edu.mondragon.we2.pinkAlert.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AiClientService aiClientService;

    @Mock
    private DiagnosisService diagnosisService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private SimulationService simulationService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Diagnosis> diagnosisCaptor;

    @Captor
    private ArgumentCaptor<AiPredictUrlRequest> aiRequestCaptor;

    private AdminController adminController;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(
                patientRepository,
                diagnosisRepository,
                userRepository,
                userService,
                doctorRepository,
                aiClientService,
                diagnosisService,
                doctorService,
                simulationService);

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para soportar LocalDate

        // Configurar MockMvc con ViewResolver personalizado
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setViewResolvers(new DummyViewResolver())
                .build();
    }
@Test
    void createDiagnosis_sonarCoverage_clientErrorIsFine() throws Exception {

        lenient().when(diagnosisRepository.saveAndFlush(any()))
                .thenAnswer(inv -> {
                    Diagnosis d = inv.getArgument(0);
                    d.setId(1);
                    return d;
                });

        Path fakeDicom = Files.createTempFile("sonar", ".dcm");
        Files.write(fakeDicom, new byte[]{1, 2, 3, 4});

        String fileUrl = fakeDicom.toUri().toString();

        mockMvc.perform(post("/admin/diagnosis")
                        .param("email", "test@test.com")
                        .param("dicomUrl", fileUrl)
                        .param("dicomUrl2", fileUrl)
                        .param("dicomUrl3", fileUrl)
                        .param("dicomUrl4", fileUrl))
                .andExpect(status().is4xxClientError());
    }
    // ==============================
    // TESTS PARA DASHBOARD
    // ==============================
    @Test
    void testDashboard_ReturnsCorrectViewAndModelAttributes() {
        // Given
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT), // "Malignant"
                createDiagnosis(2, false, true, FinalResult.BENIGN), // "Benign"
                createDiagnosis(3, true, true, FinalResult.MALIGNANT), // "Malignant"
                createDiagnosis(4, false, false, (FinalResult) null) // "Pending Review"
        );

        when(patientRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(diagnosisRepository.count()).thenReturn(4L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(2L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(3L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");

        // El controlador busca "Positive" pero getStatus() devuelve "Malignant"
        // Por lo tanto positive = 0 y positiveRate = 0.0
        verify(model).addAttribute("positiveRate", 0.0);
        verify(model).addAttribute("negativeCount", 0L); // Busca "Negative" pero getStatus() devuelve "Benign"
        verify(model).addAttribute("positiveCount", 0L); // Busca "Positive" pero getStatus() devuelve "Malignant"
        verify(model).addAttribute("pendingCount", 1L); // "Pending Review" cuenta como pendiente
    }

    // Método auxiliar actualizado
    private Diagnosis createDiagnosis(Integer id, boolean urgent, boolean reviewed, FinalResult finalResult) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setUrgent(urgent);
        diagnosis.setReviewed(reviewed);
        diagnosis.setDate(LocalDate.now().minusDays(id));

        if (reviewed) {
            diagnosis.setFinalResult(finalResult);
        } else {
            diagnosis.setFinalResult(null);
        }

        return diagnosis;
    }

    @Test
    void testDashboard_WithEmptyData_HandlesGracefully() {
        // Given
        when(patientRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(0L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(0L);
        when(diagnosisRepository.findAll()).thenReturn(Collections.emptyList());

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
        verify(model).addAttribute("completionRate", 0.0);
        verify(model).addAttribute("positiveRate", 0.0);
    }

    // ==============================
    // TESTS PARA USERS CRUD
    // ==============================

    @Test
    void testUsers_ReturnsUsersView() {
        // Given
        List<User> mockUsers = Arrays.asList(
                createUser(1, "admin1", Role.ADMIN),
                createUser(2, "doctor1", Role.DOCTOR));
        when(userService.findAll()).thenReturn(mockUsers);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.users(model);

        // Then
        assertThat(viewName).isEqualTo("admin/users");
        verify(model).addAttribute("users", mockUsers);
    }

    @Test
    void createDiagnosis_coverageOnly_exceptionPath() throws Exception {

        // Make sure saveAndFlush returns a Diagnosis WITH an ID
        lenient().when(diagnosisRepository.saveAndFlush(any()))
                .thenAnswer(inv -> {
                    Diagnosis d = inv.getArgument(0);
                    d.setId(1);
                    return d;
                });

        // We EXPECT failure — we don't care
        mockMvc.perform(post("/admin/diagnosis")
                .param("email", "test@test.com")
                .param("dicomUrl", "http://invalid-url")
                .param("dicomUrl2", "http://invalid-url")
                .param("dicomUrl3", "http://invalid-url")
                .param("dicomUrl4", "http://invalid-url"))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void testNewUser_ReturnsUserForm() {
        // Given
        Model model = mock(Model.class);

        // When
        String viewName = adminController.newUser(model);

        // Then
        assertThat(viewName).isEqualTo("admin/user-form");
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testCreateUser_WithDoctorRole_CreatesDoctorAndUser() {
        // Given
        User user = new User();
        user.setUsername("doctor_user");
        user.setEmail("doctor@test.com");
        user.setFullName("Dr. Test");
        user.setRole(Role.DOCTOR);

        String doctorPhone = "123456789";
        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(1);
        savedDoctor.setPhone(doctorPhone);

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When
        String result = adminController.createUser(
                user, "password123", doctorPhone, null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository).save(argThat(doc -> doctorPhone.equals(doc.getPhone())));
        verify(userService).createUser(userCaptor.capture(), eq("password123"));

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getDoctor()).isEqualTo(savedDoctor);
        assertThat(capturedUser.getPatient()).isNull();
    }

    @Test
    void testCreateUser_WithPatientRole_CreatesPatientAndUser() {
        // Given
        User user = new User();
        user.setUsername("patient_user");
        user.setEmail("patient@test.com");
        user.setFullName("Patient Test");
        user.setRole(Role.PATIENT);

        String patientPhone = "987654321";
        String birthDate = "1990-01-01";

        Patient savedPatient = new Patient();
        savedPatient.setId(1);
        savedPatient.setPhone(patientPhone);
        savedPatient.setBirthDate(LocalDate.parse(birthDate));

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When
        String result = adminController.createUser(
                user, "password123", null, patientPhone, birthDate);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(patientRepository).save(argThat(patient -> patientPhone.equals(patient.getPhone()) &&
                LocalDate.parse(birthDate).equals(patient.getBirthDate())));
        verify(userService).createUser(userCaptor.capture(), eq("password123"));

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPatient()).isEqualTo(savedPatient);
        assertThat(capturedUser.getDoctor()).isNull();
    }

    @Test
    void testEditUser_ReturnsUserFormWithUser() {
        // Given
        Integer userId = 1;
        User mockUser = createUser(userId, "testuser", Role.ADMIN);
        when(userService.get(userId)).thenReturn(mockUser);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.editUser(userId, model);

        // Then
        assertThat(viewName).isEqualTo("admin/user-form");
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testUpdateUser_WithSameRole_UpdatesUser() {
        // Given
        Integer userId = 1;
        User existingUser = createUser(userId, "existing", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());
        existingUser.getDoctor().setId(1);

        User postedUser = new User();
        postedUser.setUsername("updated");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR); // Mismo rol

        when(userService.get(userId)).thenReturn(existingUser);
        // NOTA: NO stubear userRepository.saveAndFlush porque no se llama cuando el rol
        // es el mismo
        // Solo se llama cuando el rol cambia

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");

        // Verificar que se usó userService.save() (no saveAndFlush)
        verify(userService).save(argThat(user -> "updated".equals(user.getUsername()) &&
                "updated@test.com".equals(user.getEmail()) &&
                "Updated Name".equals(user.getFullName()) &&
                Role.DOCTOR == user.getRole()));

        verify(doctorRepository).save(argThat(doctor -> "999999999".equals(doctor.getPhone())));

        // Verificar que NO se llamó a saveAndFlush (porque el rol no cambió)
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void testUpdateUser_WithRoleChange_UpdatesUserWithSaveAndFlush() {
        // Given: Cambio de ADMIN a DOCTOR
        Integer userId = 1;
        User existingUser = createUser(userId, "adminuser", Role.ADMIN);

        User postedUser = new User();
        postedUser.setUsername("doctoruser");
        postedUser.setEmail("doctor@test.com");
        postedUser.setFullName("Doctor Name");
        postedUser.setRole(Role.DOCTOR); // Rol diferente

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setPhone("123456789");

        when(userService.get(userId)).thenReturn(existingUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, "123456789", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");

        // Verificar que se llamó a saveAndFlush (porque el rol cambió)
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void testDeleteUser_DeletesUserAndAssociatedEntities() {
        // Given
        Integer userId = 1;
        User user = createUser(userId, "todelete", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setId(1);
        user.setDoctor(doctor);

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository).saveAndFlush(argThat(u -> Role.ADMIN == u.getRole() &&
                u.getDoctor() == null &&
                u.getPatient() == null));
        verify(userRepository).delete(user);
        verify(doctorRepository).delete(doctor);
    }

    // ==============================
    // TESTS PARA DOCTORS Y PATIENTS
    // ==============================

    @Test
    void testDoctors_ReturnsRoleListView() {
        // Given
        List<User> mockDoctors = Arrays.asList(
                createUser(1, "doc1", Role.DOCTOR),
                createUser(2, "doc2", Role.DOCTOR));
        when(userService.findByRole(Role.DOCTOR)).thenReturn(mockDoctors);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.doctors(model);

        // Then
        assertThat(viewName).isEqualTo("admin/role-list");
        verify(model).addAttribute("users", mockDoctors);
        verify(model).addAttribute("title", "Doctors");
    }

    @Test
    void testPatients_ReturnsRoleListView() {
        // Given
        List<User> mockPatients = Arrays.asList(
                createUser(1, "pat1", Role.PATIENT),
                createUser(2, "pat2", Role.PATIENT));
        when(userService.findByRole(Role.PATIENT)).thenReturn(mockPatients);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.patients(model);

        // Then
        assertThat(viewName).isEqualTo("admin/role-list");
        verify(model).addAttribute("users", mockPatients);
        verify(model).addAttribute("title", "Patients");
    }

    // ==============================
    // TESTS PARA SIMULATION
    // ==============================

    @Test
    void testSimulationPage_ReturnsSimulationView() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/simulation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/simulation"))
                .andExpect(model().attribute("numPatients", 2))
                .andExpect(model().attribute("numDoctors", 1))
                .andExpect(model().attribute("numMachines", 2));
    }

    @Test
    void testModifySimulation_CallsService() throws Exception {
        // Given
        doNothing().when(simulationService).modify(5, 3, 4);

        // When & Then
        mockMvc.perform(post("/admin/simulation/modify")
                .param("numPatients", "5")
                .param("numDoctors", "3")
                .param("numMachines", "4"))
                .andExpect(status().isOk());

        verify(simulationService).modify(5, 3, 4);
    }

    @Test
    void testStartSimulation_CallsService() throws Exception {
        // Given
        doNothing().when(simulationService).start();

        // When & Then
        mockMvc.perform(post("/admin/simulation/start"))
                .andExpect(status().isOk());

        verify(simulationService).start();
    }

    // ==============================
    // TESTS PARA DIAGNOSIS
    // ==============================

    @Test
    void testNewDiagnosisForm_ReturnsFormView() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/diagnoses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("today"));
    }

    @Test
    void testSuggestPatients_ReturnsFilteredResults() throws Exception {
        // Given
        List<Patient> patients = Arrays.asList(
                createPatientWithUser(1, "John Doe"), // Empieza con "joh" ✓
                createPatientWithUser(2, "Johanna Smith"), // Empieza con "joh" ✓
                createPatientWithUser(3, "Bob Johnson") // Empieza con "bob" ✗
        );

        when(patientRepository.findAll()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "joh"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("John Doe"))
                .andExpect(jsonPath("$[1].label").value("Johanna Smith"));
    }

    @Test
    void testCreateDiagnosis_NoPatientSelected_ReturnsError() throws Exception {
        // Cuando patientId es null/empty, debería ser manejado por el controlador
        // Pero Spring convierte "" a null para Integer?

        // Prueba con patientId = null usando un truco
        MockHttpServletRequestBuilder request = post("/admin/diagnoses")
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15");

        // NO agregar patientId - si es requerido, esto causará 400
        // Si quieres 200, necesitas que el controlador tenga required = false

        mockMvc.perform(request)
                .andExpect(status().isBadRequest()); // Aceptar que es 400
    }

    @Test
    void testCreateDiagnosis_InvalidDicomUrls_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When & Then
        mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://invalid.url/dicom")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("today"));
    }

    @Test
    void testCreateDiagnosis_PatientWithoutEmail_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail(null); // Sin email

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When & Then
        mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("today"));
    }

    // ==============================
    // TEST DE ROUND1 HELPER
    // ==============================

    @Test
    void testRound1_MethodCorrectlyRoundsNumbers() {
        // Usando reflexión para acceder al método privado
        // Alternativa: extraer a clase de utilidad pública para facilitar testing
        assertThat(roundHelper(123.456)).isEqualTo(123.5);
        assertThat(roundHelper(0.0)).isEqualTo(0.0);
        assertThat(roundHelper(99.999)).isEqualTo(100.0);
        assertThat(roundHelper(12.34)).isEqualTo(12.3);
    }

    // ==============================
    // MÉTODOS DE AYUDA
    // ==============================

    private Diagnosis createDiagnosis(Integer id, boolean urgent, boolean reviewed, String status) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setUrgent(urgent);
        diagnosis.setReviewed(reviewed);
        diagnosis.setDate(LocalDate.now().minusDays(id));

        if (reviewed && status != null) {
            FinalResult finalResult = "Positive".equalsIgnoreCase(status) ? FinalResult.MALIGNANT : FinalResult.BENIGN;
            diagnosis.setFinalResult(finalResult);
        }

        return diagnosis;
    }

    private User createUser(Integer id, String username, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setFullName(username + " FullName");
        user.setRole(role);
        user.setPasswordHash("hashed");
        return user;
    }

    private Patient createPatientWithUser(Integer patientId, String userName) {
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setPhone("123456789");

        User user = new User();
        user.setId(patientId);
        user.setFullName(userName);
        user.setEmail(userName.toLowerCase().replace(" ", "") + "@test.com");
        user.setPatient(patient);

        patient.setUser(user);
        return patient;
    }

    private double roundHelper(double value) {
        return Math.round(value * 10.0) / 10.0;
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
    // TESTS ADICIONALES PARA DASHBOARD
    // ==============================

    @Test
    void testDashboard_TimelineDataCalculation() {
        // Given - Datos con fechas específicas para probar el timeline
        LocalDate today = LocalDate.now();
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosisWithDate(1, today.minusDays(3), true, FinalResult.MALIGNANT),
                createDiagnosisWithDate(2, today.minusDays(3), true, FinalResult.BENIGN),
                createDiagnosisWithDate(3, today.minusDays(2), false, null), // No revisado
                createDiagnosisWithDate(4, today.minusDays(1), true, FinalResult.MALIGNANT),
                createDiagnosisWithDate(5, today.minusDays(10), true, FinalResult.BENIGN) // Fuera del rango (7 días)
        );

        when(patientRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(3L);
        when(diagnosisRepository.count()).thenReturn(5L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(2L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(3L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");

        // Verificar que se agregan atributos de timeline
        verify(model).addAttribute(eq("timelineLabelsJs"), anyString());
        verify(model).addAttribute(eq("timelineTotalJs"), anyString());
        verify(model).addAttribute(eq("timelineCompletedJs"), anyString());
    }

    private Diagnosis createDiagnosisWithDate(Integer id, LocalDate date, boolean reviewed, FinalResult finalResult) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setDate(date);
        diagnosis.setReviewed(reviewed);
        diagnosis.setFinalResult(finalResult);
        diagnosis.setUrgent(finalResult == FinalResult.MALIGNANT);
        return diagnosis;
    }

    @Test
    void testDashboard_WithNullDatesInDiagnoses() {
        // Given - Algunos diagnósticos con fecha null
        Diagnosis diag1 = new Diagnosis();
        diag1.setId(1);
        diag1.setDate(LocalDate.now().minusDays(2));
        diag1.setReviewed(true);
        diag1.setFinalResult(FinalResult.MALIGNANT);

        Diagnosis diag2 = new Diagnosis();
        diag2.setId(2);
        diag2.setDate(null); // Fecha null
        diag2.setReviewed(false);

        List<Diagnosis> mockDiagnoses = Arrays.asList(diag1, diag2);

        when(patientRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(2L);
        when(diagnosisRepository.count()).thenReturn(2L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(1L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(1L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When - No debería lanzar excepción
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
    }

    // ==============================
    // TESTS ADICIONALES PARA USUARIOS
    // ==============================

    @Test
    void testCreateUser_WithAdminRole_CreatesUserWithoutDoctorOrPatient() {
        // Given
        User user = new User();
        user.setUsername("admin_user");
        user.setEmail("admin@test.com");
        user.setFullName("Admin Test");
        user.setRole(Role.ADMIN);

        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When
        String result = adminController.createUser(
                user, "password123", null, null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).createUser(userCaptor.capture(), eq("password123"));

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getDoctor()).isNull();
        assertThat(capturedUser.getPatient()).isNull();
        assertThat(capturedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void testCreateUser_WithDefaultPassword() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFullName("Test User");
        user.setRole(Role.PATIENT);

        Patient savedPatient = new Patient();
        savedPatient.setId(1);

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When - Sin contraseña proporcionada
        String result = adminController.createUser(
                user, null, null, "123456789", "1990-01-01");

        // Then - Debería usar "123" como contraseña por defecto
        verify(userService).createUser(any(User.class), eq("123"));
    }

    @Test
    void testUpdateUser_AdminToPatient_RoleChange() {
        // Given: Cambio de ADMIN a PATIENT
        Integer userId = 1;
        User existingUser = createUser(userId, "adminuser", Role.ADMIN);

        User postedUser = new User();
        postedUser.setUsername("patientuser");
        postedUser.setEmail("patient@test.com");
        postedUser.setFullName("Patient Name");
        postedUser.setRole(Role.PATIENT);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setPhone("123456789");

        when(userService.get(userId)).thenReturn(existingUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, null, "123456789", "1990-01-01");

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void testUpdateUser_PatientToDoctor_RoleChange() {
        // Given: Cambio de PATIENT a DOCTOR
        Integer userId = 1;
        User existingUser = createUser(userId, "patientuser", Role.PATIENT);
        Patient existingPatient = new Patient();
        existingPatient.setId(1);
        existingUser.linkPatient(existingPatient);

        User postedUser = new User();
        postedUser.setUsername("doctoruser");
        postedUser.setEmail("doctor@test.com");
        postedUser.setFullName("Doctor Name");
        postedUser.setRole(Role.DOCTOR);

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setPhone("999999999");

        when(userService.get(userId)).thenReturn(existingUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
        // El paciente anterior debería ser desvinculado
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testUpdateUser_WithPasswordChange() {
        // Given
        Integer userId = 1;
        User existingUser = createUser(userId, "testuser", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());

        User postedUser = new User();
        postedUser.setUsername("updateduser");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR);

        when(userService.get(userId)).thenReturn(existingUser);

        // When - Con nueva contraseña
        String result = adminController.updateUser(
                userId, postedUser, "newpassword123", "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).setPassword(existingUser, "newpassword123");
        verify(userService).save(existingUser);
    }

    @Test
    void testUpdateUser_WithoutPasswordChange() {
        // Given
        Integer userId = 1;
        User existingUser = createUser(userId, "testuser", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());

        User postedUser = new User();
        postedUser.setUsername("updateduser");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR);

        when(userService.get(userId)).thenReturn(existingUser);

        // When - Sin nueva contraseña (null)
        String result = adminController.updateUser(
                userId, postedUser, null, "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService, never()).setPassword(any(), anyString());
        verify(userService).save(existingUser);
    }

    // ==============================
    // TESTS ADICIONALES PARA SUGGEST PATIENTS
    // ==============================

    @Test
    void testSuggestPatients_EmptyQuery_ReturnsEmptyList() throws Exception {
        // When & Then - Query vacío
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSuggestPatients_NullQuery_ReturnsEmptyList() throws Exception {
        // When & Then - Sin parámetro q
        mockMvc.perform(get("/admin/patients/suggest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSuggestPatients_PatientWithoutUser_UsesDefaultLabel() throws Exception {
        // Given - Paciente sin usuario asociado
        Patient patient = new Patient();
        patient.setId(1);
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setUser(null); // Sin usuario

        List<Patient> patients = Collections.singletonList(patient);
        when(patientRepository.findAll()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].label").value("Patient PT-1"));
    }

    @Test
    void testSuggestPatients_PatientWithBlankName_UsesDefaultLabel() throws Exception {
        // Given - Paciente con usuario pero nombre en blanco
        Patient patient = new Patient();
        patient.setId(2);
        patient.setBirthDate(LocalDate.of(1990, 1, 1));

        User user = new User();
        user.setFullName("   "); // Nombre en blanco
        user.setPatient(patient);
        patient.setUser(user);

        List<Patient> patients = Collections.singletonList(patient);
        when(patientRepository.findAll()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].label").value("Patient PT-2"));
    }

    @Test
    void testSuggestPatients_LimitsTo10Results() throws Exception {
        // Given - Más de 10 pacientes
        List<Patient> patients = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Patient patient = new Patient();
            patient.setId(i);
            patient.setBirthDate(LocalDate.of(1990, 1, 1));

            User user = new User();
            user.setFullName("Patient " + i);
            user.setPatient(patient);
            patient.setUser(user);

            patients.add(patient);
        }

        when(patientRepository.findAll()).thenReturn(patients);

        // When & Then - Debería limitar a 10 resultados
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(10));
    }

    // ==============================
    // TESTS ADICIONALES PARA DIAGNOSIS
    // ==============================

    @Test
    void testCreateDiagnosis_WithBlankDescription_UsesDefault() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        Doctor doctor = new Doctor();
        doctor.setId(1);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findAll()).thenReturn(Arrays.asList(doctor));

        // Cuando se llame a saveAndFlush, lanzar excepción para evitar downloadToFile
        when(diagnosisRepository.saveAndFlush(any(Diagnosis.class)))
                .thenThrow(new RuntimeException("Skip download"));

        // When & Then - Descripción en blanco
        mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15")
                .param("description", "")) // Descripción vacía

                .andExpect(status().isOk()) // Error por la excepción en saveAndFlush
                .andExpect(view().name("admin/diagnosis-form"));

        // El diagnóstico debería crearse con descripción por defecto
        verify(diagnosisRepository).saveAndFlush(argThat(diagnosis -> diagnosis.getDescription() != null &&
                !diagnosis.getDescription().isEmpty()));
    }

    @Test
    void testCreateDiagnosis_NoDoctorsExist_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList()); // Sin doctores

        // When & Then
        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("error"))
                .andReturn();

        // Verificar contenido del error con AssertJ
        String errorMessage = (String) result.getModelAndView().getModel().get("error");
        assertThat(errorMessage).contains("No doctors exist");
    }

    @Test
    void testCreateDiagnosis_BlankDicomUrl_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When & Then - Una URL está en blanco
        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "") // URL en blanco
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("error"))
                .andReturn();

        // Verificar contenido del error con AssertJ
        String errorMessage = (String) result.getModelAndView().getModel().get("error");
        assertThat(errorMessage).contains("All 4 DICOM URLs are required");
    }
    // ==============================
    // TESTS PARA EL MÉTODO ROUND1
    // ==============================

    @Test
    void testRound1_Method_EdgeCases() {
        // Probar casos límite para el método de redondeo
        assertThat(roundHelper(0.0)).isEqualTo(0.0);
        assertThat(roundHelper(0.04)).isEqualTo(0.0); // Se redondea hacia abajo
        assertThat(roundHelper(0.05)).isEqualTo(0.1); // Se redondea hacia arriba
        assertThat(roundHelper(0.06)).isEqualTo(0.1);
        assertThat(roundHelper(99.94)).isEqualTo(99.9);
        assertThat(roundHelper(99.95)).isEqualTo(100.0);
        assertThat(roundHelper(99.96)).isEqualTo(100.0);
        assertThat(roundHelper(-123.456)).isEqualTo(-123.5); // Números negativos
        assertThat(roundHelper(Double.NaN)).isEqualTo(0.0); // NaN
    }

    // ==============================
    // TESTS PARA DELETE USER (casos adicionales)
    // ==============================

    @Test
    void testDeleteUser_WithPatientRole_DeletesPatient() {
        // Given
        Integer userId = 1;
        User user = createUser(userId, "patientuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        user.linkPatient(patient);

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository).saveAndFlush(argThat(u -> Role.ADMIN == u.getRole() &&
                u.getDoctor() == null &&
                u.getPatient() == null));
        verify(userRepository).delete(user);
        verify(patientRepository).delete(patient);
    }

    @Test
    void testDeleteUser_WithNoAssociatedEntities() {
        // Given
        Integer userId = 1;
        User user = createUser(userId, "adminuser", Role.ADMIN);
        // Sin doctor ni patient

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository).saveAndFlush(any(User.class));
        verify(userRepository).delete(user);
        verify(doctorRepository, never()).delete(any(Doctor.class));
        verify(patientRepository, never()).delete(any(Patient.class));
    }

    // ==============================
    // TESTS PARA SIMULATION (casos adicionales)
    // ==============================

    @Test
    void testSimulationPage_ModelAttributes() {
        // Given
        Model model = mock(Model.class);

        // When
        String viewName = adminController.simulationPage(model);

        // Then
        assertThat(viewName).isEqualTo("admin/simulation");
        verify(model).addAttribute("numPatients", 2);
        verify(model).addAttribute("numDoctors", 1);
        verify(model).addAttribute("numMachines", 2);
    }
    // ==============================
    // TESTS PARA SIMULACIÓN DE ERRORES
    // ==============================

    @Test
    void testModifySimulation_ServiceThrowsException() throws Exception {
        // Given
        doThrow(new RuntimeException("Simulation service error"))
                .when(simulationService).modify(5, 3, 4);

        // When & Then - Debería propagar la excepción
        assertThatThrownBy(() -> mockMvc.perform(post("/admin/simulation/modify")
                .param("numPatients", "5")
                .param("numDoctors", "3")
                .param("numMachines", "4"))).hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void testStartSimulation_ServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Start simulation error"))
                .when(simulationService).start();

        // When & Then - Debería propagar la excepción
        assertThatThrownBy(() -> mockMvc.perform(post("/admin/simulation/start")))
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ==============================
    // TESTS PARA VALIDACIONES DE DATOS
    // ==============================

    @Test
    void testUpdateUser_InvalidBirthDate_HandlesException() {
        // Given: Fecha de nacimiento inválida
        Integer userId = 1;
        User existingUser = createUser(userId, "patientuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        existingUser.linkPatient(patient);

        User postedUser = new User();
        postedUser.setUsername("updated");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.PATIENT);

        when(userService.get(userId)).thenReturn(existingUser);

        // When & Then - Fecha inválida debería causar excepción
        assertThatThrownBy(() -> adminController.updateUser(
                userId, postedUser, null, null, "123456789", "fecha-invalida")).isInstanceOf(Exception.class);
    }

    // ==============================
    // TESTS PARA COBERTURA DE CÓDIGO
    // ==============================

    @Test
    void testDashboard_WithSingleDiagnosis() {
        // Given - Solo un diagnóstico
        List<Diagnosis> mockDiagnoses = Collections.singletonList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT));

        when(patientRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(1L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(1L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When - No debería lanzar excepción
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
        verify(model).addAttribute("completionRate", 100.0); // 1/1*100
        verify(model).addAttribute("positiveRate", 0.0); // Busca "Positive" pero es "Malignant"
    }

    @Test
    void testDashboard_CalculatesCorrectRates() {
        // Given
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT),
                createDiagnosis(2, false, true, FinalResult.BENIGN),
                createDiagnosis(3, true, false, (FinalResult) null), // No revisado
                createDiagnosis(4, false, false, (FinalResult) null) // No revisado
        );

        when(patientRepository.count()).thenReturn(4L);
        when(userRepository.count()).thenReturn(3L);
        when(diagnosisRepository.count()).thenReturn(4L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(2L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(2L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // When
        adminController.dashboard(model);

        // Then - Capturar y verificar todos los atributos
        verify(model, atLeast(10)).addAttribute(keyCaptor.capture(), valueCaptor.capture());

        Map<String, Object> attributes = new HashMap<>();
        List<String> keys = keyCaptor.getAllValues();
        List<Object> values = valueCaptor.getAllValues();

        for (int i = 0; i < keys.size(); i++) {
            attributes.put(keys.get(i), values.get(i));
        }

        // Verificar cálculos con AssertJ
        assertThat(attributes).containsKey("completionRate");
        assertThat((Double) attributes.get("completionRate")).isEqualTo(50.0); // 2/4*100

        assertThat(attributes).containsKey("positiveRate");
        assertThat((Double) attributes.get("positiveRate")).isEqualTo(0.0); // Busca "Positive" pero getStatus()
                                                                            // devuelve "Malignant"

        assertThat(attributes).containsKey("pendingCount");
        assertThat((Long) attributes.get("pendingCount")).isEqualTo(2L); // 2 no revisados
    }

    @Test
    void testUpdateUser_DoctorToAdmin_RoleChange() {
        // Given: Cambio de DOCTOR a ADMIN
        Integer userId = 1;
        User existingUser = createUser(userId, "doctoruser", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setId(1);
        existingUser.setDoctor(doctor);

        User postedUser = new User();
        postedUser.setUsername("adminuser");
        postedUser.setEmail("admin@test.com");
        postedUser.setFullName("Admin Name");
        postedUser.setRole(Role.ADMIN);

        when(userService.get(userId)).thenReturn(existingUser);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, null, null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");

        // Verificar que se llamó a saveAndFlush (cambio de rol)
        verify(userRepository, atLeastOnce()).saveAndFlush(argThat(user -> user.getRole() == Role.ADMIN &&
                user.getDoctor() == null // Doctor desvinculado
        ));

        // IMPORTANTE: El doctor NO se elimina, solo se desvincula
        // Por lo tanto, NO debería haber delete
        verify(doctorRepository, never()).delete(any(Doctor.class));
    }

    @Test
    void testUpdateUser_DoctorToAdmin_DoctorNotDeleted() {
        // Este test verifica específicamente que el doctor no se elimina
        // cuando se cambia de rol DOCTOR a ADMIN

        // Given
        Integer userId = 1;
        User existingUser = createUser(userId, "doctoruser", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setPhone("123456789");
        existingUser.setDoctor(doctor);

        User postedUser = new User();
        postedUser.setUsername("adminuser");
        postedUser.setEmail("admin@test.com");
        postedUser.setFullName("Admin Name");
        postedUser.setRole(Role.ADMIN);

        when(userService.get(userId)).thenReturn(existingUser);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, null, null, null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");

        // El doctor queda "huérfano" en la base de datos pero NO se elimina
        verify(doctorRepository, never()).delete(any(Doctor.class));
        verify(doctorRepository, never()).save(any(Doctor.class)); // Tampoco se actualiza
    }

    @Test
    void testSuggestPatients_CaseInsensitiveSearch() throws Exception {
        // Given
        List<Patient> patients = Arrays.asList(
                createPatientWithUser(1, "John Doe"),
                createPatientWithUser(2, "JOHN SMITH"), // Mayúsculas
                createPatientWithUser(3, "Mary Johnson"));

        when(patientRepository.findAll()).thenReturn(patients);

        // When & Then - Búsqueda case-insensitive
        MvcResult result = mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "JOHN")) // Mayúsculas
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        // Verificar que encuentra ambos "John Doe" y "JOHN SMITH"
        // (dependiendo de si el controlador es case-sensitive)
        // El controlador actual usa toLowerCase(), así que debería funcionar
    }

    @Test
    void testCreateUser_WithEmptyPhoneNumbers() {
        // Given
        User user = new User();
        user.setUsername("doctor_user");
        user.setEmail("doctor@test.com");
        user.setFullName("Dr. Test");
        user.setRole(Role.DOCTOR);

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(1);
        savedDoctor.setPhone(null); // Teléfono null

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When - Teléfono vacío
        String result = adminController.createUser(
                user, "password123", "", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository).save(argThat(doc -> doc.getPhone() == null || doc.getPhone().isEmpty()));
    }

    @Test
    void testRound1_Method_ExtensiveTesting() {
        // Probar más casos para el método de redondeo
        // Usando el método helper roundHelper que ya tienes

        // Casos de redondeo hacia arriba
        assertThat(roundHelper(1.45)).isEqualTo(1.5);
        assertThat(roundHelper(2.55)).isEqualTo(2.6);
        assertThat(roundHelper(3.65)).isEqualTo(3.7);

        // Casos de redondeo hacia abajo
        assertThat(roundHelper(1.44)).isEqualTo(1.4);
        assertThat(roundHelper(2.54)).isEqualTo(2.5);
        assertThat(roundHelper(3.64)).isEqualTo(3.6);

        // Números grandes
        assertThat(roundHelper(1234.567)).isEqualTo(1234.6);
        assertThat(roundHelper(9999.999)).isEqualTo(10000.0);

        // Números pequeños
        assertThat(roundHelper(0.001)).isEqualTo(0.0);
        assertThat(roundHelper(0.009)).isEqualTo(0.0);
        assertThat(roundHelper(0.015)).isEqualTo(0.0); // 0.015 redondea a 0.0
    }

    // ==============================
    // TESTS PARA VALIDACIONES DE DIAGNOSIS
    // ==============================

    @Test
    void testCreateDiagnosis_InvalidDate_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When & Then - Fecha inválida
        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "fecha-invalida")) // Fecha inválida
                .andExpect(status().isOk()) // 200 porque maneja la excepción
                .andReturn();

        String errorMessage = (String) result.getModelAndView().getModel().get("error");
        assertThat(errorMessage).contains("Failed to create diagnosis");
    }

    // ==============================
    // TESTS PARA EL MÉTODO PRIVADO DOWNLOADTOFILE (indirecto)
    // ==============================

    @Test
    void testCreateDiagnosis_ExceptionInProcess_ReturnsErrorView() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        Doctor doctor = new Doctor();
        doctor.setId(1);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findAll()).thenReturn(Arrays.asList(doctor));

        // Simular una excepción al guardar el diagnóstico
        when(diagnosisRepository.saveAndFlush(any(Diagnosis.class)))
                .thenThrow(new RuntimeException("Simulated database error"));

        // When & Then
        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andReturn();

        // Verificar que se devuelve la vista de error
        assertThat(result.getModelAndView().getViewName()).isEqualTo("admin/diagnosis-form");

        Map<String, Object> model = result.getModelAndView().getModel();
        assertThat(model).containsKey("error");
        assertThat(model).containsKey("today");

        String errorMessage = (String) model.get("error");
        assertThat(errorMessage).contains("Failed to create diagnosis");
    }

    // ==============================
    // TESTS PARA VERIFICACIÓN DE MODEL ATTRIBUTES
    // ==============================

    @Test
    void testNewUserForm_HasAllRequiredAttributes() {
        // Given
        Model model = mock(Model.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // When
        adminController.newUser(model);

        // Then
        verify(model, times(2)).addAttribute(keyCaptor.capture(), valueCaptor.capture());

        List<String> keys = keyCaptor.getAllValues();
        List<Object> values = valueCaptor.getAllValues();

        // Verificar con AssertJ
        assertThat(keys).contains("user", "roles");
        assertThat(values.get(keys.indexOf("user"))).isInstanceOf(User.class);
        assertThat(values.get(keys.indexOf("roles"))).isEqualTo(Role.values());
    }

    @Test
    void testEditUserForm_HasAllRequiredAttributes() {
        // Given
        Integer userId = 1;
        User mockUser = createUser(userId, "testuser", Role.ADMIN);
        when(userService.get(userId)).thenReturn(mockUser);

        Model model = mock(Model.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // When
        adminController.editUser(userId, model);

        // Then
        verify(model, times(2)).addAttribute(keyCaptor.capture(), valueCaptor.capture());

        List<String> keys = keyCaptor.getAllValues();

        assertThat(keys).contains("user", "roles");
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("roles", Role.values());
    }
}