package edu.mondragon.we2.pinkAlert.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PreviewControllerTest {

    @InjectMocks
    private PreviewController previewController;

    @Mock
    private org.springframework.core.io.Resource resource;

    @Test
    void getPreview_Success() throws Exception {
        var field = PreviewController.class.getDeclaredField("storageDir");
        field.setAccessible(true);
        field.set(previewController, "/test/storage");

        Path tempDir = Files.createTempDirectory("test-storage");
        Path previewsDir = tempDir.resolve("previews");
        Files.createDirectories(previewsDir);
        Path testFile = previewsDir.resolve("test.png");
        Files.createFile(testFile);

        field.set(previewController, tempDir.toString());

        ResponseEntity<Resource> response = previewController.getPreview("test.png");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(previewsDir);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void getPreview_FileNotFound() throws Exception {
        var field = PreviewController.class.getDeclaredField("storageDir");
        field.setAccessible(true);
        field.set(previewController, "/test/storage");

        ResponseEntity<Resource> response = previewController.getPreview("non-existent.png");

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getPreview_IOException() throws Exception {
        var field = PreviewController.class.getDeclaredField("storageDir");
        field.setAccessible(true);
        field.set(previewController, "/test/storage");

        try {
             previewController.getPreview("test.png");
        } catch (Exception e) {
            assertNull(e);
        }
        
        assertTrue(true);
    }

    @Test
    void getPreview_PathTraversal() throws Exception {
        var field = PreviewController.class.getDeclaredField("storageDir");
        field.setAccessible(true);
        field.set(previewController, "/test/storage");

        ResponseEntity<Resource> response = previewController.getPreview("../../etc/passwd");

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }


    @Test
    void testControllerCreation() {
        PreviewController controller = new PreviewController();
        assertNotNull(controller);
    }
}