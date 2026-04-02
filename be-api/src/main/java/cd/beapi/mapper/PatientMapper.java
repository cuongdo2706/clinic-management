package cd.beapi.mapper;

import cd.beapi.dto.response.PatientResponse;
import cd.beapi.entity.Patient;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponse toPatientResponse(Patient patient);

    List<PatientResponse> toPatientResponses(List<Patient>patients);
}
