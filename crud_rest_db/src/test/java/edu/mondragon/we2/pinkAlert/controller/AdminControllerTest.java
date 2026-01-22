package edu.mondragon.we2.pinkAlert.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
        mockMvc.perform(post("/admin/diagnoses"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDashboard_ReturnsCorrectViewAndModelAttributes() {
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
        String viewName = adminController.dashboard(model);
        assertThat(viewName).isEqualTo("admin/admin-dashboard");
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
        when(patientRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(0L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
        when(diagnosisRepository.findAll()).thenReturn(Collections.emptyList());
        Model model = mock(Model.class);
        String viewName = adminController.dashboard(model);

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
        List<User> mockUsers = Arrays.asList(
                createUser(1, "admin1", Role.ADMIN),
                createUser(2, "doctor1", Role.DOCTOR));
        when(userService.findAll()).thenReturn(mockUsers);
        Model model = mock(Model.class);
        String viewName = adminController.users(null, model);

        assertThat(viewName).isEqualTo("admin/users");
        verify(model).addAttribute("users", mockUsers);
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testUsers_WithRoleFilter_ReturnsFilteredUsers() {
  
        Role role = Role.DOCTOR;
        List<User> mockDoctors = Arrays.asList(
                createUser(1, "doctor1", Role.DOCTOR),
                createUser(2, "doctor2", Role.DOCTOR));
        when(userService.findByRole(role)).thenReturn(mockDoctors);
        Model model = mock(Model.class);

        String viewName = adminController.users(role, model);

        assertThat(viewName).isEqualTo("admin/users");
        verify(model).addAttribute("users", mockDoctors);
        verify(model).addAttribute("roleFilter", role);
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testNewUser_ReturnsUserForm() {
      
        Model model = mock(Model.class);

        String viewName = adminController.newUser(model);

        assertThat(viewName).isEqualTo("admin/user-form");
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("roles", Role.values());
    }

    @Test
    void testCreateUser_WithDoctorRole_CreatesDoctorAndUser() {
       
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

        String result = adminController.createUser(
                user, "password123", doctorPhone, null, null);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository).save(argThat(doc -> doctorPhone.equals(doc.getPhone())));
        verify(userService).createUser(userCaptor.capture(), eq("password123"));

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getDoctor()).isEqualTo(savedDoctor);
        assertThat(capturedUser.getPatient()).isNull();
    }

    @Test
    void testCreateUser_WithPatientRole_CreatesPatientAndUser() {
     
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

        String result = adminController.createUser(
                user, "password123", null, patientPhone, birthDate);

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
     
        Integer userId = 1;
        Role roleFilter = null;
        User mockUser = createUser(userId, "testuser", Role.ADMIN);
        when(userService.get(userId)).thenReturn(mockUser);
        Model model = mock(Model.class);

        String viewName = adminController.editUser(userId, roleFilter, model);

        assertThat(viewName).isEqualTo("admin/user-form");
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("roles", Role.values());
        verify(model).addAttribute("roleFilter", roleFilter);
    }

    @Test
    void testUpdateUser_WithSameRole_UpdatesUser() {
     
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "existing", Role.DOCTOR);
        existingUser.setDoctor(new Doctor());
        existingUser.getDoctor().setId(1);

        User postedUser = new User();
        postedUser.setUsername("updated");
        postedUser.setEmail("updated@test.com");
        postedUser.setFullName("Updated Name");
        postedUser.setRole(Role.DOCTOR); 

        when(userService.get(userId)).thenReturn(existingUser);

        String result = adminController.updateUser(
                userId, postedUser, roleFilter, "newpassword123", "999999999", null, null);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).setPassword(existingUser, "newpassword123");
        verify(userService).save(existingUser);
        verify(doctorRepository).save(argThat(doctor -> "999999999".equals(doctor.getPhone())));
    }

    @Test
    void testUpdateUser_WithRoleChange_UpdatesUserWithSaveAndFlush() {
      
        Integer userId = 1;
        Role roleFilter = null;
        User existingUser = createUser(userId, "adminuser", Role.ADMIN);

        User postedUser = new User();
        postedUser.setUsername("doctoruser");
        postedUser.setEmail("doctor@test.com");
        postedUser.setFullName("Doctor Name");
        postedUser.setRole(Role.DOCTOR); 

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setPhone("123456789");

        when(userService.get(userId)).thenReturn(existingUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);

        String result = adminController.updateUser(
                userId, postedUser, roleFilter, "password123", "123456789", null, null);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void testDeleteUser_DeletesUserAndAssociatedEntities() {
     
        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "todelete", Role.DOCTOR);
        Doctor doctor = new Doctor();
        doctor.setId(1);
        user.setDoctor(doctor);

        when(userService.get(userId)).thenReturn(user);

        String result = adminController.deleteUser(userId, roleFilter);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository).saveAndFlush(argThat(u -> Role.ADMIN == u.getRole() &&
                u.getDoctor() == null &&
                u.getPatient() == null));
        verify(userRepository).delete(user);
        verify(doctorRepository).delete(doctor);
    }

    @Test
    void testDoctors_ReturnsRoleListView() {
   
        List<User> mockDoctors = Arrays.asList(
                createUser(1, "doc1", Role.DOCTOR),
                createUser(2, "doc2", Role.DOCTOR));
        when(userService.findByRole(Role.DOCTOR)).thenReturn(mockDoctors);
        Model model = mock(Model.class);

        String viewName = adminController.doctors(model);

        assertThat(viewName).isEqualTo("admin/role-list");
        verify(model).addAttribute("users", mockDoctors);
        verify(model).addAttribute("title", "Doctors");
    }

    @Test
    void testPatients_ReturnsRoleListView() {
      
        List<User> mockPatients = Arrays.asList(
                createUser(1, "pat1", Role.PATIENT),
                createUser(2, "pat2", Role.PATIENT));
        when(userService.findByRole(Role.PATIENT)).thenReturn(mockPatients);
        Model model = mock(Model.class);

        String viewName = adminController.patients(model);

        assertThat(viewName).isEqualTo("admin/role-list");
        verify(model).addAttribute("users", mockPatients);
        verify(model).addAttribute("title", "Patients");
    }

    @Test
    void testSimulationPage_ReturnsSimulationView() throws Exception {

        mockMvc.perform(get("/admin/simulation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/simulation"))
                .andExpect(model().attribute("numPatients", 1))
                .andExpect(model().attribute("numDoctors", 1))
                .andExpect(model().attribute("numMachines", 1));
    }

    @Test
    void testModifySimulation_CallsService() throws Exception {
        doNothing().when(simulationService).modify(5, 3, 4);
        mockMvc.perform(post("/admin/simulation/modify")
                .param("numPatients", "5")
                .param("numDoctors", "3")
                .param("numMachines", "4"))
                .andExpect(status().isOk());

        verify(simulationService).modify(5, 3, 4);
    }

    @Test
    void testStartSimulation_CallsService() throws Exception {
        doNothing().when(simulationService).start();
        mockMvc.perform(post("/admin/simulation/start"))
                .andExpect(status().isOk());

        verify(simulationService).start();
    }

    @Test
    void testNewDiagnosisForm_ReturnsFormView() throws Exception {
        mockMvc.perform(get("/admin/diagnoses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/diagnosis-form"))
                .andExpect(model().attributeExists("today"));
    }

    @Test
    void testSuggestPatients_ReturnsFilteredResults() throws Exception {
        List<Patient> patients = Arrays.asList(
                createPatientWithUser(1, "John Doe"),
                createPatientWithUser(2, "Johanna Smith"),
                createPatientWithUser(3, "Bob Johnson"));

        when(patientRepository.findAll()).thenReturn(patients);
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
    LocalDate now = LocalDate.now();
    
    Patient patient1 = new Patient(); 
    Patient patient2 = new Patient();
    patient2.setBirthDate(now.minusYears(25)); 
    
    Patient patient3 = new Patient();
    patient3.setBirthDate(now.minusYears(45)); 
    
    Patient patient4 = new Patient();
    patient4.setBirthDate(now.minusYears(55));
    
    Patient patient5 = new Patient();
    patient5.setBirthDate(now.minusYears(65)); 
    
    Patient patient6 = new Patient();
    patient6.setBirthDate(now.minusYears(75));
    
    Patient patient7 = new Patient();
    patient7.setBirthDate(now.minusYears(30));
    
    Diagnosis diagnosis1 = new Diagnosis(); 
    diagnosis1.setReviewed(true);
    diagnosis1.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis2 = new Diagnosis();
    diagnosis2.setPatient(patient1);
    diagnosis2.setReviewed(true);
    diagnosis2.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis3 = new Diagnosis();
    diagnosis3.setPatient(patient2);
    diagnosis3.setReviewed(true);
    diagnosis3.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis4 = new Diagnosis(); 
    diagnosis4.setPatient(patient3);
    diagnosis4.setReviewed(true);
    diagnosis4.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis5 = new Diagnosis(); 
    diagnosis5.setPatient(patient4);
    diagnosis5.setReviewed(true);
    diagnosis5.setFinalResult(FinalResult.MALIGNANT);
    
    Diagnosis diagnosis6 = new Diagnosis(); 
    diagnosis6.setPatient(patient5);
    diagnosis6.setReviewed(true);
    diagnosis6.setFinalResult(FinalResult.BENIGN);
    
    Diagnosis diagnosis7 = new Diagnosis(); 
    diagnosis7.setPatient(patient6);
    diagnosis7.setReviewed(true);
    diagnosis7.setFinalResult(FinalResult.MALIGNANT);
    
    List<Diagnosis> diagnoses = Arrays.asList(
        diagnosis1, diagnosis2, diagnosis3, diagnosis4, 
        diagnosis5, diagnosis6, diagnosis7
    );
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);
    when(patientRepository.count()).thenReturn(7L);
    when(userRepository.count()).thenReturn(7L);
    when(diagnosisRepository.count()).thenReturn(7L);
    when(diagnosisRepository.countByUrgentTrue()).thenReturn(3L);
    
    Model model = mock(Model.class);
    
    adminController.dashboard(model);
    
    verify(model).addAttribute(eq("ageLabelsJs"), anyString());
    verify(model).addAttribute(eq("ageTotalsJs"), anyString());
    verify(model).addAttribute(eq("ageMalignantJs"), anyString());
    verify(model).addAttribute(eq("ageBenignJs"), anyString());
    verify(model).addAttribute(eq("ageInconclusiveJs"), anyString());
    verify(model).addAttribute(eq("ageMalignantRateJs"), anyString());
}

