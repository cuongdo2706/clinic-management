package cd.beapi.service;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;

import java.time.LocalDate;

public interface AppointmentService {
    AppointmentResponse findById(Long id);

    PageData<AppointmentResponse> search(SearchAppointmentRequest searchAppointmentRequest);

    AppointmentResponse save(CreateAppointmentRequest createAppointmentRequest);

    AppointmentResponse update(Long id, UpdateAppointmentRequest updateAppointmentRequest);

    AppointmentResponse updateStatus(Long id, UpdateAppointmentStatusRequest request);

    void delete(Long id);

    AppointmentResponse confirm(Long id);

    AppointmentResponse checkIn(Long id, CheckInAppointmentRequest request);

    AppointmentResponse start(Long id);

    AppointmentResponse done(Long id);

    AppointmentResponse cancel(Long id);

    AppointmentResponse noShow(Long id);

    AvailableSlotResponse getAvailableSlots(Long dentistId, LocalDate date, Integer estimatedDurationMinutes);
}
