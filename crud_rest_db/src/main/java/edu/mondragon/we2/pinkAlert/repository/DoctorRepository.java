package edu.mondragon.we2.pinkAlert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.pinkAlert.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
