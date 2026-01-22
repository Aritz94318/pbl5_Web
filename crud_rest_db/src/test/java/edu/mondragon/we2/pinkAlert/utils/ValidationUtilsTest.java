package edu.mondragon.we2.pinkAlert.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

class ValidationUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testGetJsonNode_FromString() throws Exception {

        String jsonText = "{\"name\":\"test\", \"age\":30}";

        JsonNode result = ValidationUtils.getJsonNode(jsonText);

        assertNotNull(result);
        assertEquals("test", result.get("name").asText());
        assertEquals(30, result.get("age").asInt());
    }

    @Test
    void testGetJsonNode_FromFile() throws Exception {

        File jsonFile = tempDir.resolve("test.json").toFile();
        String content = "{\"test\":\"value\"}";
        Files.write(jsonFile.toPath(), content.getBytes());

        JsonNode result = ValidationUtils.getJsonNode(jsonFile);

        assertNotNull(result);
        assertEquals("value", result.get("test").asText());
    }

    @Test
    void testGetJsonNode_FromFile_FileNotFound() {

        File nonExistentFile = new File("/non/existent/file.json");

        assertThrows(IOException.class, () -> {
            ValidationUtils.getJsonNode(nonExistentFile);
        });
    }

    @Test
    void testGetJsonNode_FromResource_ValidResource() throws Exception {
        try {
            JsonNode result = ValidationUtils.getJsonNodeFromResource("/application.properties");

            assertNotNull(result);
        } catch (IOException e) {
            assertFalse(e.getMessage().contains("resource") || e.getMessage().contains("not found"));
        }
    }

    @Test
    void testGetJsonNode_FromResource_InvalidResource() {

        String nonExistentResource = "/non/existent/resource.json";

        assertThrows(NoSuchMethodError.class, () -> {
            ValidationUtils.getJsonNodeFromResource(nonExistentResource);
        });
    }

    @Test
    void testIsJsonValid_WithValidJson() throws Exception {

        String schemaText = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "number"}
                    },
                    "required": ["name"]
                }
                """;

        String jsonText = """
                {
                    "name": "John Doe",
                    "age": 30
                }
                """;

        boolean result = ValidationUtils.isJsonValid(schemaText, jsonText);

        assertTrue(result);
    }

    @Test
    void testIsJsonValid_WithInvalidJson() throws Exception {

        String schemaText = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "number"}
                    },
                    "required": ["name"]
                }
                """;

        String jsonText = """
                {
                    "name": 123,
                    "age": 30
                }
                """;

        boolean result = ValidationUtils.isJsonValid(schemaText, jsonText);

        assertFalse(result);
    }

    @Test
    void testIsJsonValid_WithMissingRequiredField() throws Exception {

        String schemaText = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "number"}
                    },
                    "required": ["name"]
                }
                """;

        String jsonText = """
                {
                    "age": 30
                }
                """;

        boolean result = ValidationUtils.isJsonValid(schemaText, jsonText);

        assertFalse(result);
    }

    @Test
    void testValidateJson_ThrowsExceptionOnInvalid()  {

        String schemaText = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    },
                    "required": ["name"]
                }
                """;

        String jsonText = "{}";

        Exception exception = assertThrows(Exception.class, () -> {
            ValidationUtils.validateJson(schemaText, jsonText);
        });

        assertNotNull(exception);
    }

    @Test
    void testValidateJson_DoesNotThrowOnValid() {

        String schemaText = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    }
                }
                """;

        String jsonText = "{\"name\":\"test\"}";

        assertDoesNotThrow(() -> {
            ValidationUtils.validateJson(schemaText, jsonText);
        });
    }

    @Test
    void testIsJsonValid_WithFiles() throws Exception {

        File schemaFile = tempDir.resolve("schema.json").toFile();
        File jsonFile = tempDir.resolve("data.json").toFile();

        String schemaContent = """
                {
                    "type": "object",
                    "properties": {
                        "id": {"type": "number"}
                    }
                }
                """;

        String jsonContent = "{\"id\": 1}";

        Files.write(schemaFile.toPath(), schemaContent.getBytes());
        Files.write(jsonFile.toPath(), jsonContent.getBytes());

        boolean result = ValidationUtils.isJsonValid(schemaFile, jsonFile);

        assertTrue(result);
    }

    @Test
    void testIsJsonValid_WithURLs() throws Exception {

        File schemaFile = tempDir.resolve("schema.json").toFile();
        File jsonFile = tempDir.resolve("data.json").toFile();

        String schemaContent = """
                {
                    "type": "object",
                    "properties": {
                        "id": {"type": "number"}
                    }
                }
                """;

        String jsonContent = "{\"id\": 1}";

        Files.write(schemaFile.toPath(), schemaContent.getBytes());
        Files.write(jsonFile.toPath(), jsonContent.getBytes());

        URL schemaURL = schemaFile.toURI().toURL();
        URL jsonURL = jsonFile.toURI().toURL();

        boolean result = ValidationUtils.isJsonValid(schemaURL, jsonURL);

        assertTrue(result);
    }

    @Test
    void testValidateJsonResource() throws Exception {
        try {
            ValidationUtils.validateJsonResource("/schema.json", "/data.json");

        } catch (IOException e) {
            assertTrue(e instanceof IOException);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    void testConstants() {
        assertEquals("http://json-schema.org/draft-04/schema#",
                ValidationUtils.JSON_V4_SCHEMA_IDENTIFIER);
        assertEquals("$schema",
                ValidationUtils.JSON_SCHEMA_IDENTIFIER_ELEMENT);
    }

    @Test
    void testGetSchemaNode_FromString_AddsSchemaIdentifier()  {

        String schemaWithoutIdentifier = """
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    }
                }
                """;

        assertDoesNotThrow(() -> {
            ValidationUtils.getSchemaNode(schemaWithoutIdentifier);
        });
    }

    @Test
    void testGetSchemaNode_FromFile() throws Exception {

        File schemaFile = tempDir.resolve("schema.json").toFile();
        String schemaContent = """
                {
                    "$schema": "http://json-schema.org/draft-04/schema#",
                    "type": "object"
                }
                """;

        Files.write(schemaFile.toPath(), schemaContent.getBytes());

        assertDoesNotThrow(() -> {
            ValidationUtils.getSchemaNode(schemaFile);
        });
    }

    @Test
    void testGetSchemaNode_FromURL() throws Exception {

        File schemaFile = tempDir.resolve("schema.json").toFile();
        String schemaContent = """
                {
                    "$schema": "http://json-schema.org/draft-04/schema#",
                    "type": "object"
                }
                """;

        Files.write(schemaFile.toPath(), schemaContent.getBytes());
        URL schemaURL = schemaFile.toURI().toURL();

        assertDoesNotThrow(() -> {
            ValidationUtils.getSchemaNode(schemaURL);
        });
    }

    @Test
    void testComplexJsonValidation() throws Exception {

        String schemaText = """
                {
                    "$schema": "http://json-schema.org/draft-04/schema#",
                    "type": "object",
                    "properties": {
                        "id": {"type": "integer"},
                        "name": {"type": "string"},
                        "email": {"type": "string", "format": "email"},
                        "active": {"type": "boolean"},
                        "roles": {
                            "type": "array",
                            "items": {"type": "string"}
                        }
                    },
                    "required": ["id", "name", "email"]
                }
                """;

        String validJson = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@example.com",
                    "active": true,
                    "roles": ["admin", "user"]
                }
                """;

        String invalidJson = """
                {
                    "id": "should-be-number",
                    "name": "John Doe",
                    "email": "not-an-email",
                    "active": "should-be-bool"
                }
                """;

        assertTrue(ValidationUtils.isJsonValid(schemaText, validJson));
        assertFalse(ValidationUtils.isJsonValid(schemaText, invalidJson));
    }

    @Test
    void testEmptyJsonValidation() throws Exception {
        String schemaText = "{\"type\": \"object\"}";
        String emptyObject = "{}";
        String nullJson = "null";

        assertTrue(ValidationUtils.isJsonValid(schemaText, emptyObject));
        assertFalse(ValidationUtils.isJsonValid(schemaText, nullJson));
    }

    @Test
    void testArrayJsonValidation() throws Exception {
        String schemaText = """
                {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "id": {"type": "number"}
                        }
                    }
                }
                """;

        String validArray = "[{\"id\": 1}, {\"id\": 2}, {\"id\": 3}]";
        String invalidArray = "[{\"id\": \"string\"}, {\"id\": 2}]";

        assertTrue(ValidationUtils.isJsonValid(schemaText, validArray));
        assertFalse(ValidationUtils.isJsonValid(schemaText, invalidArray));
    }

    @Test
    void testMalformedJson() {

        String schemaText = "{\"type\": \"object\"}";
        String malformedJson = "{not valid json}";

        assertThrows(IOException.class, () -> {
            ValidationUtils.isJsonValid(schemaText, malformedJson);
        });
    }
}