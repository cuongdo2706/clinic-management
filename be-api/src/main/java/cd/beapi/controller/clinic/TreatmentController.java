package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateTreatmentRequest;
import cd.beapi.dto.request.SearchTreatmentRequest;
import cd.beapi.dto.request.UpdateTreatmentRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TreatmentResponse;
import cd.beapi.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/treatments")
public class TreatmentController {
    private final TreatmentService treatmentService;

    @GetMapping("/{id}")
    public SuccessResponse<TreatmentResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<TreatmentResponse>> search(@Valid @RequestBody SearchTreatmentRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentService.search(request));
    }

    @PostMapping
    public SuccessResponse<TreatmentResponse> save(@Valid @RequestBody CreateTreatmentRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), treatmentService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<TreatmentResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateTreatmentRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), treatmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        treatmentService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }
}

