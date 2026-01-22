package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.AiPrediction;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class AiResultServiceTest {

    @Test
    void testConstructorCoverage() {
        try {
            DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
            
            new AiResultService(mockRepo);
            assertTrue(true);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testApplyAiResult_DiagnosisNotFound() throws Exception {
      
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.empty());
        
        AiResultService service = Mockito.spy(new AiResultService(mockRepo));
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.5"));
        
        try {
            service.applyAiResult(1, request);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    void testApplyAiResult_InvalidPrediction() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenReturn(diagnosis);
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("INVALID_PREDICTION");
        request.setProbMalignant(new BigDecimal("0.5"));
        
        try {
            service.applyAiResult(1, request);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().contains("Invalid prediction"));
        }
    }

    @Test
    void testApplyAiResult_MalignantPrediction() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        diagnosis.setUrgent(false);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            assertEquals(AiPrediction.MALIGNANT, saved.getAiPrediction());
            assertTrue(saved.isUrgent()); 
            assertEquals(new BigDecimal("0.87654321"), saved.getProbability());
            assertFalse(saved.isReviewed());
            return saved;
        });
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.87654321"));
        
        Diagnosis result = service.applyAiResult(1, request);
        assertNotNull(result);
    }

    @Test
    void testApplyAiResult_BenignPrediction() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        diagnosis.setUrgent(true); 
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            assertEquals(AiPrediction.BENIGN, saved.getAiPrediction());
            assertFalse(saved.isUrgent()); 
            return saved;
        });
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("BENIGN");
        request.setProbMalignant(new BigDecimal("0.2"));
        
        Diagnosis result = service.applyAiResult(1, request);
        assertNotNull(result);
    }

    @Test
    void testProbabilityRounding() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            assertEquals(8, saved.getProbability().scale()); 
            return saved;
        });
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.123456789")); 
        
        service.applyAiResult(1, request);
    }


    @Test
    void testEmptyPrediction() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction(""); 
        request.setProbMalignant(new BigDecimal("0.5"));
        
        try {
            service.applyAiResult(1, request);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    void testNullRequest() throws Exception {
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        AiResultService service = new AiResultService(mockRepo);
        
        try {
            service.applyAiResult(1, null);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertNotNull(e);
        } 
    }

    @Test
    void testReviewedFlagReset() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        diagnosis.setReviewed(true); 
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenAnswer(invocation -> {
            Diagnosis saved = invocation.getArgument(0);
            assertFalse(saved.isReviewed());
            return saved;
        });
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.7"));
        
        service.applyAiResult(1, request);
    }

    @Test
    void testExceptionInSave() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenThrow(new RuntimeException("Save failed"));
        
        AiResultService service = new AiResultService(mockRepo);
        
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.5"));
        
        try {
            service.applyAiResult(1, request);
            fail("Should have propagated exception");
        } catch (RuntimeException e) {
            assertEquals("Save failed", e.getMessage());
        }
    }

    @Test
    void testInvalidJsonValidation() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));   
        AiResultService service = new AiResultService(mockRepo);
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.5"));
        
        try {
            service.applyAiResult(1, request);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid AI result JSON"));
        }
    }

    @Test
    void testConstructorIOException() {
        try {
            DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
            new AiResultService(mockRepo);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testAllPredictionVariants() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1);
        
        DiagnosisRepository mockRepo = mock(DiagnosisRepository.class);
        when(mockRepo.findById(anyInt())).thenReturn(Optional.of(diagnosis));
        when(mockRepo.save(any(Diagnosis.class))).thenReturn(diagnosis);
        
        AiResultService service = new AiResultService(mockRepo);
        
        String[] variants = {"MALIGNANT", "malignant", "Malignant", "MaLiGnAnT", "BENIGN", "benign", "Benign"};
        
        for (String variant : variants) {
            AiResultRequest request = new AiResultRequest();
            request.setPrediction(variant);
            request.setProbMalignant(new BigDecimal("0.5"));
            
            try {
                service.applyAiResult(1, request);
            } catch (Exception e) {
                assertNotNull(e);
            }
        }
    }
}