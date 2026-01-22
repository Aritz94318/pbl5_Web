package edu.mondragon.we2.pinkAlert.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;

import edu.mondragon.we2.pinkAlert.model.Notification;
import edu.mondragon.we2.pinkAlert.utils.ValidationUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;

public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchema schema;

    public NotificationService() throws IOException, ProcessingException {
        InputStream schemaStream = getClass().getResourceAsStream("/email-schema.json");
        if (schemaStream == null) {
            throw new IllegalStateException("email-schema.json couldn't be found");
        }
        JsonNode schemaNode = mapper.readTree(schemaStream);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        this.schema = factory.getJsonSchema(schemaNode);
    }

    public void sendEmail(Notification n) throws IOException, ProcessingException {
        // Basic guard (prevents sending empty recipient)
        if (n.getEmail() == null || n.getEmail().isBlank()) {
            throw new IllegalArgumentException("Notification.email is null/blank");
        }

        // Build JSON that matches BOTH:
        // - your schema (email/topic/message/date)
        // - Node-RED email conventions (to/topic/payload/subject/text)
        ObjectNode body = mapper.createObjectNode();
        body.put("email", n.getEmail());
        body.put("topic", n.getTopic());
        body.put("message", n.getMessage());
        body.put("date", n.getDate());

        // Alias fields for Node-RED / nodemailer style
        body.put("to", n.getEmail());
        body.put("subject", n.getTopic());
        body.put("payload", n.getMessage()); // Node-RED email node often uses msg.payload as body
        body.put("text", n.getMessage()); // Some flows use text/html
        // (optional) if your message might contain HTML:
        // body.put("html", n.getMessage());

        // Validate against your schema (still passes because extra props are allowed)
        if (!ValidationUtils.isJsonValid(schema, body)) {
            System.out.println("NOT valid!");
            System.out.println(body.toPrettyString());
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                "https://node-red-591094411846.europe-west1.run.app/email",
                HttpMethod.POST,
                request,
                String.class);

        System.out.println("Email endpoint status: " + resp.getStatusCode());
        System.out.println("Email endpoint body: " + resp.getBody());
    }

}
