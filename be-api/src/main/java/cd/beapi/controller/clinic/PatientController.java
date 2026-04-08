package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreatePatientRequest;
import cd.beapi.dto.request.SearchPatientRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/clinic/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @GetMapping("/{id}")
    public SuccessResponse<PatientResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), patientService.findById(id));
    }

    @PostMapping("/search")
    public SuccessResponse<PageData<PatientResponse>> search(@RequestBody SearchPatientRequest searchPatientRequest) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), patientService.search(searchPatientRequest));
    }

    @PostMapping
    public SuccessResponse<PatientResponse> save(@RequestBody CreatePatientRequest createPatientRequest) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Save data successfully", Instant.now(), patientService.save(createPatientRequest));
    }

    @PutMapping("/{id}")
    public SuccessResponse<PatientResponse> update(@PathVariable Long id,
                                                   @RequestBody UpdatePatientRequest updatePatientRequest) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), patientService.update(id, updatePatientRequest));
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<?> delete(@PathVariable Long id) {
        patientService.delete(id);
        return new SuccessResponse<>(HttpStatus.NO_CONTENT.value(), "Delete data successfully", Instant.now(), null);
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportExcel() {
        String filename = "Danh_Sach_Benh_Nhan_" + LocalDate.now() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(patientService.exportExcel());
    }
}
