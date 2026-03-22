package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum StaffType {
    DENTIST("Nha sĩ"),
    RECEPTIONIST("Lễ tân"),
    NURSE("Y tá"),
    ADMIN("Quản lý");

    String label;

    StaffType(String label) {
        this.label = label;
    }
}
