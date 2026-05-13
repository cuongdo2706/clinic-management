package cd.beapi.service.impl;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import cd.beapi.entity.QAppointment;
import cd.beapi.entity.Staff;
import cd.beapi.entity.WorkingSchedule;
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
import cd.beapi.utility.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final int SLOT_MINUTES = 30;
    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(17, 0);
    private static final List<AppointmentStatus> IGNORED_CONFLICT_STATUSES = List.of(
            AppointmentStatus.CANCELLED,
            AppointmentStatus.NO_SHOW
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AppointmentMapper appointmentMapper;
    private final SequenceService sequenceService;
    private final QueueService queueService;

    @Transactional(readOnly = true)
    @Override
    public AppointmentResponse findById(Long id) {
        return appointmentMapper.toAppointmentResponse(findAppointment(id));
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<AppointmentResponse> search(SearchAppointmentRequest request) {
        QAppointment a = QAppointment.appointment;
        BooleanBuilder whereClause = new BooleanBuilder();

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = "%" + StringUtil.normalizeKeyword(request.getKeyword()) + "%";
            String rawKeyword = "%" + request.getKeyword() + "%";
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.code).like(keyword)
                            .or(Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.patient.fullName).like(keyword))
                            .or(Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.dentist.fullName).like(keyword))
                            .or(a.patient.phone.like(rawKeyword))
                            .or(a.patient.guardianPhone.like(rawKeyword))
            );
        }
        if (StringUtils.hasText(request.getCodeKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.code)
                            .like("%" + StringUtil.normalizeKeyword(request.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(request.getPatientKeyword())) {
            String keyword = "%" + StringUtil.normalizeKeyword(request.getPatientKeyword()) + "%";
            String rawKeyword = "%" + request.getPatientKeyword() + "%";
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.patient.fullName).like(keyword)
                            .or(a.patient.phone.like(rawKeyword))
                            .or(a.patient.guardianPhone.like(rawKeyword))
            );
        }
        if (StringUtils.hasText(request.getDentistKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", a.dentist.fullName)
                            .like("%" + StringUtil.normalizeKeyword(request.getDentistKeyword()) + "%")
            );
        }
        if (request.getStatus() != null) {
            whereClause.and(a.status.eq(request.getStatus()));
        }
        if (request.getDateFrom() != null) {
            whereClause.and(a.appointmentDate.goe(request.getDateFrom().atStartOfDay()));
        }
        if (request.getDateTo() != null) {
            whereClause.and(a.appointmentDate.lt(request.getDateTo().plusDays(1).atStartOfDay()));
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), resolveSort(request.getSortBy()));
        Page<Appointment> pages = appointmentRepository.findAll(whereClause, pageable);
        return new PageData<>(
                appointmentMapper.toAppointmentResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional
    @Override
    public AppointmentResponse save(CreateAppointmentRequest request) {
        Patient patient = findPatient(request.getPatientId());
        Staff dentist = request.getDentistId() == null ? null : findDentist(request.getDentistId());
        if (dentist != null) {
            validateDentistAvailable(dentist, request.getAppointmentDate(), null);
        }

        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .patient(patient)
                .dentist(dentist)
                .appointmentDate(request.getAppointmentDate())
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
        if (dentist != null) {
            validateDentistAvailable(dentist, request.getAppointmentDate(), id);
        }

        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setSymptom(request.getSymptom());
        appointment.setNote(request.getNote());
        if (request.getVersion() != null) {
            appointment.setVersion(request.getVersion());
        }

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
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
        if (appointment.getDentist() != null) {
            validateDentistAvailable(appointment.getDentist(), appointment.getAppointmentDate(), id);
        }
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
            validateDentistAvailable(dentist, appointment.getAppointmentDate(), id);
            appointment.setDentist(dentist);
        }
        if (request != null && request.getReceptionistId() != null) {
            appointment.setReceptionist(findReceptionist(request.getReceptionistId()));
        }

        appointment.setStatus(AppointmentStatus.IN_QUEUE);
        if (appointment.getQueueNumber() == null) {
            appointment.setQueueNumber(queueService.nextQueueNumberForDate(LocalDate.now()));
        }
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
    public AvailableSlotResponse getAvailableSlots(Long dentistId, LocalDate date) {
        Staff dentist = findDentist(dentistId);
        WorkingSchedule schedule = findScheduleForDate(dentist, date);
        if (schedule == null && hasConfiguredSchedules(dentist)) {
            return new AvailableSlotResponse(dentistId, date, List.of());
        }
        if (schedule == null && date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return new AvailableSlotResponse(dentistId, date, List.of());
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        Set<LocalTime> bookedTimes = appointmentRepository.findActiveByDentistAndDateRange(
                        dentistId,
                        startOfDay,
                        endOfDay,
                        IGNORED_CONFLICT_STATUSES
                ).stream()
                .map(appointment -> appointment.getAppointmentDate().toLocalTime())
                .collect(java.util.stream.Collectors.toSet());

        LocalTime startTime = schedule == null ? DEFAULT_WORK_START : schedule.getStartTime();
        LocalTime endTime = schedule == null ? DEFAULT_WORK_END : schedule.getEndTime();
        List<String> slots = scheduleSlots(startTime, endTime).stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .map(slot -> slot.toString().substring(0, 5))
                .toList();
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
        Staff dentist = staffRepository.findByIdWithWorkingSchedules(id).orElseThrow(
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

    private void validateDentistAvailable(Staff dentist, LocalDateTime appointmentDate, Long excludeId) {
        if (appointmentDate == null) {
            throw new AppException("Appointment date is required", HttpStatus.BAD_REQUEST);
        }
        boolean withinSchedule = isDentistWorkingAt(dentist, appointmentDate);
        if (!withinSchedule) {
            throw new AppException("Dentist is not working at selected time", HttpStatus.BAD_REQUEST);
        }
        long conflicts = appointmentRepository.countActiveAtDateTime(
                dentist.getId(),
                appointmentDate,
                excludeId,
                IGNORED_CONFLICT_STATUSES
        );
        if (conflicts > 0) {
            throw new AppException("Dentist already has an appointment at selected time", HttpStatus.CONFLICT);
        }
    }

    private boolean isWithinSchedule(LocalTime time, WorkingSchedule schedule) {
        return !time.isBefore(schedule.getStartTime()) && time.isBefore(schedule.getEndTime());
    }

    private boolean isDentistWorkingAt(Staff dentist, LocalDateTime appointmentDate) {
        WorkingSchedule schedule = findScheduleForDate(dentist, appointmentDate.toLocalDate());
        if (schedule != null) {
            return isWithinSchedule(appointmentDate.toLocalTime(), schedule);
        }
        if (hasConfiguredSchedules(dentist) || appointmentDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return false;
        }
        LocalTime time = appointmentDate.toLocalTime();
        return !time.isBefore(DEFAULT_WORK_START) && time.isBefore(DEFAULT_WORK_END);
    }

    private WorkingSchedule findScheduleForDate(Staff dentist, LocalDate date) {
        if (!hasConfiguredSchedules(dentist)) {
            return null;
        }
        return dentist.getWorkingSchedules().stream()
                .filter(item -> item.getDayOfWeek() == date.getDayOfWeek())
                .findFirst()
                .orElse(null);
    }

    private boolean hasConfiguredSchedules(Staff dentist) {
        return dentist.getWorkingSchedules() != null && !dentist.getWorkingSchedules().isEmpty();
    }

    private List<LocalTime> scheduleSlots(LocalTime startTime, LocalTime endTime) {
        java.util.ArrayList<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime cursor = startTime;
        while (cursor.isBefore(endTime)) {
            slots.add(cursor);
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return slots;
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
        return switch (sortBy) {
            case null -> Sort.by("appointmentDate").descending();
            case APPOINTMENT_DATE -> Sort.by("appointmentDate");
            case APPOINTMENT_DATE_DESC -> Sort.by("appointmentDate").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };
    }
}
