package cd.beapi.dto.response;

public record RecentAppointmentResponse(
        String id,
        String patientName,
        String dentistName,
        String appointmentDate,
        String timeSlot,
        String status
) {
}
