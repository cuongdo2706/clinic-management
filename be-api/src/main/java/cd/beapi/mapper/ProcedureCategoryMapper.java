package cd.beapi.mapper;

import cd.beapi.dto.response.ProcedureCategoryResponse;
import cd.beapi.entity.ProcedureCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProcedureCategoryMapper {
    ProcedureCategoryResponse toProcedureCategoryResponse (ProcedureCategory procedureCategory);

    List<ProcedureCategoryResponse> toProcedureCategoryResponses(List<ProcedureCategory> procedureCategories);
}
