package cd.beapi.mapper;

import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppointmentMapper {

    @Mapping(target = "estimatedDurationMinutes", expression = "java(resolveDuration(appointment))")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.code")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "patientPhone", expression = "java(resolvePatientPhone(appointment.getPatient()))")
    @Mapping(target = "dentistId", source = "dentist.id")
    @Mapping(target = "dentistCode", source = "dentist.code")
    @Mapping(target = "dentistName", source = "dentist.fullName")
    @Mapping(target = "receptionistId", source = "receptionist.id")
    @Mapping(target = "receptionistName", source = "receptionist.fullName")
    AppointmentResponse toAppointmentResponse(Appointment appointment);

    default Integer resolveDuration(Appointment appointment) {
        return appointment.getEstimatedDurationMinutes() == null
                ? 30
                : appointment.getEstimatedDurationMinutes();
    }

    default String resolvePatientPhone(Patient patient) {
        if (patient == null) {
            return null;
        }
        return patient.getPhone() != null ? patient.getPhone() : patient.getGuardianPhone();
    }
}
