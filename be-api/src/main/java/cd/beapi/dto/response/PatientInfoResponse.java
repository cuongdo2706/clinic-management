package cd.beapi.dto.response;

import java.time.LocalDate;

public record PatientInfoResponse(
        Long id,
        String code,
        String fullName,
        String phone,
        String email,
        Boolean gender,
        LocalDate dob,
        String address,
        Boolean isActive,
        String guardianName,
        String guardianPhone
) {
}
