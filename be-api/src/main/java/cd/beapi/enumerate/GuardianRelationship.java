package cd.beapi.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GuardianRelationship {
    FATHER("Cha"),
    MOTHER("Mẹ"),
    GRANDFATHER("Ông"),
    GRANDMOTHER("Bà"),
    SIBLING("Anh/Chị/Em"),
    UNCLE_AUNT("Chú/Dì/Cô/Cậu"),
    OTHER("Khác");

    private final String label;
}

