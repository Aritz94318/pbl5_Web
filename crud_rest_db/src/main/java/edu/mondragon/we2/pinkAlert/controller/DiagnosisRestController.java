package edu.mondragon.we2.pinkAlert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.service.DiagnosisService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/diagnoses")
public class DiagnosisRestController {

    private final DiagnosisService diagnosisService;

    public DiagnosisRestController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping
    public List<Diagnosis> getAll() {
        return diagnosisService.findAll();
    }

    @GetMapping("/{id}")
    public Diagnosis getById(@PathVariable Integer id) {
        return diagnosisService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Diagnosis> create(@RequestBody DiagnosisRequest request) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setImagePath(request.getImagePath());
        diagnosis.setDate(request.getDate());
        diagnosis.setDescription(request.getDescription());

        Diagnosis created = diagnosisService.create(
                diagnosis,
                request.getDoctorId(),
                request.getPatientId());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diagnosis> update(@PathVariable Integer id,
            @RequestBody DiagnosisRequest request) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setImagePath(request.getImagePath());
        diagnosis.setDate(request.getDate());
        diagnosis.setDescription(request.getDescription());

        Diagnosis updated = diagnosisService.update(
                id,
                diagnosis,
                request.getDoctorId(),
                request.getPatientId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        diagnosisService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/doctors/{doctorId}/patients/{patientId}/diagnoses")
    public ResponseEntity<Diagnosis> createForDoctorAndPatient(
            @PathVariable Integer doctorId,
            @PathVariable Integer patientId,
            @RequestBody DiagnosisRequest request) {

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setImagePath(request.getImagePath());
        diagnosis.setDate(request.getDate());
        diagnosis.setDescription(request.getDescription());

        Diagnosis created = diagnosisService.createForDoctorAndPatient(diagnosis, doctorId, patientId);
        return ResponseEntity.ok(created);
    }

    public static class DiagnosisRequest {
        private String imagePath;
        private LocalDate date;
        private String description;
        private Integer doctorId;
        private Integer patientId;

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(Integer doctorId) {
            this.doctorId = doctorId;
        }

        public Integer getPatientId() {
            return patientId;
        }

        public void setPatientId(Integer patientId) {
            this.patientId = patientId;
        }
    }
}
