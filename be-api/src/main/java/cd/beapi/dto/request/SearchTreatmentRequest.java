package cd.beapi.dto.request;

import cd.beapi.enumerate.TreatmentSortOption;
import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchTreatmentRequest extends PaginationFormat {
    TreatmentSortOption sortBy;
    String codeKeyword;
    String nameKeyword;
    BigDecimal priceFrom;
    BigDecimal priceTo;

    @AssertTrue(message = "priceFrom must be less than or equal to priceTo")
    public boolean isPriceRangeValid() {
        if (priceFrom == null || priceTo == null) return true;
        return priceFrom.compareTo(priceTo) <= 0;
    }
}
