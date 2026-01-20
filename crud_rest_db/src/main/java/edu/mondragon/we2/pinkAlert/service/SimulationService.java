package edu.mondragon.we2.pinkAlert.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import edu.mondragon.we2.pinkAlert.model.GlobalUpdateRequest;
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
                    new SimEvent("SYSTEM", 0, "SSE conectado", System.currentTimeMillis())));
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
                try {
                    emitter.complete();
                } catch (Exception ignore) {
                }
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
                try {
                    emitter.complete();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public ResponseEntity<Void> modify(int numPatients, int numDoctors, int numMachines) {

        RestTemplate rt = new RestTemplate();
        String url = "https://node-red-591094411846.europe-west1.run.app/Simulation/modify"; // tu servidor de
                                                                                             // simulación

        GlobalUpdateRequest body;
        body = new GlobalUpdateRequest(numPatients, numDoctors, numMachines);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GlobalUpdateRequest> entity = new HttpEntity<>(body, headers);

        try {
            rt.exchange(url, HttpMethod.PUT, entity, Void.class);
            return ResponseEntity.ok().build();
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call Simulator service at " + url + ": " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> start() {

        RestTemplate rt = new RestTemplate();
        String url = "https://node-red-591094411846.europe-west1.run.app/Simulation/start"; // tu servidor de
                                                                                            // simulación

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("start", headers);

        rt.exchange(url, HttpMethod.POST, request, String.class);

        return ResponseEntity.ok().build();

    }
}
