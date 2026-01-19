package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testEnumValues() {
        Role[] roles = Role.values();
        
        assertEquals(3, roles.length);
        assertEquals(Role.DOCTOR, roles[0]);
        assertEquals(Role.PATIENT, roles[1]);
        assertEquals(Role.ADMIN, roles[2]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(Role.DOCTOR, Role.valueOf("DOCTOR"));
        assertEquals(Role.PATIENT, Role.valueOf("PATIENT"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    void testEnumToString() {
        assertEquals("DOCTOR", Role.DOCTOR.toString());
        assertEquals("PATIENT", Role.PATIENT.toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, Role.DOCTOR.ordinal());
        assertEquals(1, Role.PATIENT.ordinal());
        assertEquals(2, Role.ADMIN.ordinal());
    }
}