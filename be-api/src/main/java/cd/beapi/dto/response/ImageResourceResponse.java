package cd.beapi.dto.response;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ImageResourceResponse(
        Resource resource,
        MediaType mediaType,
        String filename
) {
}
