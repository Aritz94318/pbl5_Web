package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimEventTest {

    @Test
    void testSimEventCreation() {
        // Given
        String actor = "PATIENT";
        int actorId = 1;
        String text = "Patient arrived";
        long ts = 1234567890L;

        // When
        SimEvent event = new SimEvent(actor, actorId, text, ts);

        // Then
        assertEquals(actor, event.actor());
        assertEquals(actorId, event.actorId());
        assertEquals(text, event.text());
        assertEquals(ts, event.ts());
    }

    @Test
    void testSimEventWithZeroTimestamp() {
        // Given
        SimEvent event = new SimEvent("DOCTOR", 2, "Doctor assigned", 0);

        // Then
        assertEquals("DOCTOR", event.actor());
        assertEquals(2, event.actorId());
        assertEquals("Doctor assigned", event.text());
        assertEquals(0, event.ts());
    }

    @Test
    void testSimEventDifferentActors() {
        // Test all actor types
        SimEvent patient = new SimEvent("PATIENT", 1, "Test", 1000L);
        SimEvent doctor = new SimEvent("DOCTOR", 2, "Test", 1000L);
        SimEvent machine = new SimEvent("MACHINE", 3, "Test", 1000L);

        assertEquals("PATIENT", patient.actor());
        assertEquals("DOCTOR", doctor.actor());
        assertEquals("MACHINE", machine.actor());
    }

    @Test
    void testSimEventEquality() {
        SimEvent event1 = new SimEvent("PATIENT", 1, "Arrived", 1000L);
        SimEvent event2 = new SimEvent("PATIENT", 1, "Arrived", 1000L);
        SimEvent event3 = new SimEvent("DOCTOR", 1, "Arrived", 1000L);

        // Test equals (record provides this automatically)
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
    }

    @Test
    void testSimEventToString() {
        SimEvent event = new SimEvent("MACHINE", 5, "Scan completed", 987654321L);
        String toString = event.toString();

        assertTrue(toString.contains("MACHINE"));
        assertTrue(toString.contains("5"));
        assertTrue(toString.contains("Scan completed"));
        assertTrue(toString.contains("987654321"));
    }
}