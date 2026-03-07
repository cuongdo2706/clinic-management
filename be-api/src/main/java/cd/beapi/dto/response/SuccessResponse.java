package cd.beapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
        int code,
        String message,
        Instant timestamp,
        T data
) {
}
