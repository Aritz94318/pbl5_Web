package edu.mondragon.we2.pinkAlert.controller;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.service.AiResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diagnoses")
public class AiWebhookController {

    private final AiResultService aiResultService;

    public AiWebhookController(AiResultService aiResultService) {
        this.aiResultService = aiResultService;
    }

    @PutMapping(value="/{id}/ai-result",consumes = { "application/json", "application/xml" }, produces = {
            "application/json", "application/xml" })
    public ResponseEntity<Void> applyAiResult(
            @PathVariable Integer id,
            @RequestBody AiResultRequest request) {

        try {
            
            aiResultService.applyAiResult(id, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
