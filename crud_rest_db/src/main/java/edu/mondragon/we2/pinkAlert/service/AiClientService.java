package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.dto.AiPredictUrlRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AiClientService {

    private static final String AI_PREDICT_URL = "https://node-red-591094411846.europe-west1.run.app/predict-url";

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendPredictUrl(AiPredictUrlRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiPredictUrlRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(AI_PREDICT_URL, entity, String.class);
            return "redirect:/admin/dashboard";
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call AI service at " + AI_PREDICT_URL + ": " + e.getMessage(), e);
        }
    }
}
