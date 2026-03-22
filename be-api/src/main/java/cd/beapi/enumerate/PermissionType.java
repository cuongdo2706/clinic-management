package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PermissionType {
    VIEW("Xem"),
    CREATE("Tạo"),
    UPDATE("Sửa"),
    DELETE("Xóa"),
    PRINT("In");

    String label;

    PermissionType(String label) {
        this.label = label;
    }
}
