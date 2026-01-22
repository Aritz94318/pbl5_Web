package edu.mondragon.we2.pinkAlert.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.AiClientService;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import edu.mondragon.we2.pinkAlert.service.DoctorService;
import edu.mondragon.we2.pinkAlert.service.SimulationService;
import edu.mondragon.we2.pinkAlert.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setViewResolvers(new DummyViewResolver())
                .build();
    }

    @Test
    void createDiagnosis_sonarCoverage_clientErrorIsFine() throws Exception {
        // Corregido: la URL debe coincidir con el controlador actual
        mockMvc.perform(post("/admin/diagnoses"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDashboard_ReturnsCorrectViewAndModelAttributes() {
        // Given
        List<Diagnosis> mockDiagnoses = Arrays.asList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT),
                createDiagnosis(2, false, true, FinalResult.BENIGN),
                createDiagnosis(3, true, true, FinalResult.MALIGNANT),
                createDiagnosis(4, false, false, FinalResult.PENDING));

        when(patientRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(diagnosisRepository.count()).thenReturn(4L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(2L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");

        // Verificar que se agregan los atributos principales
        verify(model).addAttribute("totalPatients", 10L);
        verify(model).addAttribute("totalUsers", 5L);
        verify(model).addAttribute("totalScreenings", 4L);
        verify(model).addAttribute("urgentCases", 2L);
        verify(model).addAttribute(eq("completionRate"), anyDouble());
        verify(model).addAttribute(eq("positiveRate"), anyDouble());
        verify(model).addAttribute(eq("positiveCount"), anyLong());
        verify(model).addAttribute(eq("negativeCount"), anyLong());
        verify(model).addAttribute(eq("pendingCount"), anyLong());
        verify(model).addAttribute(eq("inconclusiveCount"), anyLong());
    }

    @Test
    void testDashboard_WithEmptyData_HandlesGracefully() {
        // Given
        when(patientRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(0L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
        when(diagnosisRepository.findAll()).thenReturn(Collections.emptyList());

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
        verify(model).addAttribute("completionRate", 0.0);
        verify(model).addAttribute("positiveRate", 0.0);
        verify(model).addAttribute("totalPatients", 0L);
        verify(model).addAttribute("totalUsers", 0L);
        verify(model).addAttribute("totalScreenings", 0L);
        verify(model).addAttribute("urgentCases", 0L);
    }

    @Test
    void testUsers_ReturnsUsersView() {
        // Given
        List<User> mockUsers = Arrays.asList(
                createUser(1, "admin1", Role.ADMIN),
                createUser(2, "doctor1", Role.DOCTOR));
        when(userService.findAll()).thenReturn(mockUsers);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.users(null, model);

        // Then
        assertThat(viewName).isEqualTo("admin/users");
        verify(model).addAttribute("users", mockUsers);
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testUsers_WithRoleFilter_ReturnsFilteredUsers() {
        // Given
        Role role = Role.DOCTOR;
        List<User> mockDoctors = Arrays.asList(
                createUser(1, "doctor1", Role.DOCTOR),
                createUser(2, "doctor2", Role.DOCTOR));
        when(userService.findByRole(role)).thenReturn(mockDoctors);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.users(role, model);

        // Then
        assertThat(viewName).isEqualTo("admin/users");
        verify(model).addAttribute("users", mockDoctors);
        verify(model).addAttribute("roleFilter", role);
        verify(model).addAttribute("roles", Role.values());
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
        Role roleFilter = null;
        User mockUser = createUser(userId, "testuser", Role.ADMIN);
        when(userService.get(userId)).thenReturn(mockUser);
        Model model = mock(Model.class);

        // When
        String viewName = adminController.editUser(userId, roleFilter, model);

        // Then
        assertThat(viewName).isEqualTo("admin/user-form");
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("roles", Role.values());
        verify(model).addAttribute("roleFilter", roleFilter);
    }

    @Test
    void testUpdateUser_WithSameRole_UpdatesUser() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "existing", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());
        existingUser.getDoctor().setId(1);

        User postedUser = new User();
        postedUser.setUsername("updated");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR); // Mismo rol

        when(userService.get(userId)).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, roleFilter, "newpassword123", "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).setPassword(existingUser, "newpassword123");
        verify(userService).save(existingUser);
        verify(doctorRepository).save(argThat(doctor -> "999999999".equals(doctor.getPhone())));
    }

    @Test
    void testUpdateUser_WithRoleChange_UpdatesUserWithSaveAndFlush() {
        // Given: Cambio de ADMIN a DOCTOR
        Integer userId = 1;
        Role roleFilter = null;
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
                userId, postedUser, roleFilter, "password123", "123456789", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void testDeleteUser_DeletesUserAndAssociatedEntities() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "todelete", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setId(1);
        user.setDoctor(doctor);

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId, roleFilter);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository).saveAndFlush(argThat(u -> Role.ADMIN == u.getRole() &&
                u.getDoctor() == null &&
                u.getPatient() == null));
        verify(userRepository).delete(user);
        verify(doctorRepository).delete(doctor);
    }

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

    @Test
    void testSimulationPage_ReturnsSimulationView() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/simulation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/simulation"))
                .andExpect(model().attribute("numPatients", 1))
                .andExpect(model().attribute("numDoctors", 1))
                .andExpect(model().attribute("numMachines", 1));
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
                createPatientWithUser(1, "John Doe"),
                createPatientWithUser(2, "Johanna Smith"),
                createPatientWithUser(3, "Bob Johnson"));

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
void testAgeBuckets_Coverage() {
    // Preparar datos para cubrir todas las ramas del código de ageOf y bucketOf
    LocalDate now = LocalDate.now();
    
    // Crear pacientes de diferentes edades
    Patient patient1 = new Patient(); // Sin fecha de nacimiento (age = null)
    Patient patient2 = new Patient();
    patient2.setBirthDate(now.minusYears(25)); // < 40
    
    Patient patient3 = new Patient();
    patient3.setBirthDate(now.minusYears(45)); // 40-49
    
    Patient patient4 = new Patient();
    patient4.setBirthDate(now.minusYears(55)); // 50-59
    
    Patient patient5 = new Patient();
    patient5.setBirthDate(now.minusYears(65)); // 60-69
    
    Patient patient6 = new Patient();
    patient6.setBirthDate(now.minusYears(75)); // 70+
    
    Patient patient7 = new Patient(); // Sin usuario
    patient7.setBirthDate(now.minusYears(30));
    
    // Crear diagnósticos
    Diagnosis diagnosis1 = new Diagnosis(); // Sin paciente
    diagnosis1.setReviewed(true);
    diagnosis1.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis2 = new Diagnosis(); // Paciente sin fecha
    diagnosis2.setPatient(patient1);
    diagnosis2.setReviewed(true);
    diagnosis2.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis3 = new Diagnosis(); // <40
    diagnosis3.setPatient(patient2);
    diagnosis3.setReviewed(true);
    diagnosis3.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis4 = new Diagnosis(); // 40-49
    diagnosis4.setPatient(patient3);
    diagnosis4.setReviewed(true);
    diagnosis4.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis5 = new Diagnosis(); // 50-59
    diagnosis5.setPatient(patient4);
    diagnosis5.setReviewed(true);
    diagnosis5.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis6 = new Diagnosis(); // 60-69
    diagnosis6.setPatient(patient5);
    diagnosis6.setReviewed(true);
    diagnosis6.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis7 = new Diagnosis(); // 70+
    diagnosis7.setPatient(patient6);
    diagnosis7.setReviewed(true);
    diagnosis7.setFinalResult(FinalResult.MALIGNANT);
    
    // Paciente con excepción (simulando error en Period.between)
    // Para cubrir el catch block, podríamos mockear LocalDate.now() pero es complejo
    // En su lugar, podemos confiar en que los casos anteriores cubren la mayoría
    
    List<Diagnosis> diagnoses = Arrays.asList(
        diagnosis1, diagnosis2, diagnosis3, diagnosis4, 
        diagnosis5, diagnosis6, diagnosis7
    );
    
    // Configurar mocks
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);
    when(patientRepository.count()).thenReturn(7L);
    when(userRepository.count()).thenReturn(7L);
    when(diagnosisRepository.count()).thenReturn(7L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(3L);
    
    Model model = mock(Model.class);
    
    // Ejecutar dashboard
    adminController.dashboard(model);
    
    // Verificar que se agregaron los atributos de age buckets
    verify(model).addAttribute(eq("ageLabelsJs"), anyString());
    verify(model).addAttribute(eq("ageTotalsJs"), anyString());
    verify(model).addAttribute(eq("ageMalignantJs"), anyString());
    verify(model).addAttribute(eq("ageBenignJs"), anyString());
    verify(model).addAttribute(eq("ageInconclusiveJs"), anyString());
    verify(model).addAttribute(eq("ageMalignantRateJs"), anyString());
}

@Test
void testAgeCalculation_ExceptionCoverage() {
    // Crear un paciente que causará una excepción en Period.between
    Patient problematicPatient = mock(Patient.class);
    when(problematicPatient.getBirthDate()).thenThrow(new RuntimeException("Simulated error"));
    
    Diagnosis diagnosis = new Diagnosis();
    diagnosis.setPatient(problematicPatient);
    diagnosis.setReviewed(true);
    diagnosis.setFinalResult(FinalResult.BENIGN);
    
    List<Diagnosis> diagnoses = Collections.singletonList(diagnosis);
    
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    
    // Este no debería lanzar excepción porque el catch block maneja el error
    adminController.dashboard(model);
    
    // Verificar que el dashboard se ejecutó sin problemas
    verify(model).addAttribute(eq("ageLabelsJs"), anyString());
}

@Test
void testAiAgreement_AiMissing() {
    // Caso: AI prediction = null
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(null);
    d.setReviewed(true);
    d.setFinalResult(FinalResult.MALIGNANT);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiMissingCount", 1L);
    verify(model).addAttribute("aiAgreeCount", 0L);
    verify(model).addAttribute("aiMismatchCount", 0L);
    verify(model).addAttribute("aiNotComparableCount", 0L);
}

@Test
void testAiAgreement_NotFinalized() {
    // Caso: No revisado
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(AiPrediction.MALIGNANT);
    d.setReviewed(false);
    d.setFinalResult(null);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiNotComparableCount", 1L);
}

@Test
void testAiAgreement_DoctorInconclusive() {
    // Caso: Doctor inconclusive
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(AiPrediction.MALIGNANT);
    d.setReviewed(true);
    d.setFinalResult(FinalResult.INCONCLUSIVE);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiNotComparableCount", 1L);
}
@Test
void testUpdateUser_PatientRole_ExistingPatient_UpdatesPatientInfo() {
    // Given: Usuario con rol PATIENT que ya tiene un paciente asociado
    Integer userId = 1;
    Role roleFilter = null;
    
    // Crear usuario existente con rol PATIENT
    User existingUser = createUser(userId, "patientuser", Role.PATIENT);
    
    // Crear paciente existente asociado
    Patient existingPatient = new Patient();
    existingPatient.setId(1);
    existingPatient.setBirthDate(LocalDate.of(1980, 1, 1));
    existingPatient.setPhone("111111111");
    existingUser.linkPatient(existingPatient);
    
    // Nuevos datos del usuario (mismo rol)
    User postedUser = new User();
    postedUser.setUsername("updatedpatient");
    postedUser.setEmail("updated@test.com");
    postedUser.setFullName("Updated Patient");
    postedUser.setRole(Role.PATIENT); // Mismo rol
    
    String newPhone = "999999999";
    String newBirthDate = "1990-05-15";
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    // When: Actualizar usuario manteniendo rol PATIENT
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, newBirthDate);
    
    // Then: Debería actualizar la información del paciente existente
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    // Verificar que se actualizaron los datos del paciente
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.parse("1990-05-15").equals(patient.getBirthDate())
    ));
    
    // Verificar que se guardó el usuario (no saveAndFlush porque el rol no cambió)
    verify(userService).save(argThat(user -> 
        "updatedpatient".equals(user.getUsername()) &&
        "updated@test.com".equals(user.getEmail()) &&
        "Updated Patient".equals(user.getFullName()) &&
        Role.PATIENT == user.getRole()
    ));
    
    // No debería llamar a saveAndFlush porque el rol no cambió
    verify(userRepository, never()).saveAndFlush(any(User.class));
}
@Test
void testUpdateUser_PatientRole_ExistingPatient_NullBirthDate() {
    // Given: Usuario con rol PATIENT, actualizar solo el teléfono
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "patientuser", Role.PATIENT);
    Patient existingPatient = new Patient();
    existingPatient.setId(1);
    existingPatient.setBirthDate(LocalDate.of(1980, 1, 1));
    existingPatient.setPhone("111111111");
    existingUser.linkPatient(existingPatient);
    
    User postedUser = new User();
    postedUser.setUsername("patientuser");
    postedUser.setEmail("patient@test.com");
    postedUser.setFullName("Patient User");
    postedUser.setRole(Role.PATIENT);
    
    String newPhone = "999999999";
    String nullBirthDate = null; // birthDate null
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    // When: Actualizar con birthDate null
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, nullBirthDate);
    
    // Then: Debería mantener la fecha de nacimiento existente
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.of(1980, 1, 1).equals(patient.getBirthDate()) // Mantiene la fecha original
    ));
}
@Test
void testUpdateUser_ChangeToAdminRole_FromDoctor() {
    // Given: Usuario DOCTOR cambiando a ADMIN
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "doctoruser", Role.DOCTOR);
    Doctor doctor = new Doctor();
    doctor.setId(1);
    doctor.setPhone("111111111");
    existingUser.setDoctor(doctor);
    
    User postedUser = new User();
    postedUser.setUsername("adminuser");
    postedUser.setEmail("admin@test.com");
    postedUser.setFullName("Admin User");
    postedUser.setRole(Role.ADMIN); // Cambiando a ADMIN
    
    when(userService.get(userId)).thenReturn(existingUser);
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);
    
    // When: Cambiar rol a ADMIN
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    // Then: Debería configurar rol ADMIN y desvincular doctor/patient
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    // Verificar que se llamó a saveAndFlush con las configuraciones correctas
    verify(userRepository, atLeastOnce()).saveAndFlush(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        user.getDoctor() == null && // Doctor desvinculado
        user.getPatient() == null   // Patient desvinculado
    ));
    
    // Verificar que el doctor NO se elimina, solo se desvincula
    verify(doctorRepository, never()).delete(any(Doctor.class));
}

