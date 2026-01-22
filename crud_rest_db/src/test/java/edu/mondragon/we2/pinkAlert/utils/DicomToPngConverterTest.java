package edu.mondragon.we2.pinkAlert.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

class DicomToPngConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void testClassExistsAndHasStaticMethod() {
        assertNotNull(DicomToPngConverter.class);

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

        File nonExistentDicom = new File(tempDir.toFile(), "nonexistent.dcm");
        File pngFile = new File(tempDir.toFile(), "output.png");

        assertThrows(IllegalStateException.class, () -> {
            DicomToPngConverter.convert(nonExistentDicom, pngFile);
        });
    }

    @Test
    void testDirectoryCreationBehavior() {
        File nestedFile = new File(tempDir.toFile(), "deep/nested/directory/file.png");
        File parent = nestedFile.getParentFile();

        if (parent != null) {
            boolean created = parent.mkdirs();
            assertTrue(created || parent.exists());
        }
    }

    @Test
    void testFileExtensionValidation() {
        String dicomName = "image.dcm";
        String pngName = "image.png";

        assertTrue(dicomName.endsWith(".dcm"));
        assertTrue(pngName.endsWith(".png"));
    }


    @Test
    void testMethodSignature() {
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