package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.AiPrediction;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AiResultService {

    private final DiagnosisRepository diagnosisRepository;

    public AiResultService(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    @Transactional
    public Diagnosis applyAiResult(AiResultRequest req) {
        if (req.getDiagnosisId() == null) {
            throw new IllegalArgumentException("diagnosis_id is required");
        }
        if (req.getPrediction() == null || req.getPrediction().isBlank()) {
            throw new IllegalArgumentException("prediction is required");
        }
        if (req.getProbMalignant() == null) {
            throw new IllegalArgumentException("prob_malignant is required");
        }

        Diagnosis d = diagnosisRepository.findById(req.getDiagnosisId())
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + req.getDiagnosisId()));

        String pred = req.getPrediction().trim().toUpperCase();

        // 1) Map prediction -> enum
        AiPrediction aiPred;
        try {
            aiPred = AiPrediction.valueOf(pred);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid prediction: " + req.getPrediction() + " (expected BENIGN or MALIGNANT)");
        }

        // 2) Normalize probability to DECIMAL(10,8)
        BigDecimal prob = req.getProbMalignant().setScale(8, RoundingMode.HALF_UP);
        if (prob.compareTo(BigDecimal.ZERO) < 0)
            prob = BigDecimal.ZERO;
        if (prob.compareTo(BigDecimal.ONE) > 0)
            prob = BigDecimal.ONE;

        // 3) Keep your UI sorting behavior
        boolean urgent = (aiPred == AiPrediction.MALIGNANT);

        // 4) Persist
        d.setAiPrediction(aiPred);
        d.setUrgent(urgent);
        d.setProbability(prob);

        // AI result does NOT mean doctor reviewed
        d.setReviewed(false);

        return diagnosisRepository.save(d);
    }

}
