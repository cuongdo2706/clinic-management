package cd.beapi.controller.clinic;

import cd.beapi.dto.request.CreateStaffRequest;
import cd.beapi.dto.request.SearchStaffRequest;
import cd.beapi.dto.request.UpdateStaffRequest;
import cd.beapi.dto.request.UpdateStaffStatusRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.StaffResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/staffs")
public class StaffController {
    private final StaffService staffService;

//    @PreAuthorize("hasAnyAuthority('STAFF:VIEW')")
    @GetMapping("/dentist-options")
    public SuccessResponse<List<PublicDentistResponse>> findDentistOptions() {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), staffService.findDentistOptions());
    }

//    @PreAuthorize("hasAnyAuthority('STAFF:VIEW')")
    @GetMapping("/{id}")
    public SuccessResponse<StaffResponse> findById(@PathVariable Long id) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), staffService.findById(id));
    }

//    @PreAuthorize("hasAnyAuthority('STAFF:VIEW')")
    @PostMapping("/search")
    public SuccessResponse<PageData<StaffResponse>> search(@Valid @RequestBody SearchStaffRequest searchStaffRequest) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully", Instant.now(), staffService.search(searchStaffRequest));
    }

//    @PreAuthorize("hasAnyAuthority('STAFF:CREATE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<StaffResponse> save(@Valid @RequestPart("request") CreateStaffRequest createStaffRequest,
                                               @RequestPart(value = "file", required = false) MultipartFile file) {
        return new SuccessResponse<>(HttpStatus.CREATED.value(), "Create data successfully", Instant.now(), staffService.save(createStaffRequest, file));
    }

//    @PreAuthorize("hasAnyAuthority('STAFF:UPDATE')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<StaffResponse> update(@PathVariable Long id,
                                                 @Valid @RequestPart("request") UpdateStaffRequest updateStaffRequest,
                                                 @RequestPart(value = "file", required = false) MultipartFile file) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update data successfully", Instant.now(), staffService.update(id, updateStaffRequest, file));
    }

//    @PreAuthorize("hasAnyAuthority('STAFF:UPDATE')")
    @PatchMapping("/{id}/status")
    public SuccessResponse<StaffResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateStaffStatusRequest updateStaffStatusRequest) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update status successfully", Instant.now(), staffService.updateStatus(id, updateStaffStatusRequest));
    }
}
