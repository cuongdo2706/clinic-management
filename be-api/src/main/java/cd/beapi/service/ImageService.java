package cd.beapi.service;

import cd.beapi.dto.response.ImageResourceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageService {
    String upload(MultipartFile file, String path) throws IOException;

    String update(MultipartFile file, String oldAvatarUrl, String path) throws IOException;

    void delete(String avatarUrl) throws IOException;

    Path buildFullPath(String avatarUrl);

    boolean exists(String avatarUrl);

    ImageResourceResponse loadAsResource(String imagePath) throws IOException;
}
