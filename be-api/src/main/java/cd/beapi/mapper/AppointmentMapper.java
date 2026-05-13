package cd.beapi.mapper;

import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Staff;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentMapper {
    public AppointmentResponse toAppointmentResponse(Appointment appointment) {
        Patient patient = appointment.getPatient();
        Staff dentist = appointment.getDentist();
        Staff receptionist = appointment.getReceptionist();

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCode(),
                appointment.getAppointmentDate(),
                appointment.getCreatedAt(),
                appointment.getModifiedAt(),
                appointment.getSymptom(),
                appointment.getNote(),
                appointment.getStatus(),
                appointment.getQueueNumber(),
                appointment.getVersion(),
                patient == null ? null : patient.getId(),
                patient == null ? null : patient.getCode(),
                patient == null ? null : patient.getFullName(),
                patient == null ? null : preferredPhone(patient),
                dentist == null ? null : dentist.getId(),
                dentist == null ? null : dentist.getCode(),
                dentist == null ? null : dentist.getFullName(),
                receptionist == null ? null : receptionist.getId(),
                receptionist == null ? null : receptionist.getFullName(),
                appointment.getSnapshotPatientName(),
                appointment.getSnapshotPatientPhone()
        );
    }

    public List<AppointmentResponse> toAppointmentResponses(List<Appointment> appointments) {
        return appointments.stream().map(this::toAppointmentResponse).toList();
    }

    private String preferredPhone(Patient patient) {
        return patient.getPhone() != null ? patient.getPhone() : patient.getGuardianPhone();
    }
}
