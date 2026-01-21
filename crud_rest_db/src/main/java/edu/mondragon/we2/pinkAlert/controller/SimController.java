package edu.mondragon.we2.pinkalert.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import edu.mondragon.we2.pinkalert.model.SimEvent;
import edu.mondragon.we2.pinkalert.model.SimTime;
import edu.mondragon.we2.pinkalert.service.SimulationService;

@RestController
public class SimController {

    private final SimulationService hub;

    public SimController(SimulationService hub) {
        this.hub = hub;
    }

    @PostMapping("/api/sim/events")
    @ResponseBody
    public ResponseEntity<Void> push(@RequestBody SimEvent event) throws IOException, ProcessingException {
        long ts = event.ts() == 0 ? System.currentTimeMillis() : event.ts();
        hub.publishValidated(new SimEvent(event.actor(), event.actorId(), event.text(), ts));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/sim/final")
    public ResponseEntity<Void> receiveFinalTime(@RequestBody SimTime time) throws IOException, ProcessingException {

        hub.publishValidated(time);
        System.out.println("⏱️ Tiempo final (s): " + (time.getSeconds()));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/sim/stream")
    @ResponseBody

    public SseEmitter stream() {
        return hub.connect();
    }

}
