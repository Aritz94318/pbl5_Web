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
    private AiResultRequest validRequest;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiWebhookController).build();
       
        
        validRequest = new AiResultRequest();
        validRequest.setPrediction("MALIGNANT");
        validRequest.setProbMalignant(new BigDecimal("0.95"));
    }
    
    @Test
    void applyAiResult_Success_MalignantPrediction() throws Exception {
       
        Integer diagnosisId = 123;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.95
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_Success_BenignPrediction() throws Exception {
   
        Integer diagnosisId = 456;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.15
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_Success_XMLContent() throws Exception {

        Integer diagnosisId = 789;
        String xmlRequest = """
            <?xml version="1.0" encoding="UTF-8"?>
            <AiResultRequest>
                <prediction>MALIGNANT</prediction>
                <prob_malignant>0.87</prob_malignant>
            </AiResultRequest>
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithHighPrecisionProbability() throws Exception {
        Integer diagnosisId = 111;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.999876
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithVeryLowProbability() throws Exception {

        Integer diagnosisId = 222;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.000123
            }
            """;
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MissingPredictionField() throws Exception {
        Integer diagnosisId = 333;
        String jsonRequest = """
            {
                "prob_malignant": 0.75
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); 
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MissingProbabilityField() throws Exception {
 
        Integer diagnosisId = 444;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT"
            }
            """;
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_InvalidPredictionValue() throws Exception {
        
        Integer diagnosisId = 555;
        String jsonRequest = """
            {
                "prediction": "INVALID_VALUE",
                "prob_malignant": 0.5
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); 
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
 
    @Test
    void applyAiResult_EmptyRequestBody() throws Exception {
    
        Integer diagnosisId = 888;
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest()); 
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_MalformedJSON() throws Exception {
        
        Integer diagnosisId = 999;
        String malformedJson = "{ malformed json ";
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_ServiceThrowsException() throws Exception {
      
        Integer diagnosisId = 101;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.92
            }
            """;
        
        doThrow(new RuntimeException("Database error"))
            .when(aiResultService).applyAiResult(eq(diagnosisId), any());
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_ServiceThrowsIllegalArgumentException() throws Exception {
  
        Integer diagnosisId = 202;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.92
            }
            """;
        
        doThrow(new IllegalArgumentException("Diagnosis not found"))
            .when(aiResultService).applyAiResult(eq(diagnosisId), any());
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithIntegerProbability() throws Exception {
   
        Integer diagnosisId = 303;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_WithProbabilityOne() throws Exception {
   
        Integer diagnosisId = 404;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 1
            }
            """;
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    

  
    @Test
    void applyAiResult_NullValuesInRequest() throws Exception {
     
        Integer diagnosisId = 707;
        String jsonRequest = """
            {
                "prediction": null,
                "prob_malignant": null
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_AdditionalFieldsInJSON() throws Exception {
       
        Integer diagnosisId = 808;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.25,
                "extra_field": "should be ignored",
                "another_field": 123
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_DirectControllerMethodTest() throws IOException, ProcessingException {
    
        Integer diagnosisId = 909;
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.91"));

        ResponseEntity<Void> response = aiWebhookController.applyAiResult(diagnosisId, request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResultService).applyAiResult(diagnosisId, request);
    }
    
    @Test
    void applyAiResult_DirectControllerMethodTest_Exception() throws IOException, ProcessingException {
       
        Integer diagnosisId = 1010;
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("BENIGN");
        request.setProbMalignant(new BigDecimal("0.12"));
        
        doThrow(new RuntimeException("Test exception"))
            .when(aiResultService).applyAiResult(diagnosisId, request);
        
        ResponseEntity<Void> response = aiWebhookController.applyAiResult(diagnosisId, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(aiResultService).applyAiResult(diagnosisId, request);
    }
    
    @Test
    void applyAiResult_WithDifferentContentTypes() throws Exception {
    
        Integer diagnosisId = 1111;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.96
            }
            """;
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
                    .content(contentType.contains("xml") ? convertToXml() : jsonRequest))
                    .andExpect(status().isOk());
        }
        
        verify(aiResultService, times(contentTypes.length))
            .applyAiResult(eq(diagnosisId), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_InvalidDiagnosisIdInURL() throws Exception {
   
        String invalidId = "not-a-number";
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.85
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()); 
        
        verify(aiResultService, never()).applyAiResult(anyInt(), any());
    }
    
    @Test
    void applyAiResult_DiagnosisIdZero() throws Exception {
        Integer diagnosisId = 0;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.1
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(0), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_DiagnosisIdNegative() throws Exception {

        Integer diagnosisId = -1;
        String jsonRequest = """
            {
                "prediction": "MALIGNANT",
                "prob_malignant": 0.8
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk()); 
        
        verify(aiResultService).applyAiResult(eq(-1), any(AiResultRequest.class));
    }
    
    @Test
    void applyAiResult_MaxIntegerId() throws Exception {

        Integer diagnosisId = Integer.MAX_VALUE;
        String jsonRequest = """
            {
                "prediction": "BENIGN",
                "prob_malignant": 0.05
            }
            """;
        
        mockMvc.perform(put("/diagnoses/{id}/ai-result", diagnosisId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
        
        verify(aiResultService).applyAiResult(eq(Integer.MAX_VALUE), any(AiResultRequest.class));
    }
    
    private String convertToXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <AiResultRequest>
                <prediction>MALIGNANT</prediction>
                <prob_malignant>0.96</prob_malignant>
            </AiResultRequest>
            """;
    }
}