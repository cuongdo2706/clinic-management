package cd.beapi.dto.request;

import cd.beapi.enumerate.AccountStatus;
import cd.beapi.enumerate.AccountType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchAccountRequest extends PaginationFormat {
    AccountType type;
    String keyword;
    String roleCode;
    AccountStatus status;
}
