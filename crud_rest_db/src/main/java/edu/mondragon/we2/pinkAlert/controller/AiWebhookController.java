package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.service.AiResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiWebhookController {

    private final AiResultService aiResultService;

    public AiWebhookController(AiResultService aiResultService) {
        this.aiResultService = aiResultService;
    }

    @PostMapping(value = "/result", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> receiveAiResult(@RequestBody AiResultRequest req) {
        Diagnosis updated = aiResultService.applyAiResult(req);

        // Return a small ack
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "diagnosis_id", updated.getId(),
                "urgent", updated.isUrgent(),
                "probability", updated.getProbability().toPlainString()));
    }
}
