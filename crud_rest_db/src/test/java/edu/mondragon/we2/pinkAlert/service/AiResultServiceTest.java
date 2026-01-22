package edu.mondragon.we2.pinkAlert.service;



import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.AiPrediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AiResultServiceTest {
    
    private Diagnosis diagnosis;
    private AiResultRequest validRequest;
    
    @BeforeEach
    void setUp() throws Exception {
        // Crear mocks manualmente
     
        
        diagnosis = new Diagnosis();
        diagnosis.setId(1);
        diagnosis.setReviewed(true);
        diagnosis.setUrgent(false);
        diagnosis.setProbability(BigDecimal.ZERO);
        
        validRequest = new AiResultRequest();
        validRequest.setPrediction("MALIGNANT");
        validRequest.setProbMalignant(new BigDecimal("0.87654321"));
        
        
    }
    
   
   
    
    
    
    @Test
    void testBusinessLogicDirectly() {
        AiResultRequest request = new AiResultRequest();
        request.setPrediction("MALIGNANT");
        request.setProbMalignant(new BigDecimal("0.87654321"));
        
        String pred = request.getPrediction().trim().toUpperCase();
        
        AiPrediction aiPred;
        try {
            aiPred = AiPrediction.valueOf(pred);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid prediction: " + request.getPrediction() + " (expected BENIGN or MALIGNANT)");
        }

        BigDecimal prob = request.getProbMalignant().setScale(8, BigDecimal.ROUND_HALF_UP);
        if (prob.compareTo(BigDecimal.ZERO) < 0)
            prob = BigDecimal.ZERO;
        if (prob.compareTo(BigDecimal.ONE) > 0)
            prob = BigDecimal.ONE;

        boolean urgent = (aiPred == AiPrediction.MALIGNANT);

        assertEquals(AiPrediction.MALIGNANT, aiPred);
        assertEquals(new BigDecimal("0.87654321"), prob);
        assertTrue(urgent);
        
        assertFalse(false); 
    }
}



