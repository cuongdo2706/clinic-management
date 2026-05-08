package cd.beapi.mapper;

import cd.beapi.dto.response.WorkingScheduleResponse;
import cd.beapi.entity.WorkingSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkingScheduleMapper {
    WorkingScheduleResponse toWorkingScheduleResponse(WorkingSchedule workingSchedule);

    List<WorkingScheduleResponse> toWorkingScheduleResponses(List<WorkingSchedule> workingSchedules);
}
