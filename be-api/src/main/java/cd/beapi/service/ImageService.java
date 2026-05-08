package cd.beapi.service;

import cd.beapi.dto.response.ImageResourceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ImageService {
    String upload(MultipartFile file, String path);

    String update(MultipartFile file, String oldAvatarUrl, String path);

    void delete(String avatarUrl);

    Path buildFullPath(String avatarUrl);

    boolean exists(String avatarUrl);

    ImageResourceResponse loadAsResource(String imagePath);
}
