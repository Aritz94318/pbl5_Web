package edu.mondragon.we2.pinkAlert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import edu.mondragon.we2.pinkAlert.model.SimEvent;
import edu.mondragon.we2.pinkAlert.model.SimTime;
import edu.mondragon.we2.pinkAlert.service.SimulationService;

@RestController
public class SimController {

    private final SimulationService hub;

    public SimController(SimulationService hub) {
        this.hub = hub;
    }

    // API: la simulación manda una acción
    @PostMapping("/api/sim/events")
    @ResponseBody
    public ResponseEntity<Void> push(@RequestBody SimEvent event) {
        long ts = event.ts() == 0 ? System.currentTimeMillis() : event.ts();
        hub.publish(new SimEvent(event.actor(), event.actorId(), event.text(), ts));
        return ResponseEntity.ok().build();
    }

    
     @PostMapping("/api/sim/final")
    public ResponseEntity<Void> receiveFinalTime(@RequestBody long timeNanoSeconds) {

        hub.publish(new SimTime(timeNanoSeconds));
        System.out.println("🏁 Tiempo final recibido (ns): " + timeNanoSeconds);
        System.out.println("⏱️ Tiempo final (s): " + (timeNanoSeconds / 1_000_000_000.0));

        return ResponseEntity.ok().build();
    }
    // Stream: el admin escucha en tiempo real
    @GetMapping("/admin/sim/stream")
    @ResponseBody

    public SseEmitter stream() {
        return hub.connect();
    }


}
