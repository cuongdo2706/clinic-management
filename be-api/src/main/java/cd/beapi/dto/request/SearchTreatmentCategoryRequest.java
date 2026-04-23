package cd.beapi.dto.request;

import cd.beapi.enumerate.TreatmentCategorySortOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchTreatmentCategoryRequest extends PaginationFormat {
    TreatmentCategorySortOption sortBy;
    String codeKeyword;
    String nameKeyword;
}