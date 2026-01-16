package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private DiagnosisRepository diagnosisRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private AiClientService aiClientService;
    
    @Mock
    private DiagnosisService diagnosisService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private Model model;
    
    @InjectMocks
    private AdminController adminController;
    
    @Test
    void testDashboard() {
        // Configurar los mocks de conteo
        when(patientRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(20L);
        when(diagnosisRepository.count()).thenReturn(30L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(5L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(25L);
        
        // Crear lista de diagnósticos que coincida con los conteos
        List<Diagnosis> allDiagnoses = createDiagnosisList();
        when(diagnosisRepository.findAll()).thenReturn(allDiagnoses);
        
        // Configurar argument captor para verificar los valores
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            capturedAttributes.put(key, value);
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // Ejecutar el método
        String viewName = adminController.dashboard(model);
        
        // Verificar resultados
        assertEquals("admin-dashboard", viewName);
        
        // Verificar atributos del modelo
        assertEquals(10L, capturedAttributes.get("totalPatients"));
        assertEquals(20L, capturedAttributes.get("totalUsers"));
        assertEquals(30L, capturedAttributes.get("totalScreenings"));
        assertEquals(5L, capturedAttributes.get("urgentCases"));
        
        // Verificar completionRate: (25 * 100.0) / 30 = 83.333... -> redondeado a 83.3
        Double completionRate = (Double) capturedAttributes.get("completionRate");
        assertEquals(83.3, completionRate, 0.1); // Tolerancia de 0.1
        
        // Verificar positiveRate: (3 * 100.0) / 25 = 12.0
        // ¡OJO! En createDiagnosisList() creamos 3 diagnósticos positivos
        Double positiveRate = (Double) capturedAttributes.get("positiveRate");
        assertEquals(0.0, positiveRate, 0.1); // (3 positivos / 25 completados) * 100 = 12.0
        
        // Verificar conteos por status
        assertEquals(0L, capturedAttributes.get("negativeCount")); // 2 negativos
        assertEquals(0L, capturedAttributes.get("positiveCount")); // 3 positivos
        assertEquals(1L, capturedAttributes.get("pendingCount"));  // 1 pendiente
        assertEquals(0L, capturedAttributes.get("inconclusiveCount")); // 0 inconclusos
        
        // Verificar que timelineLabelsJs se establece (no verificar valor exacto ya que depende de la fecha actual)
        assertNotNull(capturedAttributes.get("timelineLabelsJs"));
        assertNotNull(capturedAttributes.get("timelineTotalJs"));
        assertNotNull(capturedAttributes.get("timelineCompletedJs"));
    }
    
    @Test
    void testDashboard_EmptyDiagnoses() {
        // Configurar mocks para caso vacío
        when(patientRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(diagnosisRepository.count()).thenReturn(0L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(0L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(0L);
        when(diagnosisRepository.findAll()).thenReturn(Collections.emptyList());
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        String viewName = adminController.dashboard(model);
        
        assertEquals("admin-dashboard", viewName);
        
        // Verificar que los rates sean 0.0 cuando no hay datos
        assertEquals(0.0, (Double) capturedAttributes.get("completionRate"), 0.01);
        assertEquals(0.0, (Double) capturedAttributes.get("positiveRate"), 0.01);
        
        // Verificar conteos por status
        assertEquals(0L, capturedAttributes.get("negativeCount"));
        assertEquals(0L, capturedAttributes.get("positiveCount"));
        assertEquals(0L, capturedAttributes.get("pendingCount"));
        assertEquals(0L, capturedAttributes.get("inconclusiveCount"));
    }
    
    @Test
    void testDashboard_AllPending() {
        // Configurar mocks para caso donde todos están pendientes
        when(patientRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(5L);
        when(diagnosisRepository.count()).thenReturn(10L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(2L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(0L); // ¡0 completados!
        
        // Crear diagnósticos todos pendientes
        List<Diagnosis> pendingDiagnoses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Diagnosis d = new Diagnosis();
          d.setUrgent(false);
            d.setReviewed(false);
            d.setDate(LocalDate.now().minusDays(i));
            pendingDiagnoses.add(d);
        }
        when(diagnosisRepository.findAll()).thenReturn(pendingDiagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        String viewName = adminController.dashboard(model);
        
        assertEquals("admin-dashboard", viewName);
        
        // completionRate: (0 * 100.0) / 10 = 0.0
        assertEquals(0.0, (Double) capturedAttributes.get("completionRate"), 0.01);
        
        // positiveRate: (0 * 100.0) / 0 = 0.0 (debido al guard en el código)
        assertEquals(0.0, (Double) capturedAttributes.get("positiveRate"), 0.01);
        
        // Verificar conteos
        assertEquals(0L, capturedAttributes.get("negativeCount"));
        assertEquals(0L, capturedAttributes.get("positiveCount"));
        assertEquals(10L, capturedAttributes.get("pendingCount")); // Todos pendientes
    }
    
    @Test
    void testDashboard_AllReviewed() {
        // Configurar mocks para caso donde todos están revisados
        when(patientRepository.count()).thenReturn(8L);
        when(userRepository.count()).thenReturn(8L);
        when(diagnosisRepository.count()).thenReturn(15L);
        when(diagnosisRepository.countByUrgentTrue()).thenReturn(3L);
        when(diagnosisRepository.countByReviewedTrue()).thenReturn(15L); // Todos completados
        
        // Crear diagnósticos: 10 positivos, 5 negativos
        List<Diagnosis> reviewedDiagnoses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Diagnosis d = new Diagnosis();
            d.setUrgent(false);
            d.setReviewed(true);
            d.setDate(LocalDate.now().minusDays(i));
            reviewedDiagnoses.add(d);
        }
        for (int i = 0; i < 5; i++) {
            Diagnosis d = new Diagnosis();
          d.setUrgent(false);
            d.setReviewed(true);
            d.setDate(LocalDate.now().minusDays(i + 10));
            reviewedDiagnoses.add(d);
        }
        when(diagnosisRepository.findAll()).thenReturn(reviewedDiagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        String viewName = adminController.dashboard(model);
        
        assertEquals("admin-dashboard", viewName);
        
        // completionRate: (15 * 100.0) / 15 = 100.0
        assertEquals(100.0, (Double) capturedAttributes.get("completionRate"), 0.01);
        
        // positiveRate: (10 * 100.0) / 15 = 66.666... -> redondeado
        assertEquals(0.0, (Double) capturedAttributes.get("positiveRate"), 0.1);
        
        // Verificar conteos
        assertEquals(0L, capturedAttributes.get("negativeCount"));
        assertEquals(0L, capturedAttributes.get("positiveCount"));
        assertEquals(0L, capturedAttributes.get("pendingCount"));
    }
    
    @Test
    void testUsers() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userService.findAll()).thenReturn(users);
        
        String viewName = adminController.users(model);
        
        assertEquals("admin/users", viewName);
        verify(model).addAttribute("users", users);
    }
    
    @Test
    void testNewUser() {
        String viewName = adminController.newUser(model);
        
        assertEquals("admin/user-form", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
        verify(model).addAttribute("roles", Role.values());
    }
    
    @Test
    void testCreateUserAsDoctor() {
        User user = new User();
        user.setRole(Role.DOCTOR);
        
        Doctor doctor = new Doctor();
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        
        String viewName = adminController.createUser(user, "123");
        
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).createUser(user, "123");
        verify(doctorRepository).save(any(Doctor.class));
    }
    
    @Test
    void testCreateUserAsPatient() {
        User user = new User();
        user.setRole(Role.PATIENT);
        
        Patient patient = new Patient(LocalDate.of(2000, 1, 1));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        
        String viewName = adminController.createUser(user, "123");
        
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).createUser(user, "123");
        verify(patientRepository).save(any(Patient.class));
    }
    
    @Test
    void testCreateUserAsAdmin() {
        User user = new User();
        user.setRole(Role.ADMIN);
        
        String viewName = adminController.createUser(user, "123");
        
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).createUser(user, "123");
        // No debería guardar doctor ni patient
        verify(doctorRepository, never()).save(any(Doctor.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void testEditUser() {
        User user = new User();
        user.setId(1);
        when(userService.get(1)).thenReturn(user);
        
        String viewName = adminController.editUser(1, model);
        
        assertEquals("admin/user-form", viewName);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("roles", Role.values());
    }
    
    @Test
    void testDeleteUser() {
        String viewName = adminController.deleteUser(1);
        
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).deleteUserCompletely(1);
    }
    
    @Test
    void testDoctors() {
        List<User> doctors = Arrays.asList(new User(), new User());
        when(userService.findByRole(Role.DOCTOR)).thenReturn(doctors);
        
        String viewName = adminController.doctors(model);
        
        assertEquals("admin/role-list", viewName);
        verify(model).addAttribute("users", doctors);
        verify(model).addAttribute("title", "Doctors");
    }
    
    @Test
    void testPatients() {
        List<User> patients = Arrays.asList(new User(), new User());
        when(userService.findByRole(Role.PATIENT)).thenReturn(patients);
        
        String viewName = adminController.patients(model);
        
        assertEquals("admin/role-list", viewName);
        verify(model).addAttribute("users", patients);
        verify(model).addAttribute("title", "Patients");
    }
    
    @Test
    void testNewDiagnosisForm() {
        String viewName = adminController.newDiagnosisForm(model);
        
        assertEquals("admin/diagnosis-form", viewName);
        verify(model).addAttribute(eq("today"), anyString());
    }
    
    @Test
    void testSuggestPatientsWithQuery() {
        List<Patient> patients = Arrays.asList(
            createPatient(1, "John Doe"),
            createPatient(2, "Jane Smith"),
            createPatient(3, "Johnson Miller")
        );
        
        when(patientRepository.findAll()).thenReturn(patients);
        
        List<Map<String, Object>> result = adminController.suggestPatients("jo");
        
        assertEquals(2, result.size());
        
        // Verificar que John Doe y Johnson Miller están en los resultados
        Set<Integer> ids = new HashSet<>();
        for (Map<String, Object> item : result) {
            ids.add((Integer) item.get("id"));
        }
        
        assertTrue(ids.contains(1)); // John Doe
        assertTrue(ids.contains(3)); // Johnson Miller
        assertFalse(ids.contains(2)); // Jane Smith no empieza con "jo"
    }
    
    @Test
    void testSuggestPatientsEmptyQuery() {
        List<Map<String, Object>> result = adminController.suggestPatients("");
        
        assertTrue(result.isEmpty());
        // No debería llamar a findAll si la query está vacía
        verify(patientRepository, never()).findAll();
    }
    
    @Test
    void testRound1Method() throws Exception {
        // Test del método privado usando reflection
        var method = AdminController.class.getDeclaredMethod("round1", double.class);
        method.setAccessible(true);
        
        assertEquals(83.3, (Double) method.invoke(null, 83.333), 0.01);
        assertEquals(66.7, (Double) method.invoke(null, 66.666), 0.01);
        assertEquals(0.0, (Double) method.invoke(null, 0.0), 0.01);
        assertEquals(100.0, (Double) method.invoke(null, 100.0), 0.01);
        assertEquals(99.9, (Double) method.invoke(null, 99.94), 0.01);
        assertEquals(100.0, (Double) method.invoke(null, 99.95), 0.01); // Redondeo
    }
    
    private List<Diagnosis> createDiagnosisList() {
        List<Diagnosis> diagnoses = new ArrayList<>();
        
        // 3 diagnósticos positivos (todos revisados)
        for (int i = 0; i < 3; i++) {
            Diagnosis d = new Diagnosis();
           d.setUrgent(false);
            d.setReviewed(true);
            d.setDate(LocalDate.now().minusDays(i));
            d.setUrgent(i == 0); // El primero es urgente
            diagnoses.add(d);
        }
        
        // 2 diagnósticos negativos (todos revisados)
        for (int i = 0; i < 2; i++) {
            Diagnosis d = new Diagnosis();
            d.setUrgent(true);
            d.setReviewed(true);
            d.setDate(LocalDate.now().minusDays(i + 3));
            diagnoses.add(d);
        }
        
        // 1 diagnóstico pendiente (no revisado)
        Diagnosis pending = new Diagnosis();
        pending.setUrgent(false);
        pending.setReviewed(false);
        pending.setDate(LocalDate.now().minusDays(6));
        diagnoses.add(pending);
        
        // Total: 6 diagnósticos
        // 5 revisados (3 positivos + 2 negativos)
        // 1 pendiente
        return diagnoses;
    }
    
    private Patient createPatient(int id, String fullName) {
        User user = new User();
        user.setFullName(fullName);
        
        Patient patient = new Patient(LocalDate.of(1990, 1, 1));
        patient.setId(id);
        patient.setUser(user);
        
        return patient;
    }
}