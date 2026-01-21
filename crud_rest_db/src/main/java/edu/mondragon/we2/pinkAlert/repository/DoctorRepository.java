package edu.mondragon.we2.pinkalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.pinkalert.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
