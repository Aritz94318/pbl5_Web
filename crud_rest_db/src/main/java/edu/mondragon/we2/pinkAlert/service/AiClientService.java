package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import edu.mondragon.we2.pinkAlert.utils.ValidationUtils;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;

@Service
public class AiClientService {

    private static final String AI_PREDICT_URL = "https://node-red-591094411846.europe-west1.run.app/predict-url";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchema schema;

    public AiClientService() throws IOException, ProcessingException {
        InputStream schemaStream = getClass().getResourceAsStream("/ai-request-schema.json");
        if (schemaStream == null) {
            throw new IllegalStateException("ai-request-schema.json couldn't be found");
        }
        JsonNode schemaNode = mapper.readTree(schemaStream);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        this.schema = factory.getJsonSchema(schemaNode);
    }

    public String sendPredictUrl(AiPredictUrlRequest body) throws IOException, ProcessingException {

        String json = gson.toJson(body);
        JsonNode node = mapper.readTree(json);

        if (ValidationUtils.isJsonValid(schema, node)) {
            System.out.println("Valid!");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            try {
                ResponseEntity<String> resp = restTemplate.postForEntity(AI_PREDICT_URL, entity, String.class);
                return resp.getBody();
            } catch (RestClientException e) {
                throw new RuntimeException("Failed to call AI service at " + AI_PREDICT_URL + ": " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("Not Valid!");
        }

    }
}
