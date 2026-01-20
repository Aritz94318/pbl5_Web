package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
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

        String json = gson.toJson(req);
        JsonNode node = mapper.readTree(json);

        if (!ValidationUtils.isJsonValid(schema, node)) {
            throw new IllegalArgumentException("Invalid AI result JSON");
        }



        Diagnosis d = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));

        boolean urgent = "MALIGNANT".equalsIgnoreCase(req.getPrediction().trim());

        BigDecimal prob = req.getProbMalignant()
                .setScale(8, RoundingMode.HALF_UP);

        if (prob.compareTo(BigDecimal.ZERO) < 0) {
            prob = BigDecimal.ZERO;
        }
        if (prob.compareTo(BigDecimal.ONE) > 0) {
            prob = BigDecimal.ONE;
        }

        d.setUrgent(urgent);
        d.setProbability(prob);
        d.setReviewed(false);

        return diagnosisRepository.save(d);
    }
}
