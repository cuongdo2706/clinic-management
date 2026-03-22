package cd.beapi.controller.common;

import cd.beapi.dto.request.ClaimPatientRequest;
import cd.beapi.dto.response.ClaimPatientResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.PatientClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Public endpoint — BN tự đăng ký tài khoản (không cần authentication).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PatientRegistrationController {

    private final PatientClaimService patientClaimService;

    /**
     * POST /auth/register
     * Đăng ký tài khoản + tự động claim Patient walk-in (nếu SĐT khớp).
     */
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<ClaimPatientResponse>> register(
            @Valid @RequestBody ClaimPatientRequest request) {
        ClaimPatientResponse response = patientClaimService.registerAndClaimPatient(request);

        String message = response.claimed()
                ? "Đăng ký thành công! Đã liên kết với hồ sơ khám cũ."
                : "Đăng ký thành công! Đã tạo hồ sơ bệnh nhân mới.";

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(201, message, Instant.now(), response));
    }
}

