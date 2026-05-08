package cd.beapi.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileUploadConfig {
    String baseDir;
    Long maxSize;
    String allowedExtensions;
    @PostConstruct
    public void init() throws IOException {
        Path uploadPath= Paths.get(baseDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
    }
}
