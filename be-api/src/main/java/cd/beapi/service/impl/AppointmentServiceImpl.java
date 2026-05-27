package cd.beapi.service.impl;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import cd.beapi.entity.QAppointment;
import cd.beapi.entity.Staff;
import cd.beapi.enumerate.AppointmentSortOption;
import cd.beapi.enumerate.AppointmentStatus;
import cd.beapi.enumerate.StaffType;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.AppointmentMapper;
import cd.beapi.repository.jpa.AppointmentRepository;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.service.AppointmentService;
import cd.beapi.service.QueueService;
import cd.beapi.service.SequenceService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final int SLOT_MINUTES = 15;
    private static final int DEFAULT_DURATION_MINUTES = 30;
    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(17, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<AppointmentStatus> IGNORED_CONFLICT_STATUSES = List.of(
            AppointmentStatus.CANCELLED,
            AppointmentStatus.NO_SHOW
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final SequenceService sequenceService;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    @Override
    public AppointmentResponse findById(Long id) {
        return appointmentMapper.toAppointmentResponse(findAppointment(id));
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<AppointmentResponse> search(SearchAppointmentRequest request) {
        QAppointment appointment = QAppointment.appointment;
        BooleanBuilder whereClause = new BooleanBuilder();

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            whereClause.and(
                    appointment.code.containsIgnoreCase(keyword)
                            .or(appointment.patient.fullName.containsIgnoreCase(keyword))
                            .or(appointment.patient.phone.containsIgnoreCase(keyword))
                            .or(appointment.patient.guardianPhone.containsIgnoreCase(keyword))
                            .or(appointment.dentist.fullName.containsIgnoreCase(keyword))
            );
        }
        if (StringUtils.hasText(request.getCodeKeyword())) {
            whereClause.and(appointment.code.containsIgnoreCase(request.getCodeKeyword().trim()));
        }
        if (StringUtils.hasText(request.getPatientKeyword())) {
            String keyword = request.getPatientKeyword().trim();
            whereClause.and(
                    appointment.patient.fullName.containsIgnoreCase(keyword)
                            .or(appointment.patient.phone.containsIgnoreCase(keyword))
                            .or(appointment.patient.guardianPhone.containsIgnoreCase(keyword))
            );
        }
        if (StringUtils.hasText(request.getDentistKeyword())) {
            whereClause.and(appointment.dentist.fullName.containsIgnoreCase(request.getDentistKeyword().trim()));
        }
        if (request.getStatus() != null) {
            whereClause.and(appointment.status.eq(request.getStatus()));
        }
        if (request.getDateFrom() != null) {
            whereClause.and(appointment.appointmentDate.goe(request.getDateFrom().atStartOfDay()));
        }
        if (request.getDateTo() != null) {
            whereClause.and(appointment.appointmentDate.lt(request.getDateTo().plusDays(1).atStartOfDay()));
        }

        Pageable pageable = PageRequest.of(
                request.getPage() == null ? 0 : request.getPage(),
                request.getSize() == null ? 10 : request.getSize(),
                resolveSort(request.getSortBy())
        );
        Page<Appointment> page = appointmentRepository.findAll(whereClause, pageable);
        return new PageData<>(
                page.getContent().stream().map(appointmentMapper::toAppointmentResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    @Override
    public AppointmentResponse save(CreateAppointmentRequest request) {
        Patient patient = findPatient(request.getPatientId());
        Staff dentist = request.getDentistId() == null ? null : findDentist(request.getDentistId());
        validateAppointmentTiming(request.getAppointmentDate(), request.getEstimatedDurationMinutes());
        validateDentistAvailable(dentist, request.getAppointmentDate(), request.getEstimatedDurationMinutes(), null);

        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .patient(patient)
                .dentist(dentist)
                .appointmentDate(request.getAppointmentDate())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .symptom(request.getSymptom())
                .note(request.getNote())
                .status(AppointmentStatus.CONFIRMED)
                .build();

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse update(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointment(id);
        ensureNotFinal(appointment);

        Patient patient = findPatient(request.getPatientId());
        Staff dentist = request.getDentistId() == null ? null : findDentist(request.getDentistId());
        validateAppointmentTiming(request.getAppointmentDate(), request.getEstimatedDurationMinutes());
        validateDentistAvailable(dentist, request.getAppointmentDate(), request.getEstimatedDurationMinutes(), id);

        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        appointment.setSymptom(request.getSymptom());
        appointment.setNote(request.getNote());
        if (request.getVersion() != null) {
            appointment.setVersion(request.getVersion());
        }

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse updateStatus(Long id, UpdateAppointmentStatusRequest request) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == request.getStatus()) {
            return appointmentMapper.toAppointmentResponse(appointment);
        }

        return switch (request.getStatus()) {
            case CONFIRMED -> confirm(id);
            case IN_QUEUE -> checkIn(id, null);
            case IN_PROGRESS -> start(id);
            case DONE -> done(id);
            case CANCELLED -> cancel(id);
            case NO_SHOW -> noShow(id);
            case PENDING -> throw new AppException("Appointment cannot be returned to pending status", HttpStatus.BAD_REQUEST);
        };
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        appointmentRepository.delete(appointment);
    }

    @Transactional
    @Override
    public AppointmentResponse confirm(Long id) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.PENDING);
        validateDentistAvailable(
                appointment.getDentist(),
                appointment.getAppointmentDate(),
                durationOf(appointment),
                id
        );
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse checkIn(Long id, CheckInAppointmentRequest request) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

        if (request != null && request.getDentistId() != null) {
            Staff dentist = findDentist(request.getDentistId());
            validateDentistAvailable(dentist, appointment.getAppointmentDate(), durationOf(appointment), id);
            appointment.setDentist(dentist);
        }
        if (request != null && request.getReceptionistId() != null) {
            appointment.setReceptionist(findReceptionist(request.getReceptionistId()));
        }

        appointment.setStatus(AppointmentStatus.IN_QUEUE);
        appointment.setSnapshotPatientName(appointment.getPatient().getFullName());
        appointment.setSnapshotPatientPhone(preferredPhone(appointment.getPatient()));
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse start(Long id) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.IN_QUEUE);
        if (appointment.getDentist() == null) {
            throw new AppException("Dentist is required before starting appointment", HttpStatus.BAD_REQUEST);
        }
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse done(Long id) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.IN_PROGRESS);
        appointment.setStatus(AppointmentStatus.DONE);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = findAppointment(id);
        ensureNotFinal(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse noShow(Long id) {
        Appointment appointment = findAppointment(id);
        requireStatus(appointment, AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    @Override
    public AvailableSlotResponse getAvailableSlots(Long dentistId, LocalDate date, Integer estimatedDurationMinutes) {
        findDentist(dentistId);
        validateDuration(estimatedDurationMinutes);
        if (date == null || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new AvailableSlotResponse(dentistId, date, List.of());
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Appointment> bookedAppointments = appointmentRepository.findActiveByDentistAndDateRange(
                dentistId,
                startOfDay,
                endOfDay,
                IGNORED_CONFLICT_STATUSES
        );
        List<String> slots = new ArrayList<>();
        LocalTime cursor = DEFAULT_WORK_START;
        while (!cursor.plusMinutes(estimatedDurationMinutes).isAfter(DEFAULT_WORK_END)) {
            LocalDateTime slotStart = LocalDateTime.of(date, cursor);
            LocalDateTime slotEnd = slotStart.plusMinutes(estimatedDurationMinutes);
            boolean isOccupied = bookedAppointments.stream().anyMatch(booked -> overlaps(slotStart, slotEnd, booked));
            if (!isOccupied) {
                slots.add(cursor.format(TIME_FORMATTER));
            }
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return new AvailableSlotResponse(dentistId, date, slots);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findByIdWithRelations(id).orElseThrow(
                () -> new AppException("Cannot find appointment with id: " + id, HttpStatus.BAD_REQUEST)
        );
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find patient with id: " + id, HttpStatus.BAD_REQUEST)
        );
    }

    private Staff findDentist(Long id) {
        Staff dentist = staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find dentist with id: " + id, HttpStatus.BAD_REQUEST)
        );
        if (dentist.getStaffType() != StaffType.DENTIST || !Boolean.TRUE.equals(dentist.getIsActive())) {
            throw new AppException("Selected staff is not an active dentist", HttpStatus.BAD_REQUEST);
        }
        return dentist;
    }

    private Staff findReceptionist(Long id) {
        Staff staff = staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find receptionist with id: " + id, HttpStatus.BAD_REQUEST)
        );
        if (!Boolean.TRUE.equals(staff.getIsActive())
                || (staff.getStaffType() != StaffType.RECEPTIONIST && staff.getStaffType() != StaffType.MANAGER)) {
            throw new AppException("Selected staff is not an active receptionist", HttpStatus.BAD_REQUEST);
        }
        return staff;
    }

    private void validateAppointmentTiming(LocalDateTime appointmentDate, Integer estimatedDurationMinutes) {
        if (appointmentDate == null) {
            throw new AppException("Appointment date is required", HttpStatus.BAD_REQUEST);
        }
        validateDuration(estimatedDurationMinutes);
        LocalTime time = appointmentDate.toLocalTime();
        if (time.getMinute() % SLOT_MINUTES != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new AppException("Appointment time minute must be 00, 15, 30 or 45", HttpStatus.BAD_REQUEST);
        }
        if (appointmentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new AppException("Appointments cannot be scheduled on Sunday", HttpStatus.BAD_REQUEST);
        }
        if (time.isBefore(DEFAULT_WORK_START) || time.plusMinutes(estimatedDurationMinutes).isAfter(DEFAULT_WORK_END)) {
            throw new AppException("Appointment time is outside working hours", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuration(Integer estimatedDurationMinutes) {
        if (estimatedDurationMinutes == null || estimatedDurationMinutes <= 0 || estimatedDurationMinutes % SLOT_MINUTES != 0) {
            throw new AppException("Estimated duration must be a positive multiple of 15 minutes", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDentistAvailable(Staff dentist,
                                          LocalDateTime appointmentDate,
                                          Integer estimatedDurationMinutes,
                                          Long excludeId) {
        if (dentist == null) {
            return;
        }
        LocalDate date = appointmentDate.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        LocalDateTime start = appointmentDate;
        LocalDateTime end = appointmentDate.plusMinutes(estimatedDurationMinutes);
        boolean hasConflict = appointmentRepository.findActiveByDentistAndDateRange(
                        dentist.getId(),
                        startOfDay,
                        endOfDay,
                        IGNORED_CONFLICT_STATUSES
                ).stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .anyMatch(existing -> overlaps(start, end, existing));
        if (hasConflict) {
            throw new AppException("Dentist already has an appointment at selected time", HttpStatus.CONFLICT);
        }
    }

    private boolean overlaps(LocalDateTime start, LocalDateTime end, Appointment existing) {
        LocalDateTime existingStart = existing.getAppointmentDate();
        LocalDateTime existingEnd = existingStart.plusMinutes(durationOf(existing));
        return start.isBefore(existingEnd) && existingStart.isBefore(end);
    }

    private int durationOf(Appointment appointment) {
        return appointment.getEstimatedDurationMinutes() == null
                ? DEFAULT_DURATION_MINUTES
                : appointment.getEstimatedDurationMinutes();
    }

    private void requireStatus(Appointment appointment, AppointmentStatus... allowedStatuses) {
        for (AppointmentStatus status : allowedStatuses) {
            if (appointment.getStatus() == status) {
                return;
            }
        }
        throw new AppException("Appointment status does not allow this action", HttpStatus.BAD_REQUEST);
    }

    private void ensureNotFinal(Appointment appointment) {
        requireStatus(
                appointment,
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.IN_QUEUE
        );
    }

    private String preferredPhone(Patient patient) {
        return patient.getPhone() != null ? patient.getPhone() : patient.getGuardianPhone();
    }

    private Sort resolveSort(AppointmentSortOption sortBy) {
        if (sortBy == null) {
            return Sort.by("appointmentDate").descending();
        }
        return switch (sortBy) {
            case APPOINTMENT_DATE -> Sort.by("appointmentDate");
            case APPOINTMENT_DATE_DESC -> Sort.by("appointmentDate").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };
    }

}
