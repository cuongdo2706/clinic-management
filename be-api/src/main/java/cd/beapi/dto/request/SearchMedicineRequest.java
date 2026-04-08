package cd.beapi.dto.request;

import cd.beapi.enumerate.MedicineSortOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchMedicineRequest extends PaginationFormat{
    MedicineSortOption sortBy;
    String codeKeyword;
    String nameKeyword;
}
