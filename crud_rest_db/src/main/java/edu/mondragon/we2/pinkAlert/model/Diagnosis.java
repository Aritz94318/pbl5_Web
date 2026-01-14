package edu.mondragon.we2.pinkAlert.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "Diagnosis")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DiagnosisID")
    private Integer id;

    @Column(name = "ImagePath", nullable = false, length = 100)
    private String imagePath;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Description", nullable = false, length = 255)
    private String description;

    @Column(name = "Urgent", nullable = false)
    private boolean urgent;

    @Column(name = "Reviewed", nullable = false)
    private boolean reviewed;

    @Column(name = "Probability", nullable = false, precision = 10, scale = 8)
    private BigDecimal probability = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DoctorID", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PatientID", nullable = false)
    private Patient patient;

    public Diagnosis() {
    }

    public Diagnosis(String imagePath, LocalDate date, String description,
            boolean urgent, Doctor doctor, Patient patient) {
        this.imagePath = imagePath;
        this.date = date;
        this.description = description;
        this.urgent = urgent;
        this.doctor = doctor;
        this.patient = patient;
        this.probability = BigDecimal.ZERO;
        this.reviewed = false;
    }

    // Getters & setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public BigDecimal getProbability() {
        return probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = (probability != null) ? probability : BigDecimal.ZERO;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Transient
    public String getStatus() {
        if (!reviewed)
            return "Pending Review";
        return urgent ? "Malignant" : "Benignant";
    }
}
