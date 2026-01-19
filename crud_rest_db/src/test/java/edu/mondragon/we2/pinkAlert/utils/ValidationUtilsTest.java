package edu.mondragon.we2.pinkAlert.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @TempDir
    Path tempDir;
    
    private String validSchemaText;
    private String validJsonText;
    private String invalidJsonText;
    private File validSchemaFile;
    private File validJsonFile;
    
    @BeforeEach
    void setUp() throws IOException {
        // Schema JSON válido
        validSchemaText = """
            {
                "$schema": "http://json-schema.org/draft-04/schema#",
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "age": {"type": "integer"}
                },
                "required": ["name"]
            }
            """;
        
        // JSON válido
        validJsonText = """
            {
                "name": "John",
                "age": 30
            }
            """;
        
        // JSON inválido (falta campo requerido)
        invalidJsonText = """
            {
                "age": 30
            }
            """;
        
        // Crear archivos temporales
        validSchemaFile = tempDir.resolve("schema.json").toFile();
        Files.writeString(validSchemaFile.toPath(), validSchemaText);
        
        validJsonFile = tempDir.resolve("data.json").toFile();
        Files.writeString(validJsonFile.toPath(), validJsonText);
    }
    
    @Test
    void testGetJsonNodeFromString() throws IOException {
        JsonNode node = ValidationUtils.getJsonNode(validJsonText);
        assertNotNull(node);
        assertEquals("John", node.get("name").asText());
        assertEquals(30, node.get("age").asInt());
    }
    
    @Test
    void testGetJsonNodeFromFile() throws IOException {
        JsonNode node = ValidationUtils.getJsonNode(validJsonFile);
        assertNotNull(node);
        assertEquals("John", node.get("name").asText());
    }
    
    @Test
    void testGetJsonNodeFromResource() throws IOException {
        // Para este test necesitaríamos un archivo en resources
        // Vamos a mockear el comportamiento
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.getJsonNodeFromResource("non-existent.json");
        });
    }
    
    @Test
    void testGetSchemaNodeFromString() throws IOException, ProcessingException {
        JsonSchema schema = ValidationUtils.getSchemaNode(validSchemaText);
        assertNotNull(schema);
    }
    
    @Test
    void testGetSchemaNodeFromFile() throws IOException, ProcessingException {
        JsonSchema schema = ValidationUtils.getSchemaNode(validSchemaFile);
        assertNotNull(schema);
    }
    
    @Test
    void testIsJsonValidWithValidJson() throws IOException, ProcessingException {
        boolean isValid = ValidationUtils.isJsonValid(validSchemaText, validJsonText);
        assertTrue(isValid);
    }
    
    @Test
    void testIsJsonValidWithInvalidJson() throws IOException, ProcessingException {
        boolean isValid = ValidationUtils.isJsonValid(validSchemaText, invalidJsonText);
        assertFalse(isValid);
    }
    
    @Test
    void testIsJsonValidWithFiles() throws IOException, ProcessingException {
        boolean isValid = ValidationUtils.isJsonValid(validSchemaFile, validJsonFile);
        assertTrue(isValid);
    }
    
    @Test
    void testValidateJsonThrowsExceptionOnInvalid() {
        assertThrows(ProcessingException.class, () -> {
            ValidationUtils.validateJson(validSchemaText, invalidJsonText);
        });
    }
    
    @Test
    void testValidateJsonDoesNotThrowOnValid() throws IOException, ProcessingException {
        // No debería lanzar excepción
        ValidationUtils.validateJson(validSchemaText, validJsonText);
    }
    
    @Test
    void testGetJsonNodeWithInvalidJson() {
        assertThrows(IOException.class, () -> {
            ValidationUtils.getJsonNode("{ invalid json }");
        });
    }
    
    @Test
    void testGetSchemaNodeWithInvalidSchema() {
        String invalidSchema = "{ invalid schema }";
        assertThrows(IOException.class, () -> {
            ValidationUtils.getSchemaNode(invalidSchema);
        });
    }
}