package cd.beapi.mapper;

import cd.beapi.dto.response.TreatmentCategoryResponse;
import cd.beapi.entity.TreatmentCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TreatmentCategoryMapper {
    TreatmentCategoryResponse toTreatmentCategoryResponse (TreatmentCategory treatmentCategory);

    List<TreatmentCategoryResponse> toTreatmentCategoryResponses(List<TreatmentCategory> treatmentCategories);
}
