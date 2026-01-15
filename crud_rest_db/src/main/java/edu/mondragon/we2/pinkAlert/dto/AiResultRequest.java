package edu.mondragon.we2.pinkAlert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class AiResultRequest {

    @JsonProperty("prediction")
    private String prediction; // "MALIGNANT" or "BENIGN"

    @JsonProperty("prob_malignant")
    private BigDecimal probMalignant;


    public AiResultRequest() {
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public BigDecimal getProbMalignant() {
        return probMalignant;
    }

    public void setProbMalignant(BigDecimal probMalignant) {
        this.probMalignant = probMalignant;
    }

}
