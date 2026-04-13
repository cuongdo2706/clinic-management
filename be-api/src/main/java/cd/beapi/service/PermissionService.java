package cd.beapi.service;

import cd.beapi.dto.request.UpdatePermissionRequest;
import cd.beapi.dto.response.PermissionResponse;

public interface PermissionService {
    PermissionResponse getPermissionMatrix(Long roleId);

    PermissionResponse updatePermissions(UpdatePermissionRequest request);
}

