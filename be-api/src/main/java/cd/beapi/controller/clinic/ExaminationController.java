package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.SearchMedicineRequest;
import cd.beapi.dto.request.SearchProcedureRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.MedicineResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PatientDetailResponse;
import cd.beapi.dto.response.ProcedureResponse;
import cd.beapi.dto.response.StaffResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TreatmentDetailResponse;
import cd.beapi.service.AppointmentService;
import cd.beapi.service.MedicineService;
import cd.beapi.service.PatientService;
import cd.beapi.service.ProcedureService;
import cd.beapi.service.StaffService;
import cd.beapi.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/examinations")
public class ExaminationController {
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final TreatmentService treatmentService;
    private final ProcedureService procedureService;
    private final MedicineService medicineService;
    private final StaffService staffService;

    @GetMapping("/me")
    public SuccessResponse<StaffResponse> currentDentist(@AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                staffService.findCurrentDentist(username(jwt))
        );
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<AppointmentResponse>> searchAppointments(@Valid @RequestBody SearchAppointmentRequest request,
                                                                            @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                appointmentService.search(request, username(jwt), Set.of("DENTIST"))
        );
    }

    @PatchMapping("/{id}/start")
    public SuccessResponse<AppointmentResponse> start(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "Start appointment successfully",
                Instant.now(),
                appointmentService.start(id, username(jwt))
        );
    }

    @PatchMapping("/{id}/done")
    public SuccessResponse<AppointmentResponse> done(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "Complete appointment successfully",
                Instant.now(),
                appointmentService.done(id, username(jwt))
        );
    }

    @GetMapping("/patients/{patientId}/detail")
    public SuccessResponse<PatientDetailResponse> patientDetail(@PathVariable Long patientId) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                patientService.findDetail(patientId)
        );
    }

    @PostMapping("/procedures/search")
    public SuccessResponse<PageData<ProcedureResponse>> searchProcedures(@Valid @RequestBody SearchProcedureRequest request) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                procedureService.search(request)
        );
    }

    @PostMapping("/medicines/search")
    public SuccessResponse<PageData<MedicineResponse>> searchMedicines(@Valid @RequestBody SearchMedicineRequest request) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                medicineService.search(request)
        );
    }

    @GetMapping("/treatments/{id}")
    public SuccessResponse<TreatmentDetailResponse> findTreatmentById(@PathVariable Long id) {
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                treatmentService.findById(id)
        );
    }

    @PostMapping("/treatments")
    public SuccessResponse<TreatmentDetailResponse> saveTreatment(@Valid @RequestBody CreateTreatmentRequest request) {
        return new SuccessResponse<>(
                HttpStatus.CREATED.value(),
                "Create data successfully",
                Instant.now(),
                treatmentService.save(request)
        );
    }

    @PutMapping("/treatments/{id}")
    public SuccessResponse<TreatmentDetailResponse> updateTreatment(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateTreatmentRequest request) {
        return new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "Update data successfully",
                Instant.now(),
                treatmentService.update(id, request)
        );
    }

    private String username(Jwt jwt) {
        return jwt == null ? "system" : jwt.getSubject();
    }
}
