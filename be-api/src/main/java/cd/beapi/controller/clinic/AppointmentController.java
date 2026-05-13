package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping("/{id}")
    public SuccessResponse<AppointmentResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), appointmentService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<AppointmentResponse>> search(@Valid @RequestBody SearchAppointmentRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), appointmentService.search(request));
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

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }

    @PatchMapping("/{id}/confirm")
    public SuccessResponse<AppointmentResponse> confirm(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Confirm appointment successfully", Instant.now(), appointmentService.confirm(id));
    }

    @PatchMapping("/{id}/check-in")
    public SuccessResponse<AppointmentResponse> checkIn(@PathVariable Long id,
                                                        @RequestBody(required = false) CheckInAppointmentRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Check in appointment successfully", Instant.now(), appointmentService.checkIn(id, request));
    }

    @PatchMapping("/{id}/start")
    public SuccessResponse<AppointmentResponse> start(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Start appointment successfully", Instant.now(), appointmentService.start(id));
    }

    @PatchMapping("/{id}/done")
    public SuccessResponse<AppointmentResponse> done(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Done appointment successfully", Instant.now(), appointmentService.done(id));
    }

    @PatchMapping("/{id}/cancel")
    public SuccessResponse<AppointmentResponse> cancel(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Cancel appointment successfully", Instant.now(), appointmentService.cancel(id));
    }

    @PatchMapping("/{id}/no-show")
    public SuccessResponse<AppointmentResponse> noShow(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Mark no-show successfully", Instant.now(), appointmentService.noShow(id));
    }

    @GetMapping("/available-slots")
    public SuccessResponse<AvailableSlotResponse> getAvailableSlots(@RequestParam Long dentistId,
                                                                    @RequestParam LocalDate date) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get available slots successfully", Instant.now(), appointmentService.getAvailableSlots(dentistId, date));
    }
}
