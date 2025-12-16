package edu.mondragon.we2.pinkAlert.repository;

import edu.mondragon.we2.pinkAlert.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
