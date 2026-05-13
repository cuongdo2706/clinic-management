package cd.beapi.dto.request;

import cd.beapi.enumerate.StaffSortOption;
import cd.beapi.enumerate.StaffType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchStaffRequest extends PaginationFormat {
    String codeKeyword;
    String nameKeyword;
    String phoneKeyword;
    StaffType staffType;
    Boolean isActive;
    StaffSortOption sortBy;
}
