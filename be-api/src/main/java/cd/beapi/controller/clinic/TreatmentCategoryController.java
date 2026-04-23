package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateTreatmentCategoryRequest;
import cd.beapi.dto.request.SearchTreatmentCategoryRequest;
import cd.beapi.dto.request.UpdateTreatmentCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TreatmentCategoryResponse;
import cd.beapi.service.TreatmentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/treatment-categories")
public class TreatmentCategoryController {
    private final TreatmentCategoryService treatmentCategoryService;

    @GetMapping("/{id}")
    public SuccessResponse<TreatmentCategoryResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentCategoryService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<TreatmentCategoryResponse>> search(@Valid @RequestBody SearchTreatmentCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), treatmentCategoryService.search(request));
    }

    @PostMapping
    public SuccessResponse<TreatmentCategoryResponse> save(@Valid @RequestBody CreateTreatmentCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), treatmentCategoryService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<TreatmentCategoryResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody UpdateTreatmentCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), treatmentCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        treatmentCategoryService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }
}

