package edu.mondragon.we2.pinkAlert.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;

class AiResultRequestTest {

    @Test
    void testDefaultConstructor() {
        AiResultRequest request = new AiResultRequest();
        
        assertNull(request.getPrediction());
        assertNull(request.getProbMalignant());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String prediction = "MALIGNANT";
        BigDecimal probability = new BigDecimal("0.85");

        // When
        AiResultRequest request = new AiResultRequest(prediction, probability);

        // Then
        assertEquals(prediction, request.getPrediction());
        assertEquals(probability, request.getProbMalignant());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        AiResultRequest request = new AiResultRequest();

        // When
        request.setPrediction("BENIGN");
        request.setProbMalignant(new BigDecimal("0.15"));

        // Then
        assertEquals("BENIGN", request.getPrediction());
        assertEquals(new BigDecimal("0.15"), request.getProbMalignant());
    }

    @Test
    void testMalignantPrediction() {
        AiResultRequest request = new AiResultRequest("MALIGNANT", new BigDecimal("0.95"));
        
        assertEquals("MALIGNANT", request.getPrediction());
        assertEquals(0, new BigDecimal("0.95").compareTo(request.getProbMalignant()));
    }

    @Test
    void testBenignPrediction() {
        AiResultRequest request = new AiResultRequest("BENIGN", new BigDecimal("0.05"));
        
        assertEquals("BENIGN", request.getPrediction());
        assertEquals(0, new BigDecimal("0.05").compareTo(request.getProbMalignant()));
    }

    @Test
    void testEdgeCaseProbabilities() {
        // Test boundary probabilities
        AiResultRequest zeroProb = new AiResultRequest("BENIGN", BigDecimal.ZERO);
        AiResultRequest maxProb = new AiResultRequest("MALIGNANT", BigDecimal.ONE);
        AiResultRequest middleProb = new AiResultRequest("MALIGNANT", new BigDecimal("0.5"));
        
        assertEquals(BigDecimal.ZERO, zeroProb.getProbMalignant());
        assertEquals(BigDecimal.ONE, maxProb.getProbMalignant());
        assertEquals(0, new BigDecimal("0.5").compareTo(middleProb.getProbMalignant()));
    }

    @Test
    void testInvalidPredictionValues() {
        AiResultRequest request = new AiResultRequest();
        
        // Test with non-standard prediction values
        request.setPrediction("UNCERTAIN");
        request.setProbMalignant(new BigDecimal("0.5"));
        
        assertEquals("UNCERTAIN", request.getPrediction());
        assertEquals(0, new BigDecimal("0.5").compareTo(request.getProbMalignant()));
    }

    @Test
    void testNullValues() {
        AiResultRequest request = new AiResultRequest(null, null);
        
        assertNull(request.getPrediction());
        assertNull(request.getProbMalignant());
    }

    @Test
    void testJsonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AiResultRequest request = new AiResultRequest("MALIGNANT", new BigDecimal("0.8765"));
        
        String json = mapper.writeValueAsString(request);
        
        assertTrue(json.contains("\"prediction\":\"MALIGNANT\""));
        assertTrue(json.contains("\"prob_malignant\":0.8765"));
    }

    @Test
    void testPrecisionHandling() {
        // Test with high precision decimals
        BigDecimal highPrecision = new BigDecimal("0.12345678901234567890");
        AiResultRequest request = new AiResultRequest("MALIGNANT", highPrecision);
        
        assertEquals(0, highPrecision.compareTo(request.getProbMalignant()));
    }

    @Test
    void testProbabilityRange() {
        // Test probabilities outside 0-1 range
        AiResultRequest negativeProb = new AiResultRequest();
        negativeProb.setProbMalignant(new BigDecimal("-0.1"));
        
        AiResultRequest overOneProb = new AiResultRequest();
        overOneProb.setProbMalignant(new BigDecimal("1.1"));
        
        assertEquals(0, new BigDecimal("-0.1").compareTo(negativeProb.getProbMalignant()));
        assertEquals(0, new BigDecimal("1.1").compareTo(overOneProb.getProbMalignant()));
    }
}