package cd.beapi.controller.clinic;

import cd.beapi.dto.response.DashboardStatsResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public SuccessResponse<DashboardStatsResponse> getStats() {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get dashboard stats successfully",
                Instant.now(),
                dashboardService.getStats()
        );
    }
}
