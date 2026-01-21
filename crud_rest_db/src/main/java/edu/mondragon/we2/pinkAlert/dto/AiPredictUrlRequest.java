package edu.mondragon.we2.pinkalert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiPredictUrlRequest {

    @JsonProperty("diagnosis_id")
    private String diagnosis_id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("dicom_url")
    private String dicom_url;

    @JsonProperty("dicom_url2")
    private String dicom_url2;

    @JsonProperty("dicom_url3")
    private String dicom_url3;

    @JsonProperty("dicom_url4")
    private String dicom_url4;

    public AiPredictUrlRequest() {
    }

    public AiPredictUrlRequest(String diagnosis_id, String email, String dicom_url, String dicom_url2,
            String dicom_url3, String dicom_url4) {
        this.diagnosis_id = diagnosis_id;
        this.email = email;
        this.dicom_url = dicom_url;
        this.dicom_url2 = dicom_url2;
        this.dicom_url3 = dicom_url3;
        this.dicom_url4 = dicom_url4;
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

    public String getDicom_url2() {
        return dicom_url2;
    }

    public void setDicom_url2(String dicom_url2) {
        this.dicom_url2 = dicom_url2;
    }

    public String getDicom_url3() {
        return dicom_url3;
    }

    public void setDicom_url3(String dicom_url3) {
        this.dicom_url3 = dicom_url3;
    }

    public String getDicom_url4() {
        return dicom_url4;
    }

    public void setDicom_url4(String dicom_url4) {
        this.dicom_url4 = dicom_url4;
    }
}