package edu.mondragon.we2.pinkAlert.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/previews")
public class PreviewController {

    @Value("${pinkalert.storage.dir}")
    private String storageDir;

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getPreview(@PathVariable String filename) throws IOException {

        Path file = Paths.get(storageDir)
                .resolve("previews")
                .resolve(filename)
                .normalize();

        if (!file.startsWith(Paths.get(storageDir)) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }
}