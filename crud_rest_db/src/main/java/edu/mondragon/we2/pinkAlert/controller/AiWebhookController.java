package edu.mondragon.we2.pinkAlert.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.mondragon.we2.pinkAlert.dto.AiResultRequest;
import edu.mondragon.we2.pinkAlert.service.AiResultService;

@RestController
@RequestMapping("/diagnoses")
public class AiWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(AiWebhookController.class);
    
    private final AiResultService aiResultService;

    public AiWebhookController(AiResultService aiResultService) {
        this.aiResultService = aiResultService;
    }

    @PutMapping(value="/{id}/ai-result", consumes = { "application/json", "application/xml" }, 
                produces = { "application/json", "application/xml" })
    public ResponseEntity<Void> applyAiResult(
            @PathVariable Integer id,
            @RequestBody AiResultRequest request) {
        
        try {
            logger.debug("Processing AI result for diagnosis ID: {}", id);
            aiResultService.applyAiResult(id, request);
            logger.info("Successfully applied AI result for diagnosis ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to apply AI result for diagnosis ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}