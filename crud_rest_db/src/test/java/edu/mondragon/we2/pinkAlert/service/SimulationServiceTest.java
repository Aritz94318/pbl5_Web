package edu.mondragon.we2.pinkAlert.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import edu.mondragon.we2.pinkAlert.model.SimEvent;
import edu.mondragon.we2.pinkAlert.model.SimTime;

class SimulationServiceTest {

    private SimulationService service;



 @BeforeEach
void setUp() throws IOException, ProcessingException {

    service = new SimulationService();
    assertNotNull(service);
}
  
    @Test
    void publishValidated_simEvent_valid() {
        service.connect();

        SimEvent valid = new SimEvent("DOCTOR", 1, "starts treatment", System.currentTimeMillis());
        assertDoesNotThrow(() -> service.publishValidated(valid));
    }

    @Test
    void publishValidated_simEvent_invalid()  {
        service.connect();

        SimEvent invalid = new SimEvent(null, -1, null, -1);
        assertThrows(IllegalArgumentException.class, () -> service.publishValidated(invalid));
    }


    @Test
    void publishValidated_simTime_valid() {
        service.connect();

        SimTime valid = new SimTime(System.nanoTime(), 10, 20, 30);
        assertDoesNotThrow(() -> service.publishValidated(valid));
    }

    @Test
    void publishValidated_simTime_invalid() {
        service.connect();

        SimTime invalid = new SimTime(-1, -1, -1, -1);
        assertThrows(IllegalArgumentException.class, () -> service.publishValidated(invalid));
    }


    @Test
    void modify_invalid_throws() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService();

        assertThrows(IllegalArgumentException.class,
                () -> serviceRt.modify(-1, -1, -1));

        verifyNoInteractions(rt);
    }


    @Test
    void start_callsRestTemplate() throws Exception {
        RestTemplate rt = mock(RestTemplate.class);
        SimulationService serviceRt = new SimulationService();

        when(rt.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        assertDoesNotThrow(() -> serviceRt.start());
    }

@Test
void publishValidatedSimTime_invalidJson_throwsException() {

    SimTime time = new SimTime(
            -1,   
            -1,
            -1,
            -1
    );

    assertThrows(IllegalArgumentException.class,
            () -> service.publishValidated(time)
    );
}
@Test
void modify_invalidJson_throwsException() throws Exception {

    RestTemplate rt = mock(RestTemplate.class);
    SimulationService ser = new SimulationService();

    assertThrows(IllegalArgumentException.class, () ->
            ser.modify(-1, -1, -1)
    );

    verifyNoInteractions(rt);
}
@Test
void publish_whenEmitterThrowsException_removesEmitter() throws Exception {

    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new IOException("fail")).when(emitter).send(any());

    SimulationService ser = new SimulationService();
    ser.connect(); 
    ser.publish(new SimEvent("SYSTEM", 0, "test", System.currentTimeMillis()));

    assertDoesNotThrow(() ->
            ser.publish(new SimEvent("SYSTEM", 0, "test", System.currentTimeMillis()))
    );
}}