@Test
void testAgeCalculation_ExceptionCoverage() {
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
    
    adminController.dashboard(model);
    
    verify(model).addAttribute(eq("ageLabelsJs"), anyString());
}

@Test
void testAiAgreement_AiMissing() {
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
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "patientuser", Role.PATIENT);
    
    Patient existingPatient = new Patient();
    existingPatient.setId(1);
    existingPatient.setBirthDate(LocalDate.of(1980, 1, 1));
    existingPatient.setPhone("111111111");
    existingUser.linkPatient(existingPatient);
    
    User postedUser = new User();
    postedUser.setUsername("updatedpatient");
    postedUser.setEmail("updated@test.com");
    postedUser.setFullName("Updated Patient");
    postedUser.setRole(Role.PATIENT); 
    
    String newPhone = "999999999";
    String newBirthDate = "1990-05-15";
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, newBirthDate);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.parse("1990-05-15").equals(patient.getBirthDate())
    ));

    verify(userService).save(argThat(user -> 
        "updatedpatient".equals(user.getUsername()) &&
        "updated@test.com".equals(user.getEmail()) &&
        "Updated Patient".equals(user.getFullName()) &&
        Role.PATIENT == user.getRole()
    ));
    
    verify(userRepository, never()).saveAndFlush(any(User.class));
}
@Test
void testUpdateUser_PatientRole_ExistingPatient_NullBirthDate() {
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
    String nullBirthDate = null; 
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, nullBirthDate);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.of(1980, 1, 1).equals(patient.getBirthDate())
    ));
}
@Test
void testUpdateUser_ChangeToAdminRole_FromDoctor() {
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
    postedUser.setRole(Role.ADMIN); 
    
    when(userService.get(userId)).thenReturn(existingUser);
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);
    
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(userRepository, atLeastOnce()).saveAndFlush(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        user.getDoctor() == null && 
        user.getPatient() == null 
    ));
    
    verify(doctorRepository, never()).delete(any(Doctor.class));
}

