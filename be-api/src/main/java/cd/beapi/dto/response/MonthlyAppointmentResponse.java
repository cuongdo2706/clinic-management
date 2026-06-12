package cd.beapi.dto.response;

public record MonthlyAppointmentResponse(
        String month,
        long count
) {
}
