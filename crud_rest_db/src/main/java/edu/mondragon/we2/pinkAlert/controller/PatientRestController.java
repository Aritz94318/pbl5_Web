package edu.mondragon.we2.pinkalert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.mondragon.we2.pinkalert.model.Diagnosis;
import edu.mondragon.we2.pinkalert.model.Patient;
import edu.mondragon.we2.pinkalert.service.DiagnosisService;
import edu.mondragon.we2.pinkalert.service.PatientService;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientRestController {

    private final PatientService patientService;
    private final DiagnosisService diagnosisService;

    public PatientRestController(PatientService patientService,
            DiagnosisService diagnosisService) {
        this.patientService = patientService;
        this.diagnosisService = diagnosisService;
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.findAll();
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Integer id) {
        return patientService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody Patient patient) {
        Patient created = patientService.create(patient);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Integer id,
            @RequestBody Patient patient) {
        Patient updated = patientService.update(id, patient);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/diagnoses")
    public List<Diagnosis> getDiagnosesByPatient(@PathVariable Integer id) {
        patientService.findById(id);
        return diagnosisService.findByPatient(id);
    }
}
