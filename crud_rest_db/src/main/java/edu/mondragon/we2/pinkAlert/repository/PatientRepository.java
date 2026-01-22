package edu.mondragon.we2.pinkAlert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.pinkAlert.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
}
