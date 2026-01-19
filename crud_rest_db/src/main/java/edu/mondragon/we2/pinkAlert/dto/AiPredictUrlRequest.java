package edu.mondragon.we2.pinkAlert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiPredictUrlRequest {

    @JsonProperty("diagnosis_id")
    private String diagnosis_id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("dicom_url")
    private String dicom_url;

    public AiPredictUrlRequest() {
    }

    public AiPredictUrlRequest(String diagnosis_id, String email, String dicom_url) {
        this.diagnosis_id = diagnosis_id;
        this.email = email;
        this.dicom_url = dicom_url;
    }

    public String getDiagnosis_id() {
        return diagnosis_id;
    }

    public void setDiagnosis_id(String diagnosis_id) {
        this.diagnosis_id = diagnosis_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDicom_url() {
        return dicom_url;
    }

    public void setDicom_url(String dicom_url) {
        this.dicom_url = dicom_url;
    }
}
