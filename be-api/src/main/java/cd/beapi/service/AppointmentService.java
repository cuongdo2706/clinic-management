package cd.beapi.service;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.ClientBookingRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.PublicBookingRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentArrivalStatusRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;

import java.time.LocalDate;
import java.util.Set;

public interface AppointmentService {
    AppointmentResponse findById(Long id, String username, Set<String> roles);

    PageData<AppointmentResponse> search(SearchAppointmentRequest searchAppointmentRequest, String username, Set<String> roles);

    AppointmentResponse save(CreateAppointmentRequest createAppointmentRequest);

    AppointmentResponse saveForPatient(Long patientId, ClientBookingRequest request);

    AppointmentResponse savePublic(PublicBookingRequest request);

    AppointmentResponse update(Long id, UpdateAppointmentRequest updateAppointmentRequest);

    AppointmentResponse updateStatus(Long id, UpdateAppointmentStatusRequest request, String changedBy, Set<String> roles);

    AppointmentResponse updateArrivalStatus(Long id, UpdateAppointmentArrivalStatusRequest request, String changedBy);

    void delete(Long id);

    AppointmentResponse confirm(Long id);

    AppointmentResponse checkIn(Long id, CheckInAppointmentRequest request, String changedBy);

    AppointmentResponse start(Long id, String changedBy);

    AppointmentResponse done(Long id, String changedBy);

    AppointmentResponse cancel(Long id, String changedBy);

    AppointmentResponse noShow(Long id, String changedBy);

    AvailableSlotResponse getAvailableSlots(Long dentistId, LocalDate date, Integer estimatedDurationMinutes);
}
