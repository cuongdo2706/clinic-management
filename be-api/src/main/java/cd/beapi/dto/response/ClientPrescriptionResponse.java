package cd.beapi.dto.response;

import java.time.Instant;
import java.util.List;

public record ClientPrescriptionResponse(
        Long id,
        String code,
        String note,
        Instant createdAt,
        List<ClientPrescriptionItemResponse> items
) {
}
