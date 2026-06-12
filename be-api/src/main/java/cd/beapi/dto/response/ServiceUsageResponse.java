package cd.beapi.dto.response;

public record ServiceUsageResponse(
        String serviceName,
        Long count
) {
}
