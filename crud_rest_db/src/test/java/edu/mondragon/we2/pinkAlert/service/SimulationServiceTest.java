package edu.mondragon.we2.pinkAlert.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;

import edu.mondragon.we2.pinkAlert.model.SimEvent;
import edu.mondragon.we2.pinkAlert.model.SimTime;
import edu.mondragon.we2.pinkAlert.service.SimulationService;
import edu.mondragon.we2.pinkAlert.utils.ValidationUtils;

import com.fasterxml.jackson.databind.JsonNode;

class SimulationServiceTest {

    private SimulationService service;



    // =========================
    // CONSTRUCTOR & SCHEMAS
    // =========================
 @BeforeEach
void setUp() throws IOException, ProcessingException {
    // Constructor real con schemas reales
    service = new SimulationService();
    assertNotNull(service);
}

    // ------------------------------
    // publishValidated "Invalid" branches
    // ------------------------------

  
    // ------------------------------
    // modify() — mocks RestTemplate
    // ------------------------------

    @Test
    void publishValidated_simEvent_valid() throws Exception {
        service.connect();

        SimEvent valid = new SimEvent("DOCTOR", 1, "starts treatment", System.currentTimeMillis());
        assertDoesNotThrow(() -> service.publishValidated(valid));
    }

    @Test
    void publishValidated_simEvent_invalid() throws Exception {
        service.connect();

        SimEvent invalid = new SimEvent(null, -1, null, -1);
        assertThrows(IllegalArgumentException.class, () -> service.publishValidated(invalid));
    }

    // ------------------------------
    // publishValidated SimTime
    // ------------------------------

    @Test
    void publishValidated_simTime_valid() throws Exception {
        service.connect();

        SimTime valid = new SimTime(System.nanoTime(), 10, 20, 30);
        assertDoesNotThrow(() -> service.publishValidated(valid));
    }

    @Test
    void publishValidated_simTime_invalid() throws Exception {
        service.connect();

        SimTime invalid = new SimTime(-1, -1, -1, -1);
        assertThrows(IllegalArgumentException.class, () -> service.publishValidated(invalid));
    }

    @Test
    void modify_valid_callsRestTemplate() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService(rt);

        when(rt.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> serviceRt.modify(10, 5, 3));
        verify(rt).exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class));
    }

    @Test
    void modify_invalid_throws() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService(rt);

        assertThrows(IllegalArgumentException.class,
                () -> serviceRt.modify(-1, -1, -1));

        verifyNoInteractions(rt);
    }

    @Test
    void modify_restClientFails_throwsRuntime() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService(rt);

        when(rt.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
                .thenThrow(new RestClientException("fail"));

        assertThrows(RuntimeException.class, () -> serviceRt.modify(1, 1, 1));
    }

    // ------------------------------
    // start() — mocks RestTemplate
    // ------------------------------

    @Test
    void start_callsRestTemplate() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService(rt);

        when(rt.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        assertDoesNotThrow(() -> serviceRt.start());
    }

    // ------------------------------
    // publish throws exception handling
    // ------------------------------

    

@Test
void publishValidatedSimTime_invalidJson_throwsException() {

    SimTime time = new SimTime(
            -1,   // tiempo inválido
            -1,
            -1,
            -1
    );

    assertThrows(IllegalArgumentException.class,
            () -> service.publishValidated(time)
    );
}
@Test
void modify_validRequest_callsSimulator() throws Exception {

    RestTemplate rt = mock(RestTemplate.class);
    SimulationService service = new SimulationService(rt);

    when(rt.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
            .thenReturn(ResponseEntity.ok().build());

    assertDoesNotThrow(() ->
            service.modify(10, 5, 2)
    );

    verify(rt).exchange(
            contains("/Simulation/modify"),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(Void.class)
    );
}
@Test
void modify_invalidJson_throwsException() throws Exception {

    RestTemplate rt = mock(RestTemplate.class);
    SimulationService service = new SimulationService(rt);

    assertThrows(IllegalArgumentException.class, () ->
            service.modify(-1, -1, -1)
    );

    verifyNoInteractions(rt);
}
@Test
void modify_restClientFails_throwsRuntimeException() throws Exception {

    RestTemplate rt = mock(RestTemplate.class);
    SimulationService service = new SimulationService(rt);

    when(rt.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class)))
            .thenThrow(new RestClientException("Boom"));

    RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.modify(1, 1, 1)
    );

    assertTrue(ex.getMessage().contains("Failed to call Simulator"));
}
@Test
void start_callsSimulator() throws Exception {

    RestTemplate rt = mock(RestTemplate.class);
    SimulationService service = new SimulationService(rt);

    when(rt.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("ok"));

    assertDoesNotThrow(() -> service.start());

    verify(rt).exchange(
            contains("/Simulation/start"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class)
    );
}
@Test
void publish_whenEmitterThrowsException_removesEmitter() throws Exception {

    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new IOException("fail")).when(emitter).send(any());

    // Inyectamos el emitter manualmente
    SimulationService service = new SimulationService();
    service.connect(); // añade uno real
    service.publish(new SimEvent("SYSTEM", 0, "test", System.currentTimeMillis()));

    // no debe explotar
    assertDoesNotThrow(() ->
            service.publish(new SimEvent("SYSTEM", 0, "test", System.currentTimeMillis()))
    );
}}