package cd.beapi.service.impl;

import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.PublicProcedureResponse;
import cd.beapi.entity.Procedure;
import cd.beapi.enumerate.StaffType;
import cd.beapi.exception.AppException;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.repository.jpa.ProcedureRepository;
import cd.beapi.service.AppointmentService;
import cd.beapi.service.PublicBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PublicBookingServiceImpl implements PublicBookingService {
    private static final int DEFAULT_DURATION_MINUTES = 30;

    private final AppointmentService appointmentService;
    private final StaffRepository staffRepository;
    private final ProcedureRepository procedureRepository;

    @Override
    public List<PublicDentistResponse> findDentists() {
        return staffRepository.findActiveByStaffType(StaffType.DENTIST).stream()
                .map(staff -> new PublicDentistResponse(
                        staff.getId(),
                        staff.getCode(),
                        staff.getFullName(),
                        staff.getAvatarUrl()
                ))
                .toList();
    }

    @Override
    public List<PublicProcedureResponse> findProcedures() {
        return procedureRepository.findByIsActiveTrueOrderByName().stream()
                .map(this::toProcedureResponse)
                .toList();
    }

    @Override
    public AvailableSlotResponse getAvailableSlots(Long dentistId, Long procedureId, LocalDate date, Integer durationMinutes) {
        int resolvedDuration = resolveDuration(procedureId, durationMinutes);
        return appointmentService.getAvailableSlots(dentistId, date, resolvedDuration);
    }

    private PublicProcedureResponse toProcedureResponse(Procedure procedure) {
        return new PublicProcedureResponse(
                procedure.getId(),
                procedure.getCode(),
                procedure.getName(),
                procedure.getPrice(),
                procedure.getUnit(),
                Objects.requireNonNullElse(procedure.getDurationMinutes(), DEFAULT_DURATION_MINUTES)
        );
    }

    private int resolveDuration(Long procedureId, Integer durationMinutes) {
        if (durationMinutes != null) {
            return durationMinutes;
        }
        if (procedureId == null) {
            return DEFAULT_DURATION_MINUTES;
        }
        Procedure procedure = procedureRepository.findById(procedureId).orElseThrow(
                () -> new AppException("Cannot find procedure with id: " + procedureId, HttpStatus.BAD_REQUEST)
        );
        return Objects.requireNonNullElse(procedure.getDurationMinutes(), DEFAULT_DURATION_MINUTES);
    }
}
