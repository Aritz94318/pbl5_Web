package edu.mondragon.we2.pinkAlert.repository;

import edu.mondragon.we2.pinkAlert.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
}
