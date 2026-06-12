package cd.beapi.dto.request;

import cd.beapi.enumerate.TreatmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTreatmentRequest {
    @NotNull(message = "Patient must not be null")
    Long patientId;

    Long appointmentId;

    Long doctorId;

    String diagnosis;

    String note;

    LocalDateTime treatmentDate;

    TreatmentStatus status;

    @Valid
    PrescriptionRequest prescription;

    List<@Valid TreatmentProcedureRequest> procedures;
}
