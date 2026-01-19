package edu.mondragon.we2.pinkAlert.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private Notification notification;
    private final String EMAIL = "test@example.com";
    private final String TOPIC = "Test Topic";
    private final String MESSAGE = "Test Message";
    private final String DATE = "2024-01-01";

    @BeforeEach
    void setUp() {
        notification = new Notification();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(notification);
        assertNull(notification.getEmail());
        assertNull(notification.getTopic());
        assertNull(notification.getMessage());
        assertNull(notification.getDate());
    }

    @Test
    void testParameterizedConstructor() {
        Notification paramNotification = new Notification(EMAIL, TOPIC, MESSAGE, DATE);
        
        assertEquals(EMAIL, paramNotification.getEmail());
        assertEquals(TOPIC, paramNotification.getTopic());
        assertEquals(MESSAGE, paramNotification.getMessage());
        assertEquals(DATE, paramNotification.getDate());
    }

    @Test
    void testSetterAndGetters() {
        notification.setEmail(EMAIL);
        notification.setTopic(TOPIC);
        notification.setMessage(MESSAGE);
        notification.setDate(DATE);

        assertEquals(EMAIL, notification.getEmail());
        assertEquals(TOPIC, notification.getTopic());
        assertEquals(MESSAGE, notification.getMessage());
        assertEquals(DATE, notification.getDate());
    }

    @Test
    void testSettersWithNullValues() {
        notification.setEmail(null);
        notification.setTopic(null);
        notification.setMessage(null);
        notification.setDate(null);

        assertNull(notification.getEmail());
        assertNull(notification.getTopic());
        assertNull(notification.getMessage());
        assertNull(notification.getDate());
    }

    @Test
    void testSettersWithEmptyStrings() {
        notification.setEmail("");
        notification.setTopic("");
        notification.setMessage("");
        notification.setDate("");

        assertEquals("", notification.getEmail());
        assertEquals("", notification.getTopic());
        assertEquals("", notification.getMessage());
        assertEquals("", notification.getDate());
    }
}