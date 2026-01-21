package edu.mondragon.we2.pinkalert.model;


import org.junit.jupiter.api.Test;

import edu.mondragon.we2.pinkalert.model.Role;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testEnumValues() {
        assertEquals(Role.DOCTOR, Role.valueOf("DOCTOR"));
        assertEquals(Role.PATIENT, Role.valueOf("PATIENT"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }
}
