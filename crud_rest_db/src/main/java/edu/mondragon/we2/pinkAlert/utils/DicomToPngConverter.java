package edu.mondragon.we2.pinkAlert.utils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DicomToPngConverter {

    public static void convert(File dicomFile, File pngFile) throws IOException {
        DicomImageReader reader = (DicomImageReader) new DicomImageReaderSpi().createReaderInstance();
        reader.setInput(ImageIO.createImageInputStream(dicomFile));
        DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();

        BufferedImage image = reader.read(0, param);
        if (image == null) {
            throw new IOException("Could not read DICOM: " + dicomFile.getAbsolutePath());
        }

        File parent = pngFile.getParentFile();
        if (parent != null) parent.mkdirs();
        boolean ok = ImageIO.write(image, "png", pngFile);
        if (!ok) throw new IOException("Could not write PNG: " + pngFile.getAbsolutePath());
    }
}
