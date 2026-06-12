package cd.beapi.service.impl;

import cd.beapi.dto.response.DashboardStatsResponse;
import cd.beapi.dto.response.DailyAppointmentResponse;
import cd.beapi.dto.response.HourlyAppointmentResponse;
import cd.beapi.dto.response.MonthlyAppointmentResponse;
import cd.beapi.dto.response.RecentAppointmentResponse;
import cd.beapi.dto.response.ServiceUsageResponse;
import cd.beapi.entity.Appointment;
import cd.beapi.enumerate.AppointmentArrivalStatus;
import cd.beapi.enumerate.AppointmentStatus;
import cd.beapi.repository.jpa.AppointmentRepository;
import cd.beapi.repository.jpa.TreatmentProcedureRepository;
import cd.beapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int DEFAULT_DURATION_MINUTES = 30;
    private static final int WORK_START_HOUR = 8;
    private static final int WORK_END_HOUR = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final TreatmentProcedureRepository treatmentProcedureRepository;

    @Transactional(readOnly = true)
    @Override
    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        long totalAppointmentsToday = appointmentRepository
                .countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(startOfToday, startOfTomorrow);
        long pendingAppointmentsToday = appointmentRepository
                .countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThanAndStatus(
                        startOfToday,
                        startOfTomorrow,
                        AppointmentStatus.PENDING
                );
        long arrivedPatientsToday = appointmentRepository
                .countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThanAndArrivalStatus(
                        startOfToday,
                        startOfTomorrow,
                        AppointmentArrivalStatus.ARRIVED
                );
        long completedAppointmentsToday = appointmentRepository
                .countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThanAndStatus(
                        startOfToday,
                        startOfTomorrow,
                        AppointmentStatus.COMPLETED
                );

        return new DashboardStatsResponse(
                totalAppointmentsToday,
                pendingAppointmentsToday,
                arrivedPatientsToday,
                completedAppointmentsToday,
                recentAppointments(),
                appointmentsByMonth(today.getYear()),
                appointmentsByDay(today),
                appointmentsByHour(today),
                treatmentProcedureRepository.findTopServiceUsage(PageRequest.of(0, 7))
        );
    }

    private List<RecentAppointmentResponse> recentAppointments() {
        return appointmentRepository.findRecentAppointments(PageRequest.of(0, 5)).stream()
                .map(this::toRecentAppointment)
                .toList();
    }

    private List<MonthlyAppointmentResponse> appointmentsByMonth(int year) {
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime startOfNextYear = LocalDate.of(year + 1, 1, 1).atStartOfDay();
        long[] counts = new long[12];

        appointmentRepository.findByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(startOfYear, startOfNextYear)
                .forEach(appointment -> {
                    if (appointment.getAppointmentDate() != null) {
                        counts[appointment.getAppointmentDate().getMonthValue() - 1]++;
                    }
                });

        List<MonthlyAppointmentResponse> result = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            result.add(new MonthlyAppointmentResponse("T" + (i + 1), counts[i]));
        }
        return result;
    }

    private List<DailyAppointmentResponse> appointmentsByDay(LocalDate dateInMonth) {
        LocalDate firstDayOfMonth = dateInMonth.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);
        long[] counts = new long[firstDayOfMonth.lengthOfMonth()];

        appointmentRepository.findByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(
                        firstDayOfMonth.atStartOfDay(),
                        firstDayOfNextMonth.atStartOfDay()
                )
                .forEach(appointment -> {
                    if (appointment.getAppointmentDate() != null) {
                        counts[appointment.getAppointmentDate().getDayOfMonth() - 1]++;
                    }
                });

        List<DailyAppointmentResponse> result = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 0; i < counts.length; i++) {
            LocalDate day = firstDayOfMonth.plusDays(i);
            result.add(new DailyAppointmentResponse(day.format(dayFormatter), counts[i]));
        }
        return result;
    }

    private List<HourlyAppointmentResponse> appointmentsByHour(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfTomorrow = date.plusDays(1).atStartOfDay();
        long[] counts = new long[24];

        appointmentRepository.findByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(startOfDay, startOfTomorrow)
                .forEach(appointment -> {
                    if (appointment.getAppointmentDate() != null) {
                        counts[appointment.getAppointmentDate().getHour()]++;
                    }
                });

        List<HourlyAppointmentResponse> result = new ArrayList<>();
        for (int hour = WORK_START_HOUR; hour < WORK_END_HOUR; hour++) {
            result.add(new HourlyAppointmentResponse(String.format("%02d:00", hour), counts[hour]));
        }
        return result;
    }

    private RecentAppointmentResponse toRecentAppointment(Appointment appointment) {
        LocalDateTime appointmentDate = appointment.getAppointmentDate();
        String dateText = appointmentDate == null ? "" : appointmentDate.format(DATE_FORMATTER);
        String timeSlot = appointmentDate == null ? "" : timeSlot(appointmentDate, appointment.getEstimatedDurationMinutes());
        return new RecentAppointmentResponse(
                appointment.getCode(),
                appointment.getPatient() == null ? "" : appointment.getPatient().getFullName(),
                appointment.getDentist() == null ? "" : appointment.getDentist().getFullName(),
                dateText,
                timeSlot,
                appointment.getStatus() == null ? "" : appointment.getStatus().name()
        );
    }

    private String timeSlot(LocalDateTime start, Integer durationMinutes) {
        int duration = durationMinutes == null || durationMinutes <= 0 ? DEFAULT_DURATION_MINUTES : durationMinutes;
        LocalTime end = start.toLocalTime().plusMinutes(duration);
        return start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
    }
}
