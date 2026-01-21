package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.AiPrediction;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.repository.DiagnosisRepository;
import edu.mondragon.we2.pinkAlert.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AiResultService {

    private final DiagnosisRepository diagnosisRepository;
    private final Gson gson = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchema schema;

    public AiResultService(DiagnosisRepository diagnosisRepository) throws IOException, ProcessingException {
        this.diagnosisRepository = diagnosisRepository;
        InputStream schemaStream = getClass().getResourceAsStream("/ai-result-schema.json");
        if (schemaStream == null) {
            throw new IllegalStateException("ai-result-schema.json couldn't be found");
        }
        JsonNode schemaNode = mapper.readTree(schemaStream);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        this.schema = factory.getJsonSchema(schemaNode);

    }

    @Transactional
    public Diagnosis applyAiResult(Integer diagnosisId, AiResultRequest req) throws IOException, ProcessingException {
        
        
        JsonNode node = mapper.valueToTree(req);

        if (!ValidationUtils.isJsonValid(schema, node)) {
            throw new IllegalArgumentException("Invalid AI result JSON");
        }

        Diagnosis d = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));

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
