package cd.beapi.controller.client;

import cd.beapi.dto.request.PublicBookingRequest;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.PublicBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/booking")
public class PublicBookingController {
    private final PublicBookingService publicBookingService;

    @GetMapping("/dentists")
    public SuccessResponse<List<PublicDentistResponse>> findDentists() {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get dentists successfully",
                Instant.now(),
                publicBookingService.findDentists()
        );
    }

    @GetMapping("/procedures")
    public SuccessResponse<List<PublicProcedureResponse>> findProcedures() {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get procedures successfully",
                Instant.now(),
                publicBookingService.findProcedures()
        );
    }

    @GetMapping("/available-slots")
    public SuccessResponse<AvailableSlotResponse> getAvailableSlots(@RequestParam Long dentistId,
                                                                    @RequestParam LocalDate date,
                                                                    @RequestParam(required = false) Long procedureId,
                                                                    @RequestParam(required = false) Integer durationMinutes) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get available slots successfully",
                Instant.now(),
                publicBookingService.getAvailableSlots(dentistId, procedureId, date, durationMinutes)
        );
    }

}
