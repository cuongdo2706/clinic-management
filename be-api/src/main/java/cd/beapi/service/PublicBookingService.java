package cd.beapi.service;

import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;

import java.time.LocalDate;
import java.util.List;

public interface PublicBookingService {
    List<PublicDentistResponse> findDentists();

    List<PublicProcedureResponse> findProcedures();

    AvailableSlotResponse getAvailableSlots(Long dentistId, Long procedureId, LocalDate date, Integer durationMinutes);
}
