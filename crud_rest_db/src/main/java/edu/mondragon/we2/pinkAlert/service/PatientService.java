package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id " + id));
    }

    public Patient create(Patient patient) {
        patient.setId(null);
        return patientRepository.save(patient);
    }

    public Patient update(Integer id, Patient updated) {
        Patient existing = findById(id);
        existing.setName(updated.getName());
        existing.setBirthDate(updated.getBirthDate());
        return patientRepository.save(existing);
    }

    public void delete(Integer id) {
        patientRepository.deleteById(id);
    }
}
