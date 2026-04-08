package cd.beapi.mapper;

import cd.beapi.dto.response.PatientResponse;
import cd.beapi.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PatientMapper {
    PatientResponse toPatientResponse(Patient patient);

    List<PatientResponse> toPatientResponses(List<Patient>patients);
}
