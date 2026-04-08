package cd.beapi.dto.request;

import cd.beapi.enumerate.PatientSortOption;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchPatientRequest extends PaginationFormat {
    PatientSortOption sortBy;
    String codeKeyword;
    String nameKeyword;
    String phoneKeyword;
    String guardianNameKeyword;
    String guardianPhoneKeyword;
}
