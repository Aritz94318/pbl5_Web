package edu.mondragon.we2.pinkAlert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.service.AiResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiWebhookControllerTest {
    
    @Mock
    private AiResultService aiResultService;
    
    @InjectMocks
    private AiWebhookController aiWebhookController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AiResultRequest validRequest;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiWebhookController).build();
        objectMapper = new ObjectMapper();
        
        validRequest = new AiResultRequest();
        validRequest.setPrediction("MALIGNANT");
        validRequest.setProbMalignant(new BigDecimal("0.95"));
    }
    
    @Test
    void applyAiResult_Success_MalignantPrediction() throws Exception {
        // Given
        Integer diagnosisId = 123;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.95
            }
            """;
        
        // When & Then (using MockMvc)
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        // Verify service call
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_Success_BenignPrediction() throws Exception {
        // Given
        Integer diagnosisId = 456;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.15
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_Success_XMLContent() throws Exception {
        // Given
        Integer diagnosisId = 789;
        String xmlRequest = """
            <?xml version="1.0" encoding="UTF-8"?>
            <AiResultRequest>
                <prediction>MALIGNANT</prediction>
                <prob_malignant>0.87</prob_malignant>
            </AiResultRequest>
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithHighPrecisionProbability() throws Exception {
        // Given
        Integer diagnosisId = 111;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.999876
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithVeryLowProbability() throws Exception {
        // Given
        Integer diagnosisId = 222;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.000123
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MissingPredictionField() throws Exception {
        // Given
        Integer diagnosisId = 333;
        String jsonRequest = """
            {
                "prob_malignant": 0.75
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); // Still OK, validation happens in service
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MissingProbabilityField() throws Exception {
        // Given
        Integer diagnosisId = 444;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT"
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_InvalidPredictionValue() throws Exception {
        // Given
        Integer diagnosisId = 555;
        String jsonRequest = """
            {
                "prediction": "INVALID_VALUE",
                "prob_malignant": 0.5
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); // Controller accepts it, validation in service
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
 
    @Test
    void applyAiResult_EmptyRequestBody() throws Exception {
        // Given
        Integer diagnosisId = 888;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest()); // Empty body causes Bad Request
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_MalformedJSON() throws Exception {
        // Given
        Integer diagnosisId = 999;
        String malformedJson = "{ malformed json ";
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_ServiceThrowsException() throws Exception {
        // Given
        Integer diagnosisId = 101;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.92
            }
            """;
        
        doThrow(new RuntimeException("Database error"))
            .when(aiResultService).applyAiResult(eq(diagnosisId), any());
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_ServiceThrowsIllegalArgumentException() throws Exception {
        // Given
        Integer diagnosisId = 202;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.92
            }
            """;
        
        doThrow(new IllegalArgumentException("Diagnosis not found"))
            .when(aiResultService).applyAiResult(eq(diagnosisId), any());
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithIntegerProbability() throws Exception {
        // Given
        Integer diagnosisId = 303;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithProbabilityOne() throws Exception {
        // Given
        Integer diagnosisId = 404;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 1
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    

  
    @Test
    void applyAiResult_NullValuesInRequest() throws Exception {
        // Given
        Integer diagnosisId = 707;
        String jsonRequest = """
            {
                "prediction": null,
                "prob_malignant": null
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); // Accepts nulls
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_AdditionalFieldsInJSON() throws Exception {
        // Given
        Integer diagnosisId = 808;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.25,
                "extra_field": "should be ignored",
                "another_field": 123
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_DirectControllerMethodTest() throws IOException, ProcessingException {
        // Test the controller method directly (not through MockMvc)
        // Given
        Integer diagnosisId = 909;
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.91"));
        
        // When
        ResponseEntity<Void> response = aiWebhookController.applyAiResult(diagnosisId, request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResultService).applyAiResult(diagnosisId, request);
    }
    
    @Test
    void applyAiResult_DirectControllerMethodTest_Exception() throws IOException, ProcessingException {
        // Given
        Integer diagnosisId = 1010;
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("BENIGN");
        request.setProbMalignant(new BigDecimal("0.12"));
        
        doThrow(new RuntimeException("Test exception"))
            .when(aiResultService).applyAiResult(diagnosisId, request);
        
        // When
        ResponseEntity<Void> response = aiWebhookController.applyAiResult(diagnosisId, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(aiResultService).applyAiResult(diagnosisId, request);
    }
    
    @Test
    void applyAiResult_WithDifferentContentTypes() throws Exception {
        // Test different content type headers
        Integer diagnosisId = 1111;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.96
            }
            """;
        
        // Test with different content type variations
        String[] contentTypes = {
            "application/json",
            "application/json; charset=utf-8",
            "application/json;charset=UTF-8",
            "application/xml",
            "application/xml; charset=utf-8"
        };
        
        for (String contentType : contentTypes) {
            mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                    .contentType(contentType)
                    .content(contentType.contains("xml") ? convertToXml(jsonRequest) : jsonRequest))
                    .andExpect(status().isOk());
        }
        
        verify(aiResultService, times(contentTypes.length))
            .applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_InvalidDiagnosisIdInURL() throws Exception {
        // Given
        String invalidId = "not-a-number";
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.85
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()); // Spring can't convert "not-a-number" to Integer
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_DiagnosisIdZero() throws Exception {
        // Given: ID 0 might be invalid but controller should accept it
        Integer diagnosisId = 0;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.1
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(0), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_DiagnosisIdNegative() throws Exception {
        // Given: Negative ID
        Integer diagnosisId = -1;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.8
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); // Controller accepts negative IDs
        
        verify(aiResultService).applyAiResult(eq(-1), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MaxIntegerId() throws Exception {
        // Given: Maximum integer value
        Integer diagnosisId = Integer.MAX_VALUE;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.05
            }
            """;
        
        // When & Then
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(Integer.MAX_VALUE), any(AiResultRequest.class));
    }
    
    // Helper method to convert JSON-like string to XML (simplified)
    private String convertToXml(String json) {
        // Simple conversion for testing
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <AiResultRequest>
                <prediction>MALIGNANT</prediction>
                <prob_malignant>0.96</prob_malignant>
            </AiResultRequest>
            """;
    }
}