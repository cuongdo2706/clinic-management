package cd.beapi.service;

import cd.beapi.dto.request.ClientBookingRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.ClientHealthRecordResponse;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClientPortalService {
    PatientResponse getProfile(String username);

    PatientResponse updateProfile(String username, UpdatePatientRequest request);

    List<PublicDentistResponse> findDentists();

    List<PublicProcedureResponse> findProcedures();

    AvailableSlotResponse getAvailableSlots(Long dentistId, Long procedureId, LocalDate date, Integer durationMinutes);

    AppointmentResponse book(String username, ClientBookingRequest request);

    List<AppointmentResponse> getAppointments(String username);

    AppointmentResponse cancelAppointment(String username, Long appointmentId);

    List<ClientHealthRecordResponse> getHealthRecords(String username);
}
