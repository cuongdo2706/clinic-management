package cd.beapi.mapper;

import cd.beapi.dto.response.StaffResponse;
import cd.beapi.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                WorkingScheduleMapper.class
        })
public interface StaffMapper {
    StaffResponse toStaffResponse(Staff staff);

    List<StaffResponse> toStaffResponses(List<Staff> staffs);
}
