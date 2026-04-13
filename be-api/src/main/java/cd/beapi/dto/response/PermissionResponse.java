package cd.beapi.dto.response;

import java.util.List;
import java.util.Map;

public record PermissionResponse(
        RoleResponse role,
        List<String> actions,
        List<PagePermission> pages
) {
    public record PagePermission(
            String pageCode,
            String pageName,
            Map<String, Boolean> granted
    ) {}
}
