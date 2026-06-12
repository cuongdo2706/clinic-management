package cd.beapi.service.impl;

import cd.beapi.config.FileUploadConfig;
import cd.beapi.dto.response.ImageResourceResponse;
import cd.beapi.exception.AppException;
import cd.beapi.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final FileUploadConfig fileUploadConfig;

    @Override
    public String upload(MultipartFile file, String path) {
        Path fullPath = null;
        try {
            validateFile(file);
            String extension = getFileExtension(file.getOriginalFilename());
            String relativePath = normalizePath(path) + "/" + generateFileName(extension);
            fullPath = buildFullPathFromRelativePath(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (AppException e) {
            throw e;
        } catch (IOException e) {
            deleteQuietly(fullPath);
            throw new AppException("Cannot save image file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String update(MultipartFile file, String oldAvatarUrl, String path) {
        String newAvatarUrl = upload(file, path);
        try {
            delete(oldAvatarUrl);
            return newAvatarUrl;
        } catch (AppException e) {
            deleteQuietly(buildFullPath(newAvatarUrl));
            throw new AppException("Cannot update image file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return;
        }
        Path fullPath = buildFullPath(avatarUrl);
        try {
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                Files.delete(fullPath);
            }
        } catch (IOException e) {
            throw new AppException("Cannot delete image file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Path buildFullPath(String avatarUrl) {
        String relativePath = stripPublicPrefix(avatarUrl);
        return buildFullPathFromRelativePath(relativePath);
    }

    @Override
    public boolean exists(String avatarUrl) {
        Path fullPath = buildFullPath(avatarUrl);
        return Files.exists(fullPath) && Files.isRegularFile(fullPath);
    }

    @Override
    public ImageResourceResponse loadAsResource(String imagePath) {
        String normalizedImagePath = normalizePath(imagePath);
        Path fullPath = buildFullPathFromRelativePath(normalizedImagePath);

        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            throw new AppException("Image not found", HttpStatus.NOT_FOUND);
        }

        try {
            Resource resource = toResource(fullPath);
            MediaType mediaType = resolveMediaType(fullPath);

            return new ImageResourceResponse(resource, mediaType, fullPath.getFileName().toString());
        } catch (IOException e) {
            throw new AppException("Cannot load image file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Resource toResource(Path fullPath) throws MalformedURLException {
        Resource resource = new UrlResource(fullPath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new AppException("Image is not readable", HttpStatus.NOT_FOUND);
        }

        return resource;
    }

    private MediaType resolveMediaType(Path fullPath) throws IOException {
        String contentType = Files.probeContentType(fullPath);

        if (StringUtils.hasText(contentType)) {
            return MediaType.parseMediaType(contentType);
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private Path buildFullPathFromRelativePath(String relativePath) {
        Path baseDir = getBaseDir();
        Path fullPath = baseDir.resolve(relativePath).normalize();

        if (!fullPath.startsWith(baseDir)) {
            throw new AppException("Invalid image path", HttpStatus.BAD_REQUEST);
        }

        return fullPath;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException("Image file is required", HttpStatus.BAD_REQUEST);
        }

        if (fileUploadConfig.getMaxSize() != null && file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new AppException("Image file size exceeds allowed limit", HttpStatus.BAD_REQUEST);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!allowedExtensions().contains(extension)) {
            throw new AppException("Image file extension is not allowed", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AppException("Uploaded file must be an image", HttpStatus.BAD_REQUEST);
        }

        if (ImageIO.read(file.getInputStream()) == null) {
            throw new AppException("Uploaded file is not a valid image", HttpStatus.BAD_REQUEST);
        }
    }

    private Set<String> allowedExtensions() {
        return Arrays.stream(fileUploadConfig.getAllowedExtensions().split(","))
                .map(extension -> extension.trim().toLowerCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private String getFileExtension(String filename) {
        String cleanFilename = StringUtils.cleanPath(filename == null ? "" : filename);
        int dotIndex = cleanFilename.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == cleanFilename.length() - 1) {
            throw new AppException("Image file extension is required", HttpStatus.BAD_REQUEST);
        }

        return cleanFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            throw new AppException("Image folder path is required", HttpStatus.BAD_REQUEST);
        }
        String normalizedPath = path.replace("\\", "/");
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        while (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        if (!StringUtils.hasText(normalizedPath) || normalizedPath.contains("..")) {
            throw new AppException("Invalid image folder path", HttpStatus.BAD_REQUEST);
        }
        return normalizedPath;
    }

    private String stripPublicPrefix(String avatarUrl) {
        String normalizedUrl = avatarUrl.replace("\\", "/");
        if (normalizedUrl.startsWith("/uploads/")) {
            return normalizedUrl.substring("/uploads/".length());
        }
        if (normalizedUrl.startsWith("uploads/")) {
            return normalizedUrl.substring("uploads/".length());
        }
        while (normalizedUrl.startsWith("/")) {
            normalizedUrl = normalizedUrl.substring(1);
        }
        return normalizedUrl;
    }

    private Path getBaseDir() {
        return Paths.get(fileUploadConfig.getBaseDir()).toAbsolutePath().normalize();
    }

    private void deleteQuietly(Path fullPath) {
        if (fullPath == null) {
            return;
        }
        try {
            Files.deleteIfExists(fullPath);
        } catch (IOException ignored) {
        }
    }
}