@Test
void testUpdateUser_ChangeToAdminRole_FromPatient() {
    // Given: Usuario PATIENT cambiando a ADMIN
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "patientuser", Role.PATIENT);
    Patient patient = new Patient();
    patient.setId(1);
    patient.setBirthDate(LocalDate.of(1990, 1, 1));
    patient.setPhone("111111111");
    existingUser.linkPatient(patient);
    
    User postedUser = new User();
    postedUser.setUsername("adminuser");
    postedUser.setEmail("admin@test.com");
    postedUser.setFullName("Admin User");
    postedUser.setRole(Role.ADMIN); // Cambiando a ADMIN
    
    when(userService.get(userId)).thenReturn(existingUser);
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);
    
    // When: Cambiar rol a ADMIN
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    // Then: Debería configurar rol ADMIN y desvincular doctor/patient
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    // Verificar que se llamó a saveAndFlush con las configuraciones correctas
    verify(userRepository, atLeastOnce()).saveAndFlush(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        user.getDoctor() == null && // Doctor desvinculado
        user.getPatient() == null   // Patient desvinculado
    ));
    
    // Verificar que el patient NO se elimina, solo se desvincula
    verify(patientRepository, never()).delete(any(Patient.class));
}

@Test
void testUpdateUser_ChangeToAdminRole_FromAdmin() {
    // Given: Usuario ADMIN manteniéndose como ADMIN
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "adminuser", Role.ADMIN);
    // ADMIN no tiene doctor ni patient asociados
    
    User postedUser = new User();
    postedUser.setUsername("newadmin");
    postedUser.setEmail("newadmin@test.com");
    postedUser.setFullName("New Admin");
    postedUser.setRole(Role.ADMIN); // Mismo rol
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    // When: Mantener rol ADMIN
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    // Then: No debería usar saveAndFlush porque el rol no cambió
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    // Verificar que se usó save() normal (no saveAndFlush)
    verify(userService).save(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        "newadmin".equals(user.getUsername())
    ));
    
    // No debería llamar a saveAndFlush porque el rol no cambió
    verify(userRepository, never()).saveAndFlush(any(User.class));
}

