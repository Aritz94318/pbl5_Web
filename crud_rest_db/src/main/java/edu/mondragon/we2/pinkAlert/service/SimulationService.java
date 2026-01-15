package edu.mondragon.we2.pinkAlert.service;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import edu.mondragon.we2.pinkAlert.model.SimEvent;
import edu.mondragon.we2.pinkAlert.model.SimTime;

@Service
public class SimulationService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter connect() {
        // 0L = sin timeout (o pon por ejemplo 30 * 60 * 1000L)
        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);

        // Cuando el cliente se desconecta, limpiamos
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));

        // (Opcional) evento inicial para probar que conecta
        try {
            emitter.send(SseEmitter.event().name("sim").data(
                new SimEvent("SYSTEM", 0, "SSE conectado", System.currentTimeMillis())
            ));
        } catch (IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    public void publish(SimEvent event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("sim").data(event));
            } catch (Exception ex) {
                // Si está cerrado o falló, lo quitamos
                emitters.remove(emitter);
                try { emitter.complete(); } catch (Exception ignore) {}
            }
        }
    }
    public void publish(SimTime event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("sim-time").data(event));
            } catch (Exception ex) {
                // Si está cerrado o falló, lo quitamos
                emitters.remove(emitter);
                try { emitter.complete(); } catch (Exception ignore) {}
            }
        }
    }
}
