package cd.beapi.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class PaginationFormat {
    @PositiveOrZero(message = "PageType must be greater than or equal 0")
    Integer page = 0;
    @Positive(message = "PageType must be greater than 0")
    Integer size = 10;
}
