package cd.beapi.dto.request;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionRequest {
    Long doctorId;

    LocalDateTime prescribedAt;

    String advice;

    LocalDate reExaminationDate;

    String note;

    List<@Valid PrescriptionItemRequest> items;
}
