package cd.beapi.controller.common;

import cd.beapi.dto.response.ImageResourceResponse;
import cd.beapi.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @GetMapping("/{*imagePath}")
    public ResponseEntity<Resource> serveImage(@PathVariable String imagePath) {
        ImageResourceResponse image = imageService.loadAsResource(imagePath);
        return ResponseEntity.ok()
                .contentType(image.mediaType())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.filename() + "\"")
                .body(image.resource());
    }
}
