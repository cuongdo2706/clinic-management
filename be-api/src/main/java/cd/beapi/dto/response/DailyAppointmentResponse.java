package cd.beapi.dto.response;

public record DailyAppointmentResponse(
        String day,
        long count
) {
}
