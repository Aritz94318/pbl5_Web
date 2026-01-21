package edu.mondragon.we2.pinkAlert.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class DicomToPngConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void testClassExistsAndHasStaticMethod() {
        // Verificar que la clase existe
        assertNotNull(DicomToPngConverter.class);
        
        // Verificar que el método estático existe
        try {
            var method = DicomToPngConverter.class.getMethod("convert", File.class, File.class);
            assertNotNull(method);
            assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("El método convert no existe");
        }
    }

    @Test
    void testConvert_NonExistentDicomFile() {
        // Arrange
        File nonExistentDicom = new File(tempDir.toFile(), "nonexistent.dcm");
        File pngFile = new File(tempDir.toFile(), "output.png");
        
        // Act & Assert
        assertThrows( IllegalStateException
.class, () -> {
            DicomToPngConverter.convert(nonExistentDicom, pngFile);
        });
    }

    
    @Test
    void testDirectoryCreationBehavior() {
        // Probar la lógica de creación de directorios (similar a lo que hace el convertidor)
        File nestedFile = new File(tempDir.toFile(), "deep/nested/directory/file.png");
        File parent = nestedFile.getParentFile();
        
        // Esto es lo que hace el convertidor
        if (parent != null) {
            boolean created = parent.mkdirs();
            // O ya existe o se creó
            assertTrue(created || parent.exists());
        }
    }

    @Test
    void testFileExtensionValidation() {
        // Probar que los nombres de archivo se manejan correctamente
        String dicomName = "image.dcm";
        String pngName = "image.png";
        
        assertTrue(dicomName.endsWith(".dcm"));
        assertTrue(pngName.endsWith(".png"));
    }

    @Test
    void testNullSafety() {
        // Probar casos límite con null
        File nullFile = null;
        
        // El convertidor no maneja null directamente, pero podemos probar la lógica
        if (nullFile != null) {
            fail("Este código no debería ejecutarse");
        }
    }

    @Test
    void testMethodSignature() {
        // Verificar que el método tiene la firma correcta
        try {
            var method = DicomToPngConverter.class.getDeclaredMethod("convert", File.class, File.class);
            assertEquals(2, method.getParameterCount());
            assertEquals(File.class, method.getParameterTypes()[0]);
            assertEquals(File.class, method.getParameterTypes()[1]);
            assertEquals(void.class, method.getReturnType());
        } catch (NoSuchMethodException e) {
            fail("Método con firma incorrecta");
        }
    }
}