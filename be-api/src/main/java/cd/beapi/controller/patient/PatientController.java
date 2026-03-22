package cd.beapi.controller.patient;

import cd.beapi.dto.request.LinkPatientToUserRequest;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.PatientClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientClaimService patientClaimService;

    // ──────────────────────────────────────────────
    // Admin/Lễ tân link thủ công Patient walk-in ↔ User
    // POST /patients/link
    // ──────────────────────────────────────────────
    @PostMapping("/link")
    public ResponseEntity<SuccessResponse<PatientResponse>> linkPatientToUser(
            @Valid @RequestBody LinkPatientToUserRequest request) {
        PatientResponse response = patientClaimService.linkPatientToUser(request);
        return ResponseEntity.ok(
                new SuccessResponse<>(200, "Liên kết bệnh nhân với tài khoản thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // BN đăng nhập xem hồ sơ của mình
    // GET /patients/me
    // ──────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<PatientResponse>> getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        PatientResponse response = patientClaimService.getMyPatientProfileByUsername(username);
        return ResponseEntity.ok(
                new SuccessResponse<>(200, "Lấy hồ sơ thành công", Instant.now(), response));
    }

    // ──────────────────────────────────────────────
    // Phụ huynh xem danh sách BN con / người phụ thuộc
    // GET /patients/me/dependents
    // ──────────────────────────────────────────────
    @GetMapping("/me/dependents")
    public ResponseEntity<SuccessResponse<List<PatientResponse>>> getMyDependents(
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        List<PatientResponse> dependents = patientClaimService.getMyDependents(username);
        return ResponseEntity.ok(
                new SuccessResponse<>(200, "Lấy danh sách người phụ thuộc thành công", Instant.now(), dependents));
    }
}
