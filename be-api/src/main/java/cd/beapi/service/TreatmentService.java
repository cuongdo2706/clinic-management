package cd.beapi.service;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.PrescriptionDetailResponse;
import cd.beapi.dto.response.TreatmentDetailResponse;
import cd.beapi.dto.response.TreatmentSummaryResponse;

import java.util.List;

public interface TreatmentService {
    TreatmentDetailResponse findById(Long id);

    List<TreatmentSummaryResponse> findByPatientId(Long patientId);

    List<PrescriptionDetailResponse> findPrescriptionsByPatientId(Long patientId);

    TreatmentDetailResponse save(CreateTreatmentRequest request);

    TreatmentDetailResponse update(Long id, UpdateTreatmentRequest request);
}
