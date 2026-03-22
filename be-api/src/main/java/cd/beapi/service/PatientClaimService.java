package cd.beapi.service;

import cd.beapi.dto.request.ClaimPatientRequest;
import cd.beapi.dto.request.LinkPatientToUserRequest;
import cd.beapi.dto.response.ClaimPatientResponse;
import cd.beapi.dto.response.PatientResponse;

import java.util.List;

public interface PatientClaimService {

    /**
     * Đăng ký tài khoản + tự động claim Patient walk-in (nếu SĐT khớp).
     * Dùng cho: Bệnh nhân tự đăng ký online.
     */
    ClaimPatientResponse registerAndClaimPatient(ClaimPatientRequest request);

    /**
     * Admin/Lễ tân liên kết thủ công Patient walk-in với User đã tồn tại.
     * Dùng cho: BN đổi SĐT, hoặc không thể tự claim.
     */
    PatientResponse linkPatientToUser(LinkPatientToUserRequest request);

    /**
     * Lấy thông tin Patient theo userId (BN đăng nhập xem hồ sơ).
     */
    PatientResponse getMyPatientProfile(Long userId);

    /**
     * Lấy thông tin Patient theo username (= phone, lấy từ JWT subject).
     */
    PatientResponse getMyPatientProfileByUsername(String username);

    /**
     * Phụ huynh đăng nhập → xem danh sách BN con/người phụ thuộc.
     */
    List<PatientResponse> getMyDependents(String username);
}


