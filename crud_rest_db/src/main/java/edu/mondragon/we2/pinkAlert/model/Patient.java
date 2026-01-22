
package edu.mondragon.we2.pinkAlert.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "Patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PatientID")
    private Integer id;

    @Column(name = "BirthDate", nullable = false)
    private LocalDate birthDate;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Diagnosis> diagnoses;

    @Column(length = 30)
    private String phone;

    @OneToOne(mappedBy = "patient", fetch = FetchType.LAZY)
    private User user;

    public Patient() {
    }

    public Patient(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Patient(LocalDate birthDate, String phone) {
        this.birthDate = birthDate;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<Diagnosis> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getAge() {
        if (birthDate == null)
            return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
