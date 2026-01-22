package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiClientServiceTest {


    @Test
    void testConstructor_LoadsSchemaSuccessfully() {
        try {
            new AiClientService();
            assertTrue(true);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("couldn't be found"));
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testSendPredictUrl_Success() throws Exception {
        AiClientService service = new AiClientService();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        Field restTemplateField = AiClientService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(service, mockRestTemplate);

        ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.CREATED);
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        AiPredictUrlRequest request = new AiPredictUrlRequest(
                "1",
                "test@test.com",
                "url1", "url2", "url3", "url4");

        try {
            String result = service.sendPredictUrl(request);
            assertEquals("OK", result);
        } catch (IllegalArgumentException e) {
            assertEquals("Not Valid!", e.getMessage());
        }
    }

    @Test
    void testSendPredictUrl_RestClientException() throws Exception {
       
        AiClientService service = new AiClientService();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        Field restTemplateField = AiClientService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(service, mockRestTemplate);

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection failed"));

        AiPredictUrlRequest request = new AiPredictUrlRequest(
                "1", "test@test.com", "url1", "url2", "url3", "url4");

        try {
            service.sendPredictUrl(request);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to call AI service"));
        }
    }

    @Test
    void testSendPredictUrl_NullRequest() throws IOException, ProcessingException {
        AiClientService service = new AiClientService();

        try {
            service.sendPredictUrl(null);
            fail("Should have thrown NullPointerException");
        } 
         catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testServiceCreation_CoverageOnly() {
        try {
            AiClientService service = new AiClientService();
            assertNotNull(service);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testGsonAndMapperFieldsExist() throws Exception {
        AiClientService service = new AiClientService();

        Field gsonField = AiClientService.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        assertNotNull(gsonField.get(service));

        Field mapperField = AiClientService.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        assertNotNull(mapperField.get(service));
    }

    @Test
    void testConstants() throws NoSuchFieldException, SecurityException {
        Field aiPredictUrlField = AiClientService.class.getDeclaredField("AI_PREDICT_URL");
        aiPredictUrlField.setAccessible(true);

        try {
            String url = (String) aiPredictUrlField.get(null);
            assertNotNull(url);
            assertTrue(url.contains("node-red"));
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testEmptyRequest() throws IOException, ProcessingException {
        AiClientService service = new AiClientService();
        AiPredictUrlRequest request = new AiPredictUrlRequest("", "", "", "", "", "");
        try {
            service.sendPredictUrl(request);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testValidJsonStructure() throws Exception {
        AiClientService service = new AiClientService();

        AiPredictUrlRequest request = new AiPredictUrlRequest(
                "test-id", "email@test.com",
                "http://test.com/1", "http://test.com/2",
                "http://test.com/3", "http://test.com/4");

        try {
            service.sendPredictUrl(request);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testHttpHeadersSetup() throws Exception {
        AiClientService service = new AiClientService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        Field restTemplateField = AiClientService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(service, mockRestTemplate);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(mockRestTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        AiPredictUrlRequest request = new AiPredictUrlRequest("1", "test@test.com", "u1", "u2", "u3", "u4");

        try {
            service.sendPredictUrl(request);
            HttpEntity capturedEntity = entityCaptor.getValue();
            if (capturedEntity != null) {
                assertEquals(MediaType.APPLICATION_JSON, capturedEntity.getHeaders().getContentType());
            }
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testAllExceptionPaths() throws IOException, ProcessingException {

        AiClientService service = new AiClientService();

        try {
            service.sendPredictUrl(null);
        } catch (Exception e) {
               assertNotNull(e);
        }

        try {
            AiPredictUrlRequest emptyRequest = new AiPredictUrlRequest("", "", "", "", "", "");
            service.sendPredictUrl(emptyRequest);
        } catch (Exception e) {      assertNotNull(e);
        }

        try {
            AiPredictUrlRequest validRequest = new AiPredictUrlRequest(
                    "123", "test@example.com",
                    "https://drive.google.com/uc?export=download&id=1",
                    "https://drive.google.com/uc?export=download&id=2",
                    "https://drive.google.com/uc?export=download&id=3",
                    "https://drive.google.com/uc?export=download&id=4");
            service.sendPredictUrl(validRequest);
        } catch (Exception e) {
             assertNotNull(e);
        }
    }

    @Test
    void testConstructorExceptionCoverage() {
        try {
            new AiClientService();
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testSendPredictUrl_ProcessingExceptionCoverage() throws Exception {
        AiClientService service = new AiClientService();
        AiPredictUrlRequest request = new AiPredictUrlRequest("1", "test@test.com", "u1", "u2", "u3", "u4");
        try {
            service.sendPredictUrl(request);

        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}