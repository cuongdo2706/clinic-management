package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull(message = "Appointment ID không được để trống")
        Long appointmentId,

        Long receptionistId,   // Staff lễ tân tiếp nhận (optional — lấy từ token nếu null)
        String note
) {}

