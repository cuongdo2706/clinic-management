package cd.beapi.controller.client;

import cd.beapi.dto.request.ClientBookingRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.ClientHealthRecordResponse;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.ClientPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientPortalController {
    private final ClientPortalService clientPortalService;

    @GetMapping("/me")
    public SuccessResponse<PatientResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get profile successfully", Instant.now(),
                clientPortalService.getProfile(jwt.getSubject()));
    }

    @PutMapping("/me")
    public SuccessResponse<PatientResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody UpdatePatientRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update profile successfully", Instant.now(),
                clientPortalService.updateProfile(jwt.getSubject(), request));
    }

    @GetMapping("/booking/dentists")
    public SuccessResponse<List<PublicDentistResponse>> findDentists() {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get dentists successfully", Instant.now(),
                clientPortalService.findDentists());
    }

    @GetMapping("/booking/procedures")
    public SuccessResponse<List<PublicProcedureResponse>> findProcedures() {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get procedures successfully", Instant.now(),
                clientPortalService.findProcedures());
    }

    @GetMapping("/booking/available-slots")
    public SuccessResponse<AvailableSlotResponse> getAvailableSlots(@RequestParam Long dentistId,
                                                                    @RequestParam LocalDate date,
                                                                    @RequestParam(required = false) Long procedureId,
                                                                    @RequestParam(required = false) Integer durationMinutes) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get available slots successfully", Instant.now(),
                clientPortalService.getAvailableSlots(dentistId, procedureId, date, durationMinutes));
    }

    @PostMapping("/appointments")
    public SuccessResponse<AppointmentResponse> book(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody ClientBookingRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Book appointment successfully", Instant.now(),
                clientPortalService.book(jwt.getSubject(), request));
    }

    @GetMapping("/appointments")
    public SuccessResponse<List<AppointmentResponse>> getAppointments(@AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get appointments successfully", Instant.now(),
                clientPortalService.getAppointments(jwt.getSubject()));
    }

    @PatchMapping("/appointments/{id}/cancel")
    public SuccessResponse<AppointmentResponse> cancelAppointment(@AuthenticationPrincipal Jwt jwt,
                                                                  @PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Cancel appointment successfully", Instant.now(),
                clientPortalService.cancelAppointment(jwt.getSubject(), id));
    }

    @GetMapping("/health-records")
    public SuccessResponse<List<ClientHealthRecordResponse>> getHealthRecords(@AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get health records successfully", Instant.now(),
                clientPortalService.getHealthRecords(jwt.getSubject()));
    }
}
