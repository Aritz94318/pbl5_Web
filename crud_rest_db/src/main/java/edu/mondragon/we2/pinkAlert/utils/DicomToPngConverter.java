package edu.mondragon.we2.pinkAlert.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class DicomToPngConverter {
    private DicomToPngConverter() {}

    public static void convert(File dicomFile, File pngFile) throws IOException {
        BufferedImage image = ImageIO.read(dicomFile);
        if (image == null) {
            throw new IOException("Could not read DICOM (ImageIO returned null): " + dicomFile.getAbsolutePath());
        }
        File parent = pngFile.getParentFile();
        if (parent != null) parent.mkdirs();
        boolean ok = ImageIO.write(image, "png", pngFile);
        if (!ok) throw new IOException("Could not write PNG: " + pngFile.getAbsolutePath());
    }
}
