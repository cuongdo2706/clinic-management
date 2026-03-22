package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateMedicalRecordRequest(
        @NotNull(message = "Appointment ID không được để trống")
        Long appointmentId,

        String chiefComplaint,    // Lý do đến khám
        String diagnosis,         // Chẩn đoán
        String treatmentPlan,     // Kế hoạch điều trị
        String notes,

        Set<Long> serviceIds      // Dịch vụ thực tế đã thực hiện
) {}

