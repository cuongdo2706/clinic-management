package cd.beapi.dto.response;

import java.util.List;

public record PatientDetailResponse(
        PatientInfoResponse patient,
        List<TreatmentSummaryResponse> treatments,
        List<PrescriptionDetailResponse> prescriptions,
        List<AppointmentResponse> appointments
) {
}
