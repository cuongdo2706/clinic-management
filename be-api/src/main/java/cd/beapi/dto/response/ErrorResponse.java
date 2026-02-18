package cd.beapi.dto.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String path,
        String error,
        String message) {
}
