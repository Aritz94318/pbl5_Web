package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.model.Diagnosis;
import edu.mondragon.we2.pinkAlert.service.AiResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/diagnoses")
public class AiWebhookController {

    private final AiResultService aiResultService;

    public AiWebhookController(AiResultService aiResultService) {
        this.aiResultService = aiResultService;
    }

    @PutMapping("/{id}/ai-result")
    public ResponseEntity<Diagnosis> applyAiResult(
            @PathVariable Integer id,
            @RequestBody AiResultRequest request
    ) {
        Diagnosis updated = aiResultService.applyAiResult(id, request);
        return ResponseEntity.ok(updated);
    }
}
