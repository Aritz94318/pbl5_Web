package edu.mondragon.we2.pinkAlert.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;



class AiPredictUrlRequestTest {

    @Test
    void testDefaultConstructor() {
        AiPredictUrlRequest request = new AiPredictUrlRequest();
        
        assertNull(request.getDiagnosis_id());
        assertNull(request.getEmail());
        assertNull(request.getDicom_url());
        assertNull(request.getDicom_url2());
        assertNull(request.getDicom_url3());
        assertNull(request.getDicom_url4());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String diagnosisId = "DIAG-123";
        String email = "patient@test.com";
        String dicomUrl = "http://server.com/dicom1";
        String dicomUrl2 = "http://server.com/dicom2";
        String dicomUrl3 = "http://server.com/dicom3";
        String dicomUrl4 = "http://server.com/dicom4";

        // When
        AiPredictUrlRequest request = new AiPredictUrlRequest(diagnosisId, email, dicomUrl, dicomUrl2, dicomUrl3, dicomUrl4);

        // Then
        assertEquals(diagnosisId, request.getDiagnosis_id());
        assertEquals(email, request.getEmail());
        assertEquals(dicomUrl, request.getDicom_url());
        assertEquals(dicomUrl2, request.getDicom_url2());
        assertEquals(dicomUrl3, request.getDicom_url3());
        assertEquals(dicomUrl4, request.getDicom_url4());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        AiPredictUrlRequest request = new AiPredictUrlRequest();

        // When
        request.setDiagnosis_id("DIAG-456");
        request.setEmail("doctor@hospital.com");
        request.setDicom_url("http://test.com/img1");
        request.setDicom_url2("http://test.com/img2");
        request.setDicom_url3("http://test.com/img3");
        request.setDicom_url4("http://test.com/img4");

        // Then
        assertEquals("DIAG-456", request.getDiagnosis_id());
        assertEquals("doctor@hospital.com", request.getEmail());
        assertEquals("http://test.com/img1", request.getDicom_url());
        assertEquals("http://test.com/img2", request.getDicom_url2());
        assertEquals("http://test.com/img3", request.getDicom_url3());
        assertEquals("http://test.com/img4", request.getDicom_url4());
    }

    @Test
    void testPartialUrls() {
        // Test with only some URLs provided
        AiPredictUrlRequest request = new AiPredictUrlRequest("DIAG-001", "test@test.com", 
            "url1", null, "url3", null);
        
        assertEquals("DIAG-001", request.getDiagnosis_id());
        assertEquals("test@test.com", request.getEmail());
        assertEquals("url1", request.getDicom_url());
        assertNull(request.getDicom_url2());
        assertEquals("url3", request.getDicom_url3());
        assertNull(request.getDicom_url4());
    }

    @Test
    void testEmptyStrings() {
        AiPredictUrlRequest request = new AiPredictUrlRequest("", "", "", "", "", "");
        
        assertEquals("", request.getDiagnosis_id());
        assertEquals("", request.getEmail());
        assertEquals("", request.getDicom_url());
        assertEquals("", request.getDicom_url2());
        assertEquals("", request.getDicom_url3());
        assertEquals("", request.getDicom_url4());
    }

    @Test
    void testJsonAnnotations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AiPredictUrlRequest request = new AiPredictUrlRequest("123", "email@test.com", 
            "url1", "url2", "url3", "url4");
        
        String json = mapper.writeValueAsString(request);
        
        // Check JSON property names from annotations
        assertTrue(json.contains("\"diagnosis_id\":\"123\""));
        assertTrue(json.contains("\"email\":\"email@test.com\""));
        assertTrue(json.contains("\"dicom_url\":\"url1\""));
        assertTrue(json.contains("\"dicom_url2\":\"url2\""));
        assertTrue(json.contains("\"dicom_url3\":\"url3\""));
        assertTrue(json.contains("\"dicom_url4\":\"url4\""));
    }

    @Test
    void testUrlValidation() {
        AiPredictUrlRequest request = new AiPredictUrlRequest();
        
        // Test with special characters in URLs
        String complexUrl = "https://server.com/path/to/file.dcm?param=value&token=abc123";
        request.setDicom_url(complexUrl);
        
        assertEquals(complexUrl, request.getDicom_url());
    }

    @Test
    void testEmailFormat() {
        AiPredictUrlRequest request = new AiPredictUrlRequest();
        
        // Test various email formats
        String[] emails = {
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-dash@example.com"
        };
        
        for (String email : emails) {
            request.setEmail(email);
            assertEquals(email, request.getEmail());
        }
    }
}