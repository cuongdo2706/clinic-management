package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
        @NotNull(message = "Patient ID không được để trống")
        Long patientId,

        @NotNull(message = "Dentist ID không được để trống")
        Long dentistId,

        @NotNull(message = "Ngày hẹn không được để trống")
        String appointmentDate,   // yyyy-MM-dd

        @NotNull(message = "Giờ bắt đầu không được để trống")
        String startTime,         // HH:mm

        @NotNull(message = "Giờ kết thúc không được để trống")
        String endTime,           // HH:mm

        String note               // Lý do khám
) {}

