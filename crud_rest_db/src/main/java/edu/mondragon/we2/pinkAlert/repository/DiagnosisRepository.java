package edu.mondragon.we2.pinkAlert.repository;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Integer> {

    List<Diagnosis> findByDoctor_Id(Integer doctorId);

    List<Diagnosis> findByPatient_Id(Integer patientId);

    List<Diagnosis> findAllByOrderByUrgentDescDateDesc();

    List<Diagnosis> findByDateOrderByUrgentDesc(LocalDate date);

    long countByUrgentTrue();

    long countByReviewedTrue();
}
