package edu.mondragon.we2.pinkAlert.model;


import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testEnumValues() {
        assertEquals(Role.DOCTOR, Role.valueOf("DOCTOR"));
        assertEquals(Role.PATIENT, Role.valueOf("PATIENT"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }
}
