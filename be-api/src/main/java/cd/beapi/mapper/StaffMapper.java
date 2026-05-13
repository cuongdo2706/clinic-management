package cd.beapi.mapper;

import cd.beapi.dto.response.StaffResponse;
import cd.beapi.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                WorkingScheduleMapper.class
        })
public interface StaffMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "temporaryPassword", ignore = true)
    StaffResponse toStaffResponse(Staff staff);

    List<StaffResponse> toStaffResponses(List<Staff> staffs);
}
