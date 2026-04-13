package cd.beapi.controller.clinic;

import cd.beapi.dto.response.RoleResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clinic/roles")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public SuccessResponse<List<RoleResponse>> getAllRoles() {
        return new SuccessResponse<>(HttpStatus.OK.value(), "Get data successfully",
                Instant.now(), roleService.getAllRoles());
    }
}

