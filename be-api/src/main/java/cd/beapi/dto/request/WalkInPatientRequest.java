package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WalkInPatientRequest(
        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        /**
         * SĐT của bệnh nhân.
         * - Nếu BN là người lớn → SĐT chính của họ (bắt buộc)
         * - Nếu BN là trẻ em (isMinor=true) → có thể null, dùng guardianPhone thay thế
         */
        String phone,

        String dob,           // yyyy-MM-dd (optional)
        Boolean gender,       // true=Nam, false=Nữ (optional)
        String email,
        String identityNumber,
        String address,

        // ── Guardian fields (cho BN nhỏ tuổi) ──
        Boolean isMinor,                // true = BN chưa đủ tuổi
        Long guardianId,                // ID của Patient (nếu phụ huynh đã có trong hệ thống)
        String guardianName,            // Họ tên phụ huynh (nếu guardianId = null)
        String guardianPhone,           // SĐT phụ huynh
        String guardianRelationship     // FATHER, MOTHER, GRANDFATHER, GRANDMOTHER, SIBLING, UNCLE_AUNT, OTHER
) {}