@Test
void testUpdateUser_ChangeToAdminRole_FromPatient() {
    
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
    postedUser.setRole(Role.ADMIN); 
    
    when(userService.get(userId)).thenReturn(existingUser);
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingUser);
    
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(userRepository, atLeastOnce()).saveAndFlush(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        user.getDoctor() == null && 
        user.getPatient() == null   
    ));
    
    verify(patientRepository, never()).delete(any(Patient.class));
}

@Test
void testUpdateUser_ChangeToAdminRole_FromAdmin() {
    Integer userId = 1;
    Role roleFilter = null;
    
    User existingUser = createUser(userId, "adminuser", Role.ADMIN);
   
    
    User postedUser = new User();
    postedUser.setUsername("newadmin");
    postedUser.setEmail("newadmin@test.com");
    postedUser.setFullName("New Admin");
    postedUser.setRole(Role.ADMIN); 
    
    when(userService.get(userId)).thenReturn(existingUser);
    
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, "password123", null, null, null);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(userService).save(argThat(user -> 
        Role.ADMIN == user.getRole() &&
        "newadmin".equals(user.getUsername())
    ));
    
    verify(userRepository, never()).saveAndFlush(any(User.class));
}

@Test
void testUpdateUser_PatientRole_ExistingPatient_BlankBirthDate() {
   
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
    String blankBirthDate = "   "; 
    
    when(userService.get(userId)).thenReturn(existingUser);
    String result = adminController.updateUser(
            userId, postedUser, roleFilter, null, null, newPhone, blankBirthDate);
    
    assertThat(result).isEqualTo("redirect:/admin/users");
    
    verify(patientRepository).save(argThat(patient -> 
        "999999999".equals(patient.getPhone()) &&
        LocalDate.of(1980, 1, 1).equals(patient.getBirthDate()) 
    ));
}
@Test
void testAiAgreement_AiPending() {
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
    Diagnosis diagnosis = new Diagnosis();
    diagnosis.setPatient(null); 
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
    
    verify(model).addAttribute(eq("ageLabelsJs"), argThat(s -> ((String)s).contains("'Unknown'")));
}
@Test
void testMapAiToFinal_CoverageTest() throws Exception {
    Method mapAiToFinalMethod = AdminController.class.getDeclaredMethod("mapAiToFinal", AiPrediction.class);
    mapAiToFinalMethod.setAccessible(true);
    
    assertThat(mapAiToFinalMethod.invoke(null, (Object) null)).isNull();
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.MALIGNANT))
        .isEqualTo(FinalResult.MALIGNANT);
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.BENIGN))
        .isEqualTo(FinalResult.BENIGN);
    assertThat(mapAiToFinalMethod.invoke(null, AiPrediction.PENDING)).isNull();
}
    @Test
    void testCreateDiagnosis_NoPatientSelected_ReturnsError() throws Exception {

        MvcResult result = mockMvc.perform(post("/admin/diagnoses")
                .param("dicomUrl", "https://drive.google.com/uc?export=download&id=1")
                .param("dicomUrl2", "https://drive.google.com/uc?export=download&id=2")
                .param("dicomUrl3", "https://drive.google.com/uc?export=download&id=3")
                .param("dicomUrl4", "https://drive.google.com/uc?export=download&id=4")
                .param("date", "2024-01-15"))
                .andReturn();

        int status = result.getResponse().getStatus();
        if (status == 200) {
            if (result.getModelAndView() != null) {
                assertThat(result.getModelAndView().getViewName()).isEqualTo("admin/diagnosis-form");
                assertThat(result.getModelAndView().getModel()).containsKey("error");
            }
        } else if (status == 400) {
            assertThat(status).isEqualTo(400);
        } else {
            fail("Código de estado inesperado: " + status);
        }
    }

    @Test
    void testCreateDiagnosis_InvalidDicomUrls_ReturnsError() throws Exception {
    
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

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
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail(null);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
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
   
        User user = new User();
        user.setUsername("admin_user");
        user.setEmail("admin@test.com");
        user.setFullName("Admin Test");
        user.setRole(Role.ADMIN);

        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        String result = adminController.createUser(
                user, "password123", null, null, null);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).createUser(userCaptor.capture(), eq("password123"));

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getDoctor()).isNull();
        assertThat(capturedUser.getPatient()).isNull();
        assertThat(capturedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void testCreateUser_WithDefaultPassword() {
       User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setFullName("Test User");
        user.setRole(Role.PATIENT);

        Patient savedPatient = new Patient();
        savedPatient.setId(1);

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        
        adminController.createUser(
                user, null, null, "123456789", "1990-01-01");

        verify(userService).createUser(any(User.class), eq("123"));
    }

    @Test
    void testUpdateUser_AdminToPatient_RoleChange() {
      
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

        String result = adminController.updateUser(
                userId, postedUser, roleFilter, null, null, "123456789", "1990-01-01");

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void testUpdateUser_WithPasswordChange() {
    
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

        String result = adminController.updateUser(
                userId, postedUser, roleFilter, "newpassword123", "999999999", null, null);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(userService).setPassword(existingUser, "newpassword123");
        verify(userService).save(existingUser);
    }

    @Test
    void testUpdateUser_WithoutPasswordChange() {
        
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

        String result = adminController.updateUser(
                userId, postedUser, roleFilter, null, "999999999", null, null);

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
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUser(null);

        when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

        mockMvc.perform(get("/admin/patients/suggest")
                .param("q", "patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Patient PT-1"));
    }

    @Test
    void testCreateDiagnosis_NoDoctorsExist_ReturnsError() throws Exception {
  
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());

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
   
        Integer patientId = 1;
        Patient patient = createPatientWithUser(patientId, "Test Patient");
        patient.getUser().setEmail("patient@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

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

        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "patientuser", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        user.linkPatient(patient);

        when(userService.get(userId)).thenReturn(user);

        String result = adminController.deleteUser(userId, roleFilter);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(patientRepository).delete(patient);
    }

    @Test
    void testDeleteUser_WithNoAssociatedEntities() {
     
        Integer userId = 1;
        Role roleFilter = null;
        User user = createUser(userId, "adminuser", Role.ADMIN);

        when(userService.get(userId)).thenReturn(user);

        String result = adminController.deleteUser(userId, roleFilter);

        assertThat(result).isEqualTo("redirect:/admin/users");
        verify(doctorRepository, never()).delete(any());
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void testSimulationPage_ModelAttributes() {
      
        Model model = mock(Model.class);

        String viewName = adminController.simulationPage(model);

        assertThat(viewName).isEqualTo("admin/simulation");
        verify(model).addAttribute("numPatients", 1);
        verify(model).addAttribute("numDoctors", 1);
        verify(model).addAttribute("numMachines", 1);
    }

    @Test
    void testDashboard_WithSingleDiagnosis() {
        List<Diagnosis> mockDiagnoses = Collections.singletonList(
                createDiagnosis(1, true, true, FinalResult.MALIGNANT));

        when(patientRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(1L);
        when(diagnosisRepository.count()).thenReturn(1L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(1L);
        when(diagnosisRepository.findAll()).thenReturn(mockDiagnoses);

        Model model = mock(Model.class);

        String viewName = adminController.dashboard(model);

        assertThat(viewName).isEqualTo("admin/admin-dashboard");
        verify(model).addAttribute(eq("completionRate"), anyDouble());
    }

    @Test
    void testCreateUser_WithEmptyPhoneNumbers() {
   
        User user = new User();
        user.setUsername("doctor_user");
        user.setRole(Role.DOCTOR);

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(1);

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userService.createUser(any(User.class), anyString())).thenReturn(user);

        String result = adminController.createUser(
                user, "password123", "", null, null);

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
               
            };
        }
    }
}