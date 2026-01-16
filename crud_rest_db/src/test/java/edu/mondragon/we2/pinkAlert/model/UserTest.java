package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getUsername());
        assertNull(user.getFullName());
        assertNull(user.getPasswordHash());
        assertNull(user.getRole());
        assertNull(user.getDoctor());
        assertNull(user.getPatient());
    }

    @Test
    void testSetterAndGetters() {
        Integer id = 1;
        String email = "test@example.com";
        String username = "testuser";
        String fullName = "Test User";
        String passwordHash = "hashedpassword";
        Role role = Role.PATIENT;
        Doctor doctor = new Doctor();
        Patient patient = new Patient();

        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setDoctor(doctor);
        user.setPatient(patient);

        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(username, user.getUsername());
        assertEquals(fullName, user.getFullName());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(role, user.getRole());
        assertEquals(doctor, user.getDoctor());
        assertEquals(patient, user.getPatient());
    }

    @Test
    void testLinkPatient() {
        Patient patient = new Patient();
        user.linkPatient(patient);

        assertEquals(patient, user.getPatient());
        assertEquals(user, patient.getUser());
    }

    @Test
    void testLinkPatientWithNull() {
        user.linkPatient(null);
        assertNull(user.getPatient());
    }

    @Test
    void testUnlinkPatient() {
        Patient patient = new Patient();
        user.linkPatient(patient);
        
        user.unlinkPatient();
        
        assertNull(user.getPatient());
        assertNull(patient.getUser());
    }

    @Test
    void testUnlinkPatientWhenNoPatient() {
        // Should not throw exception
        user.unlinkPatient();
        assertNull(user.getPatient());
    }

    @Test
    void testUnlinkDoctor() {
        Doctor doctor = new Doctor();
        user.setDoctor(doctor);
        
        user.unlinkDoctor();
        
        assertNull(user.getDoctor());
    }

    @Test
    void testUnlinkDoctorWhenNoDoctor() {
        // Should not throw exception
        user.unlinkDoctor();
        assertNull(user.getDoctor());
    }

    @Test
    void testSettersWithNullValues() {
        user.setId(null);
        user.setEmail(null);
        user.setUsername(null);
        user.setFullName(null);
        user.setPasswordHash(null);
        user.setRole(null);
        user.setDoctor(null);
        user.setPatient(null);

        assertAll(
            () -> assertNull(user.getId()),
            () -> assertNull(user.getEmail()),
            () -> assertNull(user.getUsername()),
            () -> assertNull(user.getFullName()),
            () -> assertNull(user.getPasswordHash()),
            () -> assertNull(user.getRole()),
            () -> assertNull(user.getDoctor()),
            () -> assertNull(user.getPatient())
        );
    }
}