package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuestBookingRequest {

    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    String phone;

    LocalDate dob;

    Boolean gender;

    // Triệu chứng mô tả ban đầu của khách
    String symptom;

    String note;

    @NotNull(message = "Ngày hẹn không được để trống")
    LocalDateTime appointmentDate;

    // ID bác sĩ nếu khách chủ động chọn (nullable)
    Long dentistId;
}



