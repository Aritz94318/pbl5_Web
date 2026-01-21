package edu.mondragon.we2.pinkalert.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;

import edu.mondragon.we2.pinkalert.model.GlobalUpdateRequest;
import edu.mondragon.we2.pinkalert.model.SimEvent;
import edu.mondragon.we2.pinkalert.model.SimTime;
import edu.mondragon.we2.pinkalert.utils.ValidationUtils;

@Service
public class SimulationService {
    private final Gson gson = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchema modifyschema;
    private final JsonSchema simEventSchema;
    private final JsonSchema simTimeSchema;
    private final RestTemplate rt;

    public SimulationService(RestTemplate rt) throws IOException, ProcessingException {
        this.rt = rt;
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

        this.modifyschema = factory.getJsonSchema(mapper.readTree(getSchema("/modify-sim-schema.json")));

        this.simEventSchema = factory.getJsonSchema(mapper.readTree(getSchema("/sim-event-schema.json")));

        this.simTimeSchema = factory.getJsonSchema(mapper.readTree(getSchema("/sim-time-schema.json")));

    } public SimulationService() throws IOException, ProcessingException {
        this(new RestTemplate());
    }

    public InputStream getSchema(String path) {
        InputStream schemaStream = getClass().getResourceAsStream(path);
        if (schemaStream == null) {
            throw new IllegalStateException("modify-sim-schema.json couldn't be found");
        }
        return schemaStream;

    }

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
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
                emitters.remove(emitter);
                try {
                    emitter.complete();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void modify(int numPatients, int numDoctors, int numMachines) throws IOException, ProcessingException {

        String url = "https://node-red-591094411846.europe-west1.run.app/Simulation/modify"; 
        GlobalUpdateRequest body;
        body = new GlobalUpdateRequest(numPatients, numDoctors, numMachines);
        String json = gson.toJson(body);
        JsonNode node = mapper.readTree(json);

        if (!ValidationUtils.isJsonValid(modifyschema, node)) {
            throw new IllegalArgumentException("Not Valid!");

        } else {
            System.out.println("Valid!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        try {
            rt.exchange(url, HttpMethod.PUT, entity, Void.class);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to call Simulator service at " + url + ": " + e.getMessage(), e);
        }
    }

    public void start() {

        String url = "https://node-red-591094411846.europe-west1.run.app/Simulation/start"; 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("start", headers);

        rt.exchange(url, HttpMethod.POST, request, String.class);

    }

    public void publishValidated(SimEvent event)
            throws IOException, ProcessingException {

        String json = gson.toJson(event);
        JsonNode node = mapper.readTree(json);

        if (!ValidationUtils.isJsonValid(simEventSchema, node)) {
            throw new IllegalArgumentException("Invalid SimEvent JSON");
        } else {
            System.out.println("Valid event");
        }

        publish(event);
    }

    public void publishValidated(SimTime time)
            throws IOException, ProcessingException {

        String json = gson.toJson(time);
        JsonNode node = mapper.readTree(json);

        if (!ValidationUtils.isJsonValid(simTimeSchema, node)) {
            throw new IllegalArgumentException("Invalid SimTime JSON");
        } else {
            System.out.println("Valid time");
        }

        publish(time);
    }

}
