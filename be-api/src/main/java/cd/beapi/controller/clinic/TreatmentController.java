package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.PrescriptionDetailResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TreatmentDetailResponse;
import cd.beapi.dto.response.TreatmentSummaryResponse;
import cd.beapi.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/treatments")
public class TreatmentController {
    private final TreatmentService treatmentService;

    @GetMapping("/{id}")
    public SuccessResponse<TreatmentDetailResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentService.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    public SuccessResponse<List<TreatmentSummaryResponse>> findByPatientId(@PathVariable Long patientId) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentService.findByPatientId(patientId));
    }

    @GetMapping("/patient/{patientId}/prescriptions")
    public SuccessResponse<List<PrescriptionDetailResponse>> findPrescriptionsByPatientId(@PathVariable Long patientId) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentService.findPrescriptionsByPatientId(patientId));
    }

    @PostMapping
    public SuccessResponse<TreatmentDetailResponse> save(@Valid @RequestBody CreateTreatmentRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), treatmentService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<TreatmentDetailResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateTreatmentRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), treatmentService.update(id, request));
    }
}
