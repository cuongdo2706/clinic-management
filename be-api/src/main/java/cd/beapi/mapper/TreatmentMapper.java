package cd.beapi.mapper;

import cd.beapi.dto.response.TreatmentResponse;
import cd.beapi.entity.Treatment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
        TreatmentCategoryMapper.class
})
public interface TreatmentMapper {
    TreatmentResponse toTreatmentResponse(Treatment treatment);

    List<TreatmentResponse>toTreatmentResponses(List<Treatment> treatments);
}
