package edu.mondragon.we2.pinkalert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.mondragon.we2.pinkalert.model.Diagnosis;
import edu.mondragon.we2.pinkalert.model.Doctor;
import edu.mondragon.we2.pinkalert.model.Patient;
import edu.mondragon.we2.pinkalert.service.DiagnosisService;
import edu.mondragon.we2.pinkalert.service.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorRestController {

    private final DoctorService doctorService;
    private final DiagnosisService diagnosisService;

    public DoctorRestController(DoctorService doctorService,
            DiagnosisService diagnosisService) {
        this.doctorService = doctorService;
        this.diagnosisService = diagnosisService;
    }

    @GetMapping
    public List<Doctor> getAll() {
        return doctorService.findAll();
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Integer id) {
        return doctorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@RequestBody Doctor doctor) {
        Doctor created = doctorService.create(doctor);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@PathVariable Integer id,
            @RequestBody Doctor doctor) {
        Doctor updated = doctorService.update(id, doctor);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/diagnoses")
    public List<Diagnosis> getDiagnosesByDoctor(@PathVariable Integer id) {
        doctorService.findById(id);
        return diagnosisService.findByDoctor(id);
    }

    @GetMapping("/{id}/patients")
    public List<Patient> getPatientsOfDoctor(@PathVariable Integer id) {
        doctorService.findById(id);

        return diagnosisService.findByDoctor(id).stream()
                .map(Diagnosis::getPatient)
                .distinct()
                .toList();
    }
}
