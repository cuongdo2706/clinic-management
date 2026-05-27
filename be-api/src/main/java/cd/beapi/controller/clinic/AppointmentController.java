package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
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

    @PatchMapping("/{id}/status")
    public SuccessResponse<AppointmentResponse> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update status successfully", Instant.now(), appointmentService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }

}
