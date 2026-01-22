package edu.mondragon.we2.pinkAlert.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import edu.mondragon.we2.pinkAlert.model.SimEvent;
import edu.mondragon.we2.pinkAlert.model.SimTime;
import edu.mondragon.we2.pinkAlert.service.SimulationService;

@ExtendWith(MockitoExtension.class)
class SimControllerTest {

    @Mock
    private SimulationService simulationService;

    @InjectMocks
    private SimController simController;

    @Test
    void testPushEventWithTimestamp() throws IOException, ProcessingException {
       
        SimEvent event = new SimEvent("PATIENT", 1, "Patient arrived", 1234567890L);
        
        ResponseEntity<Void> response = simController.push(event);
        
        assertEquals(ResponseEntity.ok().build(), response);
        verify(simulationService, times(1)).publishValidated(any(SimEvent.class));
    }

    @Test
    void testPushEventWithZeroTimestamp() throws IOException, ProcessingException {

        SimEvent event = new SimEvent("DOCTOR", 2, "Doctor assigned", 0);
        ResponseEntity<Void> response = simController.push(event);

        assertEquals(ResponseEntity.ok().build(), response);
        verify(simulationService, times(1)).publishValidated(any(SimEvent.class));
    }

 
    @Test
    void testReceiveFinalTime() throws IOException, ProcessingException {
      
        SimTime time = new SimTime(1000000000L, 1, 30, 45);
        
        ResponseEntity<Void> response = simController.receiveFinalTime(time);
        
        assertEquals(ResponseEntity.ok().build(), response);
        verify(simulationService, times(1)).publishValidated(time);
    }

    @Test
    void testReceiveFinalTimeWithZeroValues() throws IOException, ProcessingException {
       
        SimTime time = new SimTime(0, 0, 0, 0);
        
        ResponseEntity<Void> response = simController.receiveFinalTime(time);
        
        assertEquals(ResponseEntity.ok().build(), response);
        verify(simulationService, times(1)).publishValidated(time);
    }

    @Test
    void testStream() {
        
        SseEmitter mockEmitter = new SseEmitter();
        when(simulationService.connect()).thenReturn(mockEmitter);

        SseEmitter result = simController.stream();
        
        assertNotNull(result);
        assertEquals(mockEmitter, result);
        verify(simulationService, times(1)).connect();
    }

    @Test
    void testPushEventWithDifferentActors() throws IOException, ProcessingException {
       
        SimEvent[] events = {
            new SimEvent("PATIENT", 1, "Event 1", 1000L),
            new SimEvent("DOCTOR", 2, "Event 2", 2000L),
            new SimEvent("MACHINE", 3, "Event 3", 3000L)
        };
        
        for (SimEvent event : events) {
            ResponseEntity<Void> response = simController.push(event);
            assertEquals(ResponseEntity.ok().build(), response);
        }
        
        verify(simulationService, times(3)).publishValidated(any(SimEvent.class));
    }

    @Test
    void testPushEventExceptionHandling() throws IOException, ProcessingException {
     
        SimEvent event = new SimEvent("PATIENT", 1, "Test", 1000L);
        doThrow(new IOException("Test exception")).when(simulationService).publishValidated(any(SimEvent.class));

        assertThrows(IOException.class, () -> simController.push(event));
    }

    @Test
    void testReceiveFinalTimeExceptionHandling() throws IOException, ProcessingException {

        SimTime time = new SimTime(1000L, 0, 0, 10);
        doThrow(new ProcessingException("Test exception")).when(simulationService).publishValidated(any(SimTime.class));
        assertThrows(ProcessingException.class, () -> simController.receiveFinalTime(time));
    }

    @Test
    void testConstructorInjection() {
        assertNotNull(simController);
        assertNotNull(simulationService);
    }

    @Test
    void testControllerAnnotations() {
        RestController annotation = SimController.class.getAnnotation(RestController.class);
        assertNotNull(annotation, "SimController should have @RestController annotation");
    }
}