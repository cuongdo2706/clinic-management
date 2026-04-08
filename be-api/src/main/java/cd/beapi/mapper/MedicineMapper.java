package cd.beapi.mapper;

import cd.beapi.dto.response.MedicineResponse;
import cd.beapi.entity.Medicine;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MedicineMapper {
    MedicineResponse toMedicineResponse(Medicine medicine);

    List<MedicineResponse> toMedicineResponses(List<Medicine> medicines);
}
