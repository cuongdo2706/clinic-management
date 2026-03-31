package cd.beapi.controller.clinic;

import cd.beapi.dto.request.GuestBookingRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Đặt lịch không cần tài khoản — khách vãng lai điền form.
     * Public endpoint, không yêu cầu xác thực.
     *
     * POST /api/appointments/guest-booking
     */
    @PostMapping("/guest-booking")
    public ResponseEntity<SuccessResponse<AppointmentResponse>> guestBooking(
            @Valid @RequestBody GuestBookingRequest request) {
        AppointmentResponse response = appointmentService.guestBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Đặt lịch thành công", Instant.now(), response));
    }
}



