package cd.beapi.dto.request;

import cd.beapi.enumerate.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStaffRequest {
    String code;

    @NotBlank(message = "Name must have more than 1 digit")
    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String email;

    String address;

    @NotNull(message = "Staff type is required")
    StaffType staffType;

    List<WorkingScheduleRequest> workingSchedules;

    Long version;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkingScheduleRequest {
        DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
    }
}