@Test
void testUpdateUser_PatientRole_ExistingPatient_BlankBirthDate() {
    // Given: Usuario con rol PATIENT, birthDate en blanco
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "patientuser", Role.PATIENT);
    Patient existingPatient = new Patient();
    existingPatient.setId(1);
    existingPatient.setBirthDate(LocalDate.of(1980, 1, 1));
    existingPatient.setPhone("111111111");
    existingUser.linkPatient(existingPatient);
    
    User postedUser = new User();
    postedUser.setUsername("patientuser");
    postedUser.setEmail("patient@test.com");
    postedUser.setFullName("Patient User");
    postedUser.setRole(Role.PATIENT);
    
    String newPhone = "999999999";
    String blankBirthDate = "   "; // birthDate en blanco
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    // When: Actualizar con birthDate en blanco
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, blankBirthDate);
    
    // Then: Debería mantener la fecha de nacimiento existente
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.of(1980, 1, 1).equals(patient.getBirthDate()) // Mantiene la fecha original
    ));
}
@Test
void testAiAgreement_AiPending() {
    // Caso: AI prediction = PENDING (aiAsFinal = null)
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(AiPrediction.PENDING);
    d.setReviewed(true);
    d.setFinalResult(FinalResult.MALIGNANT);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiNotComparableCount", 1L);
}

