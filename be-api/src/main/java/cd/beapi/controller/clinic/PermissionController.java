package cd.beapi.controller.clinic;

import cd.beapi.dto.request.UpdatePermissionRequest;
import cd.beapi.dto.response.PermissionResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/{roleId}")
    public SuccessResponse<PermissionResponse> getMatrix(@PathVariable Long roleId) {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully",
                Instant.now(), permissionService.getPermissionMatrix(roleId));
    }

    @PutMapping
    public SuccessResponse<PermissionResponse> updateMatrix(
            @Valid @RequestBody UpdatePermissionRequest request) {
        return new SuccessResponse<>(HttpStatus.ACCEPTED.value(), "Update permissions successfully",
                Instant.now(), permissionService.updatePermissions(request));
    }
}
