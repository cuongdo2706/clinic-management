package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateMedicineRequest;
import cd.beapi.dto.request.CreatePatientRequest;
import cd.beapi.dto.request.SearchMedicineRequest;
import cd.beapi.dto.request.UpdateMedicineRequest;
import cd.beapi.dto.response.MedicineResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/medicines")
public class MedicineController {
    private final MedicineService medicineService;

    @GetMapping("/{id}")
    public SuccessResponse<MedicineResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), medicineService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<MedicineResponse>> search(@Valid @RequestBody SearchMedicineRequest searchMedicineRequest) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), medicineService.search(searchMedicineRequest));
    }

    @PostMapping
    public SuccessResponse<MedicineResponse> save(@Valid @RequestBody CreateMedicineRequest createPatientRequest) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), medicineService.save(createPatientRequest));
    }

    @PutMapping("/{id}")
    public SuccessResponse<MedicineResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateMedicineRequest updateMedicineRequest) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), medicineService.update(id, updateMedicineRequest));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel() {
        String filename = "Danh_Sach_Thuoc_" + LocalDate.now() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(medicineService.exportExcel());
    }
}
