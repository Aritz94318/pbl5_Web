package edu.mondragon.we2.pinkAlert.model;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class GlobalUpdateRequestTest {

    @Test
    void testConstructorAndGetters() {
        // Given
        int patients = 10;
        int doctors = 3;
        int machines = 2;

        // When
        GlobalUpdateRequest request = new GlobalUpdateRequest(patients, doctors, machines);

        // Then
        assertEquals(patients, request.getNumPatients());
        assertEquals(doctors, request.getNumDoctors());
        assertEquals(machines, request.getNumMachines());
    }

    @Test
    void testSetters() {
        // Given
        GlobalUpdateRequest request = new GlobalUpdateRequest(0, 0, 0);

        // When
        request.setNumPatients(5);
        request.setNumDoctors(2);
        request.setNumMachines(1);

        // Then
        assertEquals(5, request.getNumPatients());
        assertEquals(2, request.getNumDoctors());
        assertEquals(1, request.getNumMachines());
    }

    @Test
    void testZeroValues() {
        GlobalUpdateRequest request = new GlobalUpdateRequest(0, 0, 0);
        
        assertEquals(0, request.getNumPatients());
        assertEquals(0, request.getNumDoctors());
        assertEquals(0, request.getNumMachines());
    }

    @Test
    void testNegativeValues() {
        // Testing with negative values
        GlobalUpdateRequest request = new GlobalUpdateRequest(-1, -2, -3);
        
        assertEquals(-1, request.getNumPatients());
        assertEquals(-2, request.getNumDoctors());
        assertEquals(-3, request.getNumMachines());
    }

    @Test
    void testLargeValues() {
        // Testing with large numbers
        GlobalUpdateRequest request = new GlobalUpdateRequest(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, request.getNumPatients());
        assertEquals(Integer.MAX_VALUE, request.getNumDoctors());
        assertEquals(Integer.MAX_VALUE, request.getNumMachines());
    }

    @Test
    void testSetAndGetCombinations() {
        GlobalUpdateRequest request = new GlobalUpdateRequest(1, 2, 3);
        
        // Change values multiple times
        request.setNumPatients(10);
        assertEquals(10, request.getNumPatients());
        
        request.setNumDoctors(5);
        assertEquals(5, request.getNumDoctors());
        
        request.setNumMachines(2);
        assertEquals(2, request.getNumMachines());
        
        // Set back to original
        request.setNumPatients(1);
        request.setNumDoctors(2);
        request.setNumMachines(3);
        assertEquals(1, request.getNumPatients());
        assertEquals(2, request.getNumDoctors());
        assertEquals(3, request.getNumMachines());
    }
}