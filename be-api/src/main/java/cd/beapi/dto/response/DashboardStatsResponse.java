package cd.beapi.dto.response;

import java.util.List;

public record DashboardStatsResponse(
        long totalAppointmentsToday,
        long pendingAppointmentsToday,
        long arrivedPatientsToday,
        long completedAppointmentsToday,
        List<RecentAppointmentResponse> recentAppointments,
        List<MonthlyAppointmentResponse> appointmentsByMonth,
        List<DailyAppointmentResponse> appointmentsByDay,
        List<HourlyAppointmentResponse> appointmentsByHour,
        List<ServiceUsageResponse> serviceUsage
) {
}
