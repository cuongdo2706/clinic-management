package cd.beapi.mapper;

import cd.beapi.dto.response.ProcedureResponse;
import cd.beapi.entity.Procedure;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
        ProcedureCategoryMapper.class
})
public interface ProcedureMapper {
    ProcedureResponse toProcedureResponse(Procedure procedure);

    List<ProcedureResponse>toProcedureResponses(List<Procedure> procedures);
}
