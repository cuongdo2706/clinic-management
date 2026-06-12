package cd.beapi.mapper;

import cd.beapi.dto.response.StaffResponse;
import cd.beapi.dto.response.StaffWorkingScheduleResponse;
import cd.beapi.entity.Staff;
import cd.beapi.entity.StaffWorkingSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StaffMapper {
    LocalTime DEFAULT_WORK_START = LocalTime.of(8, 30);
    LocalTime DEFAULT_WORK_END = LocalTime.of(20, 0);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "workingSchedules", expression = "java(toWorkingScheduleResponses(staff))")
    @Mapping(target = "temporaryPassword", ignore = true)
    StaffResponse toStaffResponse(Staff staff);

    List<StaffResponse> toStaffResponses(List<Staff> staffs);

    default List<StaffWorkingScheduleResponse> toWorkingScheduleResponses(Staff staff) {
        Map<DayOfWeek, StaffWorkingSchedule> scheduleByDay = staff.getWorkingSchedules() == null
                ? Map.of()
                : staff.getWorkingSchedules().stream()
                .filter(schedule -> schedule.getDayOfWeek() != null)
                .collect(Collectors.toMap(StaffWorkingSchedule::getDayOfWeek, Function.identity(), (first, second) -> second));

        return List.of(DayOfWeek.values()).stream()
                .map(dayOfWeek -> {
                    StaffWorkingSchedule schedule = scheduleByDay.get(dayOfWeek);
                    return new StaffWorkingScheduleResponse(
                            dayOfWeek,
                            schedule == null ? true : Boolean.TRUE.equals(schedule.getWorking()),
                            schedule == null || schedule.getStartTime() == null ? DEFAULT_WORK_START : schedule.getStartTime(),
                            schedule == null || schedule.getEndTime() == null ? DEFAULT_WORK_END : schedule.getEndTime()
                    );
                })
                .toList();
    }
}
