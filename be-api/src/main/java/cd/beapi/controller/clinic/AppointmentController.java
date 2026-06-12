package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentArrivalStatusRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping("/{id}")
    public SuccessResponse<AppointmentResponse> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(),
                appointmentService.findById(id, username(jwt), roles(jwt)));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<AppointmentResponse>> search(@Valid @RequestBody SearchAppointmentRequest request,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(),
                appointmentService.search(request, username(jwt), roles(jwt)));
    }

    @PostMapping
    public SuccessResponse<AppointmentResponse> save(@Valid @RequestBody CreateAppointmentRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), appointmentService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<AppointmentResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateAppointmentRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), appointmentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public SuccessResponse<AppointmentResponse> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateAppointmentStatusRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update status successfully", Instant.now(),
                appointmentService.updateStatus(id, request, username(jwt), roles(jwt)));
    }

    @PatchMapping("/{id}/arrival-status")
    public SuccessResponse<AppointmentResponse> updateArrivalStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateAppointmentArrivalStatusRequest request,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update arrival status successfully", Instant.now(),
                appointmentService.updateArrivalStatus(id, request, username(jwt)));
    }

    @PatchMapping("/{id}/confirm")
    public SuccessResponse<AppointmentResponse> confirm(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Confirm appointment successfully", Instant.now(), appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/check-in")
    public SuccessResponse<AppointmentResponse> checkIn(@PathVariable Long id,
                                                        @RequestBody(required = false) CheckInAppointmentRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Check in appointment successfully", Instant.now(),
                appointmentService.checkIn(id, request, username(jwt)));
    }

    @PatchMapping("/{id}/start")
    public SuccessResponse<AppointmentResponse> start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Start appointment successfully", Instant.now(),
                appointmentService.start(id, username(jwt)));
    }

    @PatchMapping("/{id}/done")
    public SuccessResponse<AppointmentResponse> done(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Complete appointment successfully", Instant.now(),
                appointmentService.done(id, username(jwt)));
    }

    @PatchMapping("/{id}/cancel")
    public SuccessResponse<AppointmentResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Cancel appointment successfully", Instant.now(),
                appointmentService.cancel(id, username(jwt)));
    }

    @PatchMapping("/{id}/no-show")
    public SuccessResponse<AppointmentResponse> noShow(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Mark appointment as no-show successfully", Instant.now(),
                appointmentService.noShow(id, username(jwt)));
    }

    @GetMapping("/available-slots")
    public SuccessResponse<AvailableSlotResponse> getAvailableSlots(@RequestParam Long dentistId,
                                                                    @RequestParam LocalDate date,
                                                                    @RequestParam(required = false) Integer estimatedDurationMinutes) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get available slots successfully",
                Instant.now(),
                appointmentService.getAvailableSlots(dentistId, date, estimatedDurationMinutes)
        );
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }

    private String username(Jwt jwt) {
        return jwt == null ? "system" : jwt.getSubject();
    }

    private Set<String> roles(Jwt jwt) {
        if (jwt == null) {
            return Set.of();
        }
        Object roleClaim = jwt.getClaims().get("role");
        if (roleClaim instanceof List<?> roles) {
            Set<String> result = new HashSet<>();
            roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .forEach(result::add);
            return result;
        }
        if (roleClaim instanceof String role) {
            return Set.of(role);
        }
        return Collections.emptySet();
    }

}
