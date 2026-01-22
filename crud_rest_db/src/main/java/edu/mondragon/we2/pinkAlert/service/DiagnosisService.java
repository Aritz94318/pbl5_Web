package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.FinalResult;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public List<Diagnosis> findAllSortedByUrgency() {
        return diagnosisRepository.findAllByOrderByUrgentDescDateDesc();
    }

    public List<Diagnosis> findByDateSortedByUrgency(LocalDate date) {
        return diagnosisRepository.findByDateOrderByUrgentDesc(date);
    }

    public List<Diagnosis> findAll() {
        return diagnosisRepository.findAll();
    }

    public Diagnosis findById(Integer id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnosis not found with id " + id));
    }

    public void saveDoctorReview(Integer id, String finalResultRaw, String description, boolean patientNotified) {
        Diagnosis d = diagnosisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + id));

        d.setDescription(description == null ? "" : description.trim());
        d.setPatientNotified(patientNotified);
        // finalResult (allow pending/clear)
        if (finalResultRaw == null || finalResultRaw.isBlank()) {
            d.setFinalResult(null);
            d.setReviewed(false); // optional
        } else {
            FinalResult fr = FinalResult.valueOf(finalResultRaw.trim().toUpperCase());
            d.setFinalResult(fr);
            d.setReviewed(true);

            // urgent policy example:
            if (fr == FinalResult.MALIGNANT || fr == FinalResult.INCONCLUSIVE)
                d.setUrgent(true);
            else
                d.setUrgent(false);
        }

        diagnosisRepository.save(d);
    }

    public long countByPatientId(Integer patientId) {
        return diagnosisRepository.countByPatientId(patientId);
    }

    public Diagnosis create(Diagnosis diagnosis, Integer doctorId, Integer patientId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id " + doctorId));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id " + patientId));

        diagnosis.setId(null);
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);

        return diagnosisRepository.save(diagnosis);
    }

    public Diagnosis update(Integer id, Diagnosis updated, Integer doctorId, Integer patientId) {
        Diagnosis existing = findById(id);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id " + doctorId));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id " + patientId));

        existing.setImagePath(updated.getImagePath());
        existing.setDate(updated.getDate());
        existing.setDescription(updated.getDescription());
        existing.setDoctor(doctor);
        existing.setPatient(patient);

        return diagnosisRepository.save(existing);
    }

    public void delete(Integer id) {
        diagnosisRepository.deleteById(id);
    }

    public List<Diagnosis> findByDoctor(Integer doctorId) {
        return diagnosisRepository.findByDoctor_Id(doctorId);
    }

    public List<Diagnosis> findByPatient(Integer patientId) {
        return diagnosisRepository.findByPatient_Id(patientId);
    }

    public Diagnosis createForDoctorAndPatient(Diagnosis diagnosis,
            Integer doctorId,
            Integer patientId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id " + doctorId));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id " + patientId));

        diagnosis.setId(null);
        diagnosis.setDoctor(doctor);
        diagnosis.setPatient(patient);

        return diagnosisRepository.save(diagnosis);
    }
}
