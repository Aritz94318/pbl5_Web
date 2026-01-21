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

    public void sendEmail(Notification notification) throws IOException, ProcessingException {

        String json = gson.toJson(notification);
        JsonNode node = mapper.readTree(json);

        if (ValidationUtils.isJsonValid(schema, node)) {
            System.out.println("Valid!");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(json, headers);

            restTemplate.exchange(
                    "https://node-red-591094411846.europe-west1.run.app/email",
                    HttpMethod.POST,
                    request,
                    Void.class);
        } else {
            System.out.println("NOT valid!");
        }

    }

}
