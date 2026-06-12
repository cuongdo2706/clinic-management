package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateProcedureCategoryRequest;
import cd.beapi.dto.request.SearchProcedureCategoryRequest;
import cd.beapi.dto.request.UpdateProcedureCategoryRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.ProcedureCategoryResponse;
import cd.beapi.service.ProcedureCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/procedure-categories")
public class ProcedureCategoryController {
    private final ProcedureCategoryService procedureCategoryService;

    @GetMapping("/{id}")
    public SuccessResponse<ProcedureCategoryResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), procedureCategoryService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<ProcedureCategoryResponse>> search(@Valid @RequestBody SearchProcedureCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), procedureCategoryService.search(request));
    }

    @PostMapping
    public SuccessResponse<ProcedureCategoryResponse> save(@Valid @RequestBody CreateProcedureCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), procedureCategoryService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<ProcedureCategoryResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody UpdateProcedureCategoryRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), procedureCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        procedureCategoryService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }
}

