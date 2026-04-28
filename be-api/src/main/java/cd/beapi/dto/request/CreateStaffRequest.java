package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CreateStaffRequest {
    String code;

    @NotBlank(message = "Name must have more than 1 digit")
    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String email;

    String address;

    CreateUserRequest user;

    List<WorkingScheduleRequest> workingSchedules;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkingScheduleRequest{
        DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
    }
}
