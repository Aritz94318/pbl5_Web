package edu.mondragon.we2.pinkalert.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.mondragon.we2.pinkalert.model.Notification;

class NotificationTest {

    @Test
    void testDefaultConstructor() {
        Notification notification = new Notification();
        
        assertNull(notification.getEmail());
        assertNull(notification.getTopic());
        assertNull(notification.getMessage());
        assertNull(notification.getDate());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String email = "test@example.com";
        String topic = "Appointment Reminder";
        String message = "Your appointment is tomorrow";
        String date = "2024-01-15";

        // When
        Notification notification = new Notification(email, topic, message, date);

        // Then
        assertEquals(email, notification.getEmail());
        assertEquals(topic, notification.getTopic());
        assertEquals(message, notification.getMessage());
        assertEquals(date, notification.getDate());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Notification notification = new Notification();

        // When
        notification.setEmail("doctor@hospital.com");
        notification.setTopic("Test Results");
        notification.setMessage("Your test results are ready");
        notification.setDate("2024-01-16");

        // Then
        assertEquals("doctor@hospital.com", notification.getEmail());
        assertEquals("Test Results", notification.getTopic());
        assertEquals("Your test results are ready", notification.getMessage());
        assertEquals("2024-01-16", notification.getDate());
    }

    @Test
    void testEmptyStrings() {
        Notification notification = new Notification("", "", "", "");
        
        assertEquals("", notification.getEmail());
        assertEquals("", notification.getTopic());
        assertEquals("", notification.getMessage());
        assertEquals("", notification.getDate());
    }

    @Test
    void testNullValues() {
        Notification notification = new Notification(null, null, null, null);
        
        assertNull(notification.getEmail());
        assertNull(notification.getTopic());
        assertNull(notification.getMessage());
        assertNull(notification.getDate());
    }

    @Test
    void testLongStrings() {
        String longEmail = "very.long.email.address.with.many.characters@example.domain.com";
        String longTopic = "A very long topic name that exceeds normal length for testing purposes";
        String longMessage = "This is a very long message that contains many characters and words to test the handling of lengthy strings in the notification system. It should work without issues.";
        String longDate = "2024-01-15T10:30:00.000Z";
        
        Notification notification = new Notification(longEmail, longTopic, longMessage, longDate);
        
        assertEquals(longEmail, notification.getEmail());
        assertEquals(longTopic, notification.getTopic());
        assertEquals(longMessage, notification.getMessage());
        assertEquals(longDate, notification.getDate());
    }

    @Test
    void testJsonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Notification notification = new Notification("test@test.com", "Alert", "Message", "2024-01-15");
        
        String json = mapper.writeValueAsString(notification);
        
        assertTrue(json.contains("\"email\":\"test@test.com\""));
        assertTrue(json.contains("\"topic\":\"Alert\""));
        assertTrue(json.contains("\"message\":\"Message\""));
        assertTrue(json.contains("\"date\":\"2024-01-15\""));
    }

    @Test
    void testMultipleSetOperations() {
        Notification notification = new Notification();
        
        // Set values multiple times
        notification.setEmail("first@test.com");
        notification.setEmail("second@test.com");
        assertEquals("second@test.com", notification.getEmail());
        
        notification.setTopic("Topic 1");
        notification.setTopic("Topic 2");
        assertEquals("Topic 2", notification.getTopic());
        
        notification.setMessage("Message 1");
        notification.setMessage("Message 2");
        assertEquals("Message 2", notification.getMessage());
        
        notification.setDate("2024-01-01");
        notification.setDate("2024-12-31");
        assertEquals("2024-12-31", notification.getDate());
    }
}