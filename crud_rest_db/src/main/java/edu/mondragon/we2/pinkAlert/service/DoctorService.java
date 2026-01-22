package edu.mondragon.we2.pinkAlert.service;

import org.springframework.stereotype.Service;

import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(Integer id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id " + id));
    }

    public Doctor create(Doctor doctor) {
        doctor.setId(null);
        return doctorRepository.save(doctor);
    }

    public Doctor update(Integer id, Doctor updated) {
        Doctor existing = findById(id);
        return doctorRepository.save(existing);
    }

    public void delete(Integer id) {
        doctorRepository.deleteById(id);
    }
}