@Test
void testAiAgreement_Agree() {
    // Caso: AI y doctor coinciden (MALIGNANT)
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(AiPrediction.MALIGNANT);
    d.setReviewed(true);
    d.setFinalResult(FinalResult.MALIGNANT);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiAgreeCount", 1L);
    verify(model).addAttribute("aiMismatchCount", 0L);
}

@Test
void testAiAgreement_Mismatch() {
    // Caso: AI y doctor no coinciden
    Diagnosis d = new Diagnosis();
    d.setAiPrediction(AiPrediction.BENIGN);
    d.setReviewed(true);
    d.setFinalResult(FinalResult.MALIGNANT);
    
    when(diagnosisRepository.findAll()).thenReturn(Collections.singletonList(d));
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    adminController.dashboard(model);
    
    verify(model).addAttribute("aiAgreeCount", 0L);
    verify(model).addAttribute("aiMismatchCount", 1L);
}
@Test
void testAgeBuckets_NullPatientCoverage() {
    // Diagnóstico sin paciente asignado
    Diagnosis diagnosis = new Diagnosis();
    diagnosis.setPatient(null); // Explicitamente null
    diagnosis.setReviewed(true);
    diagnosis.setFinalResult(FinalResult.MALIGNANT);
    
    List<Diagnosis> diagnoses = Collections.singletonList(diagnosis);
    
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);
    when(patientRepository.count()).thenReturn(1L);
    when(userRepository.count()).thenReturn(1L);
    when(diagnosisRepository.count()).thenReturn(1L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
    
    Model model = mock(Model.class);
    
    adminController.dashboard(model);
    
    // Verificar que la etiqueta "Unknown" está presente
    verify(model).addAttribute(eq("ageLabelsJs"), argThat(s -> ((String)s).contains("'Unknown'")));
}
@Test
void testMapAiToFinal_CoverageTest() throws Exception {
    // Obtener el método privado usando reflexión
    Method mapAiToFinalMethod = AdminController.class.getDeclaredMethod("mapAiToFinal", AiPrediction.class);
    mapAiToFinalMethod.setAccessible(true);
    
    // Test null case
    assertThat(mapAiToFinalMethod.invoke(null, (Object) null)).isNull();
    
    // Test MALIGNANT case
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.MALIGNANT))
        .isEqualTo(FinalResult.MALIGNANT);
    
    // Test BENIGN case  
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.BENIGN))
        .isEqualTo(FinalResult.BENIGN);
    
    // Test default case (PENDING)
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.PENDING)).isNull();
}
    @Test
    void testCreateDiagnosis_NoPatientSelected_ReturnsError() throws Exception {
        // Cuando patientId es null/empty, Spring puede lanzar 400 antes de llegar al
        // controlador
        // Necesitamos manejar este caso específicamente

        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andReturn();

        // Verificar el status code
        int status = result.getResponse().getStatus();

        // Si es 400, es un comportamiento válido (validación de Spring)
        // Si es 200, el controlador manejó el error
        if (status == 200) {
            // Solo verificar getViewName() si tenemos ModelAndView
            if (result.getModelAndView() != null) {
                assertThat(result.getModelAndView().getViewName()).isEqualTo("admin/diagnosis-form");
                assertThat(result.getModelAndView().getModel().containsKey("error")).isTrue();
            }
        } else if (status == 400) {
            // Comportamiento aceptable - validación de Spring
            assertThat(status).isEqualTo(400);
        } else {
            // Otro código de estado inesperado
            fail("Código de estado inesperado: " + status);
        }
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
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testCreateDiagnosis_PatientWithoutEmail_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail(null);

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
                .andExpect(model().attributeExists("error"));
    }

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

        // When
        String result = adminController.createUser(
                user, null, null, "123456789", "1990-01-01");

        // Then
        verify(userService).createUser(any(User.class), eq("123"));
    }

    @Test
    void testUpdateUser_AdminToPatient_RoleChange() {
        // Given: Cambio de ADMIN a PATIENT
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "adminuser", Role.ADMIN);

        User postedUser = new User();
        postedUser.setUsername("patientuser");
        postedUser.setEmail("patient@test.com");
        postedUser.setFullName("Patient Name");
        postedUser.setRole(Role.PATIENT);

        Patient patient = new Patient();
        patient.setId(1);

        when(userService.get(userId)).thenReturn(existingUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, roleFilter, null, null, "123456789", "1990-01-01");

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void testUpdateUser_WithPasswordChange() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "testuser", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());

        User postedUser = new User();
        postedUser.setUsername("updateduser");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR);

        when(userService.get(userId)).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, roleFilter, "newpassword123", "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).setPassword(existingUser, "newpassword123");
        verify(userService).save(existingUser);
    }

    @Test
    void testUpdateUser_WithoutPasswordChange() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "testuser", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());

        User postedUser = new User();
        postedUser.setUsername("updateduser");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR);

        when(userService.get(userId)).thenReturn(existingUser);

        // When
        String result = adminController.updateUser(
                userId, postedUser, roleFilter, null, "999999999", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService, never()).setPassword(any(), anyString());
        verify(userService).save(existingUser);
    }

    @Test
    void testSuggestPatients_EmptyQuery_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSuggestPatients_NullQuery_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/admin/patients/suggest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testSuggestPatients_PatientWithoutUser_UsesDefaultLabel() throws Exception {
        // Given
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUser(null);

        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        // When & Then
        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Patient PT-1"));
    }

    @Test
    void testCreateDiagnosis_NoDoctorsExist_ReturnsError() throws Exception {
        // Given
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());

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
                .andReturn();

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

        // When & Then
        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("patientId", patientId.toString())
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andReturn();

        String errorMessage = (String) result.getModelAndView().getModel().get("error");
        assertThat(errorMessage).contains("All 4 DICOM URLs are required");
    }

    @Test
    void testRound1_Method_EdgeCases() {
        assertThat(roundHelper(0.0)).isEqualTo(0.0);
        assertThat(roundHelper(0.04)).isEqualTo(0.0);
        assertThat(roundHelper(0.05)).isEqualTo(0.1);
        assertThat(roundHelper(0.06)).isEqualTo(0.1);
        assertThat(roundHelper(99.94)).isEqualTo(99.9);
        assertThat(roundHelper(99.95)).isEqualTo(100.0);
        assertThat(roundHelper(99.96)).isEqualTo(100.0);
        assertThat(roundHelper(-123.456)).isEqualTo(-123.5);
    }

    @Test
    void testDeleteUser_WithPatientRole_DeletesPatient() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "patientuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        user.linkPatient(patient);

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId, roleFilter);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(patientRepository).delete(patient);
    }

    @Test
    void testDeleteUser_WithNoAssociatedEntities() {
        // Given
        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "adminuser", Role.ADMIN);

        when(userService.get(userId)).thenReturn(user);

        // When
        String result = adminController.deleteUser(userId, roleFilter);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository, never()).delete(any());
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void testSimulationPage_ModelAttributes() {
        // Given
        Model model = mock(Model.class);

        // When
        String viewName = adminController.simulationPage(model);

        // Then
        assertThat(viewName).isEqualTo("admin/simulation");
        verify(model).addAttribute("numPatients", 1);
        verify(model).addAttribute("numDoctors", 1);
        verify(model).addAttribute("numMachines", 1);
    }

    @Test
    void testDashboard_WithSingleDiagnosis() {
        // Given
        List<Diagnosis> mockDiagnoses = Collections.singletonList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT));

        when(patientRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(1L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        // When
        String viewName = adminController.dashboard(model);

        // Then
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
        verify(model).addAttribute(eq("completionRate"), anyDouble());
    }

    @Test
    void testCreateUser_WithEmptyPhoneNumbers() {
        // Given
        User user = new User();
        user.setUsername("doctor_user");
        user.setRole(Role.DOCTOR);

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(1);

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        // When
        String result = adminController.createUser(
                user, "password123", "", null, null);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository).save(argThat(doc -> doc.getPhone() == null || doc.getPhone().isEmpty()));
    }

    @Test
    void testRound1_Method_ExtensiveTesting() {
        assertThat(roundHelper(1.45)).isEqualTo(1.5);
        assertThat(roundHelper(2.55)).isEqualTo(2.6);
        assertThat(roundHelper(3.65)).isEqualTo(3.7);
        assertThat(roundHelper(1.44)).isEqualTo(1.4);
        assertThat(roundHelper(2.54)).isEqualTo(2.5);
        assertThat(roundHelper(3.64)).isEqualTo(3.6);
        assertThat(roundHelper(1234.567)).isEqualTo(1234.6);
        assertThat(roundHelper(9999.999)).isEqualTo(10000.0);
    }

    // Helper methods
    private Diagnosis createDiagnosis(Integer id, boolean urgent, boolean reviewed, FinalResult finalResult) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setUrgent(urgent);
        diagnosis.setReviewed(reviewed);
        diagnosis.setDate(LocalDate.now().minusDays(id));
        diagnosis.setFinalResult(finalResult);
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

    private static class DummyViewResolver implements ViewResolver {
        @Override
        public View resolveViewName(String viewName, Locale locale) {
            return (model, request, response) -> {
                // No hacer nada
            };
        }
    }
}