package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.mondragon.we2.pinkAlert.model.SimTime;

class SimTimeTest {

    @Test
    void testSimTimeCreation() {
        // Given
        long time = 1000000000L; // 1 second in nanoseconds
        int hours = 1;
        int minutes = 30;
        int seconds = 45;

        // When
        SimTime simTime = new SimTime(time, hours, minutes, seconds);

        // Then
        assertEquals(time, simTime.getTime());
        assertEquals(hours, simTime.getHours());
        assertEquals(minutes, simTime.getMinutes());
        assertEquals(seconds, simTime.getSeconds());
    }

    @Test
    void testSimTimeWithZeroValues() {
        SimTime simTime = new SimTime(0, 0, 0, 0);
        
        assertEquals(0, simTime.getTime());
        assertEquals(0, simTime.getHours());
        assertEquals(0, simTime.getMinutes());
        assertEquals(0, simTime.getSeconds());
    }

    @Test
    void testSimTimeWithNegativeValues() {
        // Testing boundary values
        SimTime simTime = new SimTime(-1, -1, -1, -1);
        
        assertEquals(-1, simTime.getTime());
        assertEquals(-1, simTime.getHours());
        assertEquals(-1, simTime.getMinutes());
        assertEquals(-1, simTime.getSeconds());
    }

    @Test
    void testSimTimeWithMaximumValues() {
        // Testing with maximum reasonable values
        SimTime simTime = new SimTime(Long.MAX_VALUE, 23, 59, 59);
        
        assertEquals(Long.MAX_VALUE, simTime.getTime());
        assertEquals(23, simTime.getHours());
        assertEquals(59, simTime.getMinutes());
        assertEquals(59, simTime.getSeconds());
    }

    @Test
    void testSimTimeSerialization() throws Exception {
        // Test JSON serialization
        ObjectMapper mapper = new ObjectMapper();
        SimTime simTime = new SimTime(123456789L, 2, 15, 30);
        
        String json = mapper.writeValueAsString(simTime);
        
        assertTrue(json.contains("\"time\":123456789"));
        assertTrue(json.contains("\"hours\":2"));
        assertTrue(json.contains("\"minutes\":15"));
        assertTrue(json.contains("\"seconds\":30"));
    }
}