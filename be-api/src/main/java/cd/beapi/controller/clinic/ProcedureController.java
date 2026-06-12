package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateProcedureRequest;
import cd.beapi.dto.request.SearchProcedureRequest;
import cd.beapi.dto.request.UpdateProcedureRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.ProcedureResponse;
import cd.beapi.service.ProcedureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/procedures")
public class ProcedureController {
    private final ProcedureService procedureService;

    @GetMapping("/{id}")
    public SuccessResponse<ProcedureResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), procedureService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<ProcedureResponse>> search(@Valid @RequestBody SearchProcedureRequest request) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), procedureService.search(request));
    }

    @PostMapping
    public SuccessResponse<ProcedureResponse> save(@Valid @RequestBody CreateProcedureRequest request) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), procedureService.save(request));
    }

    @PutMapping("/{id}")
    public SuccessResponse<ProcedureResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateProcedureRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), procedureService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        procedureService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }
}

