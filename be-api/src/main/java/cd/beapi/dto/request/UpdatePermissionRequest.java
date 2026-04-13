package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePermissionRequest {

    @NotNull
    Long roleId;

    List<PermissionEntry> permissions;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PermissionEntry {
        @NotNull
        String pageCode;
        @NotNull
        String actionCode;
    }
}

