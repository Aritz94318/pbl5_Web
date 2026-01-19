package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {
    
    @Mock
    private DiagnosisService diagnosisService;
    
    @Mock
    private Model model;
    
    @InjectMocks
    private DoctorController doctorController;
    
    private LocalDate today;
    
    @BeforeEach
    void setUp() {
        today = LocalDate.of(2024, 1, 16); // Fecha fija para tests consistentes
    }
    
    @Test
    void dashboard_WithNullDate_UsesToday() {
        // Given
        LocalDate testToday = LocalDate.now(); // Usar la fecha real del sistema
        List<Diagnosis> diagnoses = createDiagnosesForDate(testToday, 5);
        
        when(diagnosisService.findByDateSortedByUrgency(testToday)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        String viewName = doctorController.dashboard(null, model);
        
        // Then
        assertEquals("doctor/doctor-dashboard", viewName);
        assertEquals(diagnoses, capturedAttributes.get("diagnoses"));
        assertEquals(5, capturedAttributes.get("totalCount"));
        assertEquals(testToday, capturedAttributes.get("selectedDate"));
        
        // Verify service was called with today's date
        verify(diagnosisService).findByDateSortedByUrgency(testToday);
    }

    
    @Test
    void dashboard_WithUrgentAndRoutineCases() {
        // Given
        LocalDate selectedDate = today.minusDays(2);
        List<Diagnosis> diagnoses = createMixedUrgencyDiagnoses(selectedDate);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        String viewName = doctorController.dashboard(selectedDate, model);
        
        // Then
        assertEquals("doctor/doctor-dashboard", viewName);
        
        // Check counts
        assertEquals(10, capturedAttributes.get("totalCount"));
        assertEquals(4L, capturedAttributes.get("urgentCount")); // 4 urgent cases
        assertEquals(6L, capturedAttributes.get("routineCount")); // 10 total - 4 urgent = 6 routine
        
        // Verify previous screenings are calculated correctly
        Map<Integer, Long> previousScreenings = 
            (Map<Integer, Long>) capturedAttributes.get("previousScreenings");
        assertNotNull(previousScreenings);
        assertEquals(2L, previousScreenings.get(1)); // Patient 1 has 2 diagnoses
        assertEquals(3L, previousScreenings.get(2)); // Patient 2 has 3 diagnoses
        assertEquals(5L, previousScreenings.get(3)); // Patient 3 has 5 diagnoses
        
        verify(diagnosisService).findByDateSortedByUrgency(selectedDate);
    }
    
    @Test
    void dashboard_EmptyDiagnosesList() {
        // Given
        LocalDate selectedDate = today.minusDays(1);
        List<Diagnosis> emptyList = Collections.emptyList();
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(emptyList);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        String viewName = doctorController.dashboard(selectedDate, model);
        
        // Then
        assertEquals("doctor/doctor-dashboard", viewName);
        assertEquals(0, capturedAttributes.get("totalCount"));
        assertEquals(0L, capturedAttributes.get("urgentCount"));
        assertEquals(0L, capturedAttributes.get("routineCount"));
        
        Map<Integer, Long> previousScreenings = 
            (Map<Integer, Long>) capturedAttributes.get("previousScreenings");
        assertTrue(previousScreenings.isEmpty());
        
        verify(diagnosisService).findByDateSortedByUrgency(selectedDate);
    }
    
    @Test
    void dashboard_AllRoutineCases() {
        // Given
        LocalDate selectedDate = today.minusDays(2);
        List<Diagnosis> diagnoses = createAllRoutineDiagnoses(selectedDate, 7);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        assertEquals(7, capturedAttributes.get("totalCount"));
        assertEquals(0L, capturedAttributes.get("urgentCount")); // No urgent cases
        assertEquals(7L, capturedAttributes.get("routineCount")); // All are routine
        
        verify(diagnosisService).findByDateSortedByUrgency(selectedDate);
    }
    
    @Test
    void dashboard_AllUrgentCases() {
        // Given
        LocalDate selectedDate = today.minusDays(1);
        List<Diagnosis> diagnoses = createAllUrgentDiagnoses(selectedDate, 4);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        assertEquals(4, capturedAttributes.get("totalCount"));
        assertEquals(4L, capturedAttributes.get("urgentCount")); // All urgent
        assertEquals(0L, capturedAttributes.get("routineCount")); // No routine
        
        verify(diagnosisService).findByDateSortedByUrgency(selectedDate);
    }
    
    @Test
    void diagnosisDetails_Success() {
        // Given
        Integer diagnosisId = 42;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        List<Diagnosis> historyDiagnoses = Arrays.asList(
            createDiagnosis(40),
            createDiagnosis(41),
            diagnosis
        );
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(diagnosis.getPatient().getId()))
            .thenReturn(historyDiagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        String viewName = doctorController.diagnosisDetails(diagnosisId, model);
        
        // Then
        assertEquals("doctor/doctor-diagnosis", viewName);
        assertEquals(diagnosis, capturedAttributes.get("diagnosis"));
        assertEquals(diagnosis.getPatient(), capturedAttributes.get("patient"));
        
        // Verify history is sorted newest first
        List<Diagnosis> capturedHistory = 
            (List<Diagnosis>) capturedAttributes.get("historyDiagnoses");
        assertEquals(3, capturedHistory.size());
        
        // Check sorting: newest first // Current diagnosis (newest)
        assertEquals(41, capturedHistory.get(1).getId()); // Second newest
        assertEquals(42, capturedHistory.get(2).getId()); // Oldest
        
        verify(diagnosisService).findById(diagnosisId);
        verify(diagnosisService).findByPatient(diagnosis.getPatient().getId());
    }
    
    @Test
    void diagnosisDetails_DiagnosisNotFound() {
        // Given
        Integer diagnosisId = 999;
        when(diagnosisService.findById(diagnosisId))
            .thenThrow(new RuntimeException("Diagnosis not found"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            doctorController.diagnosisDetails(diagnosisId, model);
        });
        
        verify(diagnosisService).findById(diagnosisId);
        verify(diagnosisService, never()).findByPatient(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }
    
    @Test
    void diagnosisDetails_EmptyHistory() {
        // Given
        Integer diagnosisId = 50;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(diagnosis.getPatient().getId()))
            .thenReturn(Collections.emptyList());
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        String viewName = doctorController.diagnosisDetails(diagnosisId, model);
        
        // Then
        assertEquals("doctor/doctor-diagnosis", viewName);
        
        List<Diagnosis> capturedHistory = 
            (List<Diagnosis>) capturedAttributes.get("historyDiagnoses");
        assertTrue(capturedHistory.isEmpty());
        
        verify(diagnosisService).findById(diagnosisId);
        verify(diagnosisService).findByPatient(diagnosis.getPatient().getId());
    }
    
    @Test
    void diagnosisDetails_HistoryWithSingleDiagnosis() {
        // Given
        Integer diagnosisId = 100;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        List<Diagnosis> historyDiagnoses = Collections.singletonList(diagnosis);
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(diagnosis.getPatient().getId()))
            .thenReturn(historyDiagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.diagnosisDetails(diagnosisId, model);
        
        // Then
        List<Diagnosis> capturedHistory = 
            (List<Diagnosis>) capturedAttributes.get("historyDiagnoses");
        assertEquals(1, capturedHistory.size());
        assertEquals(diagnosisId, capturedHistory.get(0).getId());
    }
    
    @Test
    void diagnosisDetails_HistoryUnsorted() {
        // Given
        Integer diagnosisId = 200;
        Diagnosis diagnosis = createDiagnosis(diagnosisId);
        diagnosis.setDate(LocalDate.now()); // Most recent
        
        Diagnosis oldDiagnosis1 = createDiagnosis(201);
        oldDiagnosis1.setDate(LocalDate.now().minusDays(10)); // Oldest
        
        Diagnosis oldDiagnosis2 = createDiagnosis(202);
        oldDiagnosis2.setDate(LocalDate.now().minusDays(5)); // Middle
        
        List<Diagnosis> historyDiagnoses = Arrays.asList(
            oldDiagnosis1, // Oldest first (unsorted)
            diagnosis,     // Newest
            oldDiagnosis2  // Middle
        );
        
        when(diagnosisService.findById(diagnosisId)).thenReturn(diagnosis);
        when(diagnosisService.findByPatient(diagnosis.getPatient().getId()))
            .thenReturn(historyDiagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.diagnosisDetails(diagnosisId, model);
        
        // Then - Should be sorted newest first
        List<Diagnosis> capturedHistory = 
            (List<Diagnosis>) capturedAttributes.get("historyDiagnoses");
        
        assertEquals(3, capturedHistory.size());
        assertEquals(diagnosisId, capturedHistory.get(0).getId()); // Newest
        assertEquals(202, capturedHistory.get(1).getId());         // Middle
        assertEquals(201, capturedHistory.get(2).getId());         // Oldest
    }
    
    @Test
    void datePillConstructorAndGetters() {
        // Test the DatePill DTO
        String label = "Today";
        String display = "01/15/2024";
        String param = "2024-01-15";
        boolean active = true;
        
        DoctorController.DatePill datePill = 
            new DoctorController.DatePill(label, display, param, active);
        
        assertEquals(label, datePill.getLabel());
        assertEquals(display, datePill.getDisplay());
        assertEquals(param, datePill.getParam());
        assertTrue(datePill.isActive());
    }
    
    @Test
    void datePillNotActive() {
        DoctorController.DatePill datePill = 
            new DoctorController.DatePill("Jan 10", "01/10/2024", "2024-01-10", false);
        
        assertFalse(datePill.isActive());
    }
    
    @Test
    void datePillEdgeCases() {
        // Test with empty strings
        DoctorController.DatePill datePill1 = 
            new DoctorController.DatePill("", "", "", true);
        assertEquals("", datePill1.getLabel());
        assertEquals("", datePill1.getDisplay());
        assertEquals("", datePill1.getParam());
        assertTrue(datePill1.isActive());
        
        // Test with special characters
        DoctorController.DatePill datePill2 = 
            new DoctorController.DatePill("Dec 31", "12/31/2023", "2023-12-31", false);
        assertEquals("Dec 31", datePill2.getLabel());
        assertEquals("12/31/2023", datePill2.getDisplay());
        assertEquals("2023-12-31", datePill2.getParam());
        assertFalse(datePill2.isActive());
    }
    
    @Test
    void dashboard_DatePillsWithTodaySelected() {
        // Given: Today is selected
        LocalDate selectedDate = LocalDate.now();
        List<Diagnosis> diagnoses = createDiagnosesForDate(selectedDate, 3);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        List<DoctorController.DatePill> datePills = 
            (List<DoctorController.DatePill>) capturedAttributes.get("datePills");
        
        // Today's pill should be active
        DoctorController.DatePill todayPill = datePills.get(5); // Last pill is today
        assertTrue(todayPill.isActive());
        assertEquals("Today", todayPill.getLabel());
    }
    
    @Test
    void dashboard_DatePillsWithYesterdaySelected() {
        // Given: Yesterday is selected
        LocalDate selectedDate = LocalDate.now().minusDays(1);
        List<Diagnosis> diagnoses = createDiagnosesForDate(selectedDate, 2);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        List<DoctorController.DatePill> datePills = 
            (List<DoctorController.DatePill>) capturedAttributes.get("datePills");
        
        // Yesterday's pill should be active
        DoctorController.DatePill yesterdayPill = datePills.get(4); // Second last is yesterday
        assertTrue(yesterdayPill.isActive());
        assertEquals("Yesterday", yesterdayPill.getLabel());
    }
    
    @Test
    void dashboard_DatePillsWithFiveDaysAgoSelected() {
        // Given: 5 days ago is selected
        LocalDate selectedDate = LocalDate.now().minusDays(5);
        List<Diagnosis> diagnoses = createDiagnosesForDate(selectedDate, 1);
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        List<DoctorController.DatePill> datePills = 
            (List<DoctorController.DatePill>) capturedAttributes.get("datePills");
        
        // First pill (5 days ago) should be active
        DoctorController.DatePill fiveDaysAgoPill = datePills.get(0);
        assertTrue(fiveDaysAgoPill.isActive());
        
        // Label should be formatted as "MMM dd"
        DateTimeFormatter monthDayFmt = DateTimeFormatter.ofPattern("MMM dd");
        assertEquals(selectedDate.format(monthDayFmt), fiveDaysAgoPill.getLabel());
    }
    
    @Test
    void dashboard_SinglePatientMultipleDiagnoses() {
        // Given: All diagnoses belong to the same patient
        LocalDate selectedDate = today.minusDays(2);
        List<Diagnosis> diagnoses = new ArrayList<>();
        
        Patient patient = new Patient(LocalDate.of(1990, 5, 15));
        patient.setId(1);
        
        for (int i = 1; i <= 5; i++) {
            Diagnosis d = new Diagnosis();
            d.setId(i);
            d.setDate(selectedDate);
            d.setUrgent(i <= 2); // First 2 are urgent
            d.setPatient(patient);
            diagnoses.add(d);
        }
        
        when(diagnosisService.findByDateSortedByUrgency(selectedDate)).thenReturn(diagnoses);
        
        Map<String, Object> capturedAttributes = new HashMap<>();
        doAnswer(invocation -> {
            capturedAttributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(model).addAttribute(anyString(), any());
        
        // When
        doctorController.dashboard(selectedDate, model);
        
        // Then
        assertEquals(5, capturedAttributes.get("totalCount"));
        assertEquals(2L, capturedAttributes.get("urgentCount"));
        assertEquals(3L, capturedAttributes.get("routineCount"));
        
        Map<Integer, Long> previousScreenings = 
            (Map<Integer, Long>) capturedAttributes.get("previousScreenings");
        assertEquals(1, previousScreenings.size());
        assertEquals(5L, previousScreenings.get(1)); // Patient 1 has 5 diagnoses
    }
    
    // Helper methods
    
    private List<Diagnosis> createDiagnosesForDate(LocalDate date, int count) {
        List<Diagnosis> diagnoses = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Diagnosis diagnosis = new Diagnosis();
            diagnosis.setId(i);
            diagnosis.setDate(date);
            diagnosis.setUrgent(i % 2 == 0); // Even IDs are urgent
            
            Patient patient = new Patient(LocalDate.of(1980 + i, 1, 1));
            patient.setId(i);
            diagnosis.setPatient(patient);
            
            diagnoses.add(diagnosis);
        }
        return diagnoses;
    }
    
    private List<Diagnosis> createMixedUrgencyDiagnoses(LocalDate date) {
        List<Diagnosis> diagnoses = new ArrayList<>();
        
        // Patient 1: 2 diagnoses (1 urgent, 1 routine)
        Patient patient1 = new Patient(LocalDate.of(1990, 1, 1));
        patient1.setId(1);
        
        Diagnosis d1 = new Diagnosis();
        d1.setId(1);
        d1.setDate(date);
        d1.setUrgent(true);
        d1.setPatient(patient1);
        diagnoses.add(d1);
        
        Diagnosis d2 = new Diagnosis();
        d2.setId(2);
        d2.setDate(date);
        d2.setUrgent(false);
        d2.setPatient(patient1);
        diagnoses.add(d2);
        
        // Patient 2: 3 diagnoses (2 urgent, 1 routine)
        Patient patient2 = new Patient(LocalDate.of(1985, 5, 15));
        patient2.setId(2);
        
        for (int i = 3; i <= 5; i++) {
            Diagnosis d = new Diagnosis();
            d.setId(i);
            d.setDate(date);
            d.setUrgent(i <= 4); // IDs 3 and 4 are urgent
            d.setPatient(patient2);
            diagnoses.add(d);
        }
        
        // Patient 3: 5 diagnoses (1 urgent, 4 routine)
        Patient patient3 = new Patient(LocalDate.of(1975, 10, 30));
        patient3.setId(3);
        
        for (int i = 6; i <= 10; i++) {
            Diagnosis d = new Diagnosis();
            d.setId(i);
            d.setDate(date);
            d.setUrgent(i == 6); // Only ID 6 is urgent
            d.setPatient(patient3);
            diagnoses.add(d);
        }
        
        return diagnoses;
    }
    
    private List<Diagnosis> createAllRoutineDiagnoses(LocalDate date, int count) {
        List<Diagnosis> diagnoses = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Diagnosis d = new Diagnosis();
            d.setId(i);
            d.setDate(date);
            d.setUrgent(false); // All routine
            
            Patient p = new Patient(LocalDate.of(1980 + i, 1, 1));
            p.setId(i);
            d.setPatient(p);
            
            diagnoses.add(d);
        }
        return diagnoses;
    }
    
    private List<Diagnosis> createAllUrgentDiagnoses(LocalDate date, int count) {
        List<Diagnosis> diagnoses = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Diagnosis d = new Diagnosis();
            d.setId(i);
            d.setDate(date);
            d.setUrgent(true); // All urgent
            
            Patient p = new Patient(LocalDate.of(1980 + i, 1, 1));
            p.setId(i);
            d.setPatient(p);
            
            diagnoses.add(d);
        }
        return diagnoses;
    }
    
    private Diagnosis createDiagnosis(Integer id) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(id);
        diagnosis.setDate(LocalDate.now().minusDays(id % 10)); // Vary dates
        
        Patient patient = new Patient(LocalDate.of(1985, 5, 15));
        patient.setId(id * 10);
        diagnosis.setPatient(patient);
        
        return diagnosis;
    }
}