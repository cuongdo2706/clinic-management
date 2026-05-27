package cd.beapi.dto.request;

import cd.beapi.enumerate.AppointmentSortOption;
import cd.beapi.enumerate.AppointmentStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchAppointmentRequest extends PaginationFormat {
    String keyword;
    String codeKeyword;
    String patientKeyword;
    String dentistKeyword;
    AppointmentStatus status;
    LocalDate dateFrom;
    LocalDate dateTo;
    AppointmentSortOption sortBy;
}
