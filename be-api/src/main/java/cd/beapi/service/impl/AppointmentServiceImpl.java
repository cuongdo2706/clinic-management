package cd.beapi.service.impl;

import cd.beapi.dto.request.CheckInAppointmentRequest;
import cd.beapi.dto.request.ClientBookingRequest;
import cd.beapi.dto.request.CreateAppointmentRequest;
import cd.beapi.dto.request.PublicBookingRequest;
import cd.beapi.dto.request.SearchAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentArrivalStatusRequest;
import cd.beapi.dto.request.UpdateAppointmentRequest;
import cd.beapi.dto.request.UpdateAppointmentStatusRequest;
import cd.beapi.dto.response.AppointmentResponse;
import cd.beapi.dto.response.AvailableSlotItemResponse;
import cd.beapi.dto.response.AvailableSlotResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.entity.Appointment;
import cd.beapi.entity.Patient;
import cd.beapi.entity.QAppointment;
import cd.beapi.entity.Staff;
import cd.beapi.entity.Procedure;
import cd.beapi.entity.StaffWorkingSchedule;
import cd.beapi.enumerate.AppointmentSortOption;
import cd.beapi.enumerate.AppointmentArrivalStatus;
import cd.beapi.enumerate.AppointmentStatus;
import cd.beapi.enumerate.StaffType;
import cd.beapi.enumerate.TreatmentStatus;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.AppointmentMapper;
import cd.beapi.repository.jpa.AppointmentRepository;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.repository.jpa.ProcedureRepository;
import cd.beapi.repository.jpa.TreatmentRepository;
import cd.beapi.service.AppointmentService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final int SLOT_MINUTES = 15;
    private static final int DEFAULT_DURATION_MINUTES = 30;
    private static final int MAX_AGE_UNDER_SUPERVISION = 14;
    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(20, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<AppointmentStatus> IGNORED_CONFLICT_STATUSES = List.of(
            AppointmentStatus.CANCELLED
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final ProcedureRepository procedureRepository;
    private final TreatmentRepository treatmentRepository;
    private final SequenceService sequenceService;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    @Override
    public AppointmentResponse findById(Long id, String username, Set<String> roles) {
        Appointment appointment = findAppointment(id);
        requireVisibleToCurrentUser(appointment, username, roles);
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    @Override
    public PageData<AppointmentResponse> search(SearchAppointmentRequest request, String username, Set<String> roles) {
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
        if (isRestrictedDentist(username, roles)) {
            whereClause.and(appointment.dentist.user.username.eq(username));
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
        Staff dentist = findDentist(request.getDentistId());
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
                .arrivalStatus(AppointmentArrivalStatus.NOT_ARRIVED)
                .build();

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse saveForPatient(Long patientId, ClientBookingRequest request) {
        Patient patient = findPatient(patientId);
        Staff dentist = findDentist(request.getDentistId());
        Procedure procedure = request.getProcedureId() == null ? null : findActiveProcedure(request.getProcedureId());
        int durationMinutes = resolvePublicDuration(request.getDurationMinutes(), procedure);

        validateAppointmentTiming(request.getAppointmentDate(), durationMinutes);
        validateClientBookingDate(request.getAppointmentDate());
        validateDentistAvailable(dentist, request.getAppointmentDate(), durationMinutes, null);

        String symptom = StringUtils.hasText(request.getSymptom())
                ? request.getSymptom()
                : procedure == null ? null : procedure.getName();
        String note = request.getNote();
        if (procedure != null && !StringUtils.hasText(note)) {
            note = "Dịch vụ đặt trước: " + procedure.getName();
        }

        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .patient(patient)
                .dentist(dentist)
                .appointmentDate(request.getAppointmentDate())
                .estimatedDurationMinutes(durationMinutes)
                .symptom(symptom)
                .note(note)
                .status(AppointmentStatus.PENDING)
                .arrivalStatus(AppointmentArrivalStatus.NOT_ARRIVED)
                .build();

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse savePublic(PublicBookingRequest request) {
        Patient patient = findOrCreatePublicPatient(request);
        Staff dentist = findDentist(request.getDentistId());
        Procedure procedure = request.getProcedureId() == null ? null : findActiveProcedure(request.getProcedureId());
        int durationMinutes = resolvePublicDuration(request.getDurationMinutes(), procedure);

        validateAppointmentTiming(request.getAppointmentDate(), durationMinutes);
        validateClientBookingDate(request.getAppointmentDate());
        validateDentistAvailable(dentist, request.getAppointmentDate(), durationMinutes, null);

        String symptom = StringUtils.hasText(request.getSymptom())
                ? request.getSymptom()
                : procedure == null ? null : procedure.getName();
        String note = request.getNote();
        if (procedure != null && !StringUtils.hasText(note)) {
            note = "Dịch vụ đặt trước: " + procedure.getName();
        }

        Appointment appointment = Appointment.builder()
                .code(sequenceService.generateAppointmentCode())
                .patient(patient)
                .dentist(dentist)
                .appointmentDate(request.getAppointmentDate())
                .estimatedDurationMinutes(durationMinutes)
                .symptom(symptom)
                .note(note)
                .status(AppointmentStatus.PENDING)
                .arrivalStatus(AppointmentArrivalStatus.NOT_ARRIVED)
                .build();

        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse update(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointment(id);

        Patient patient = findPatient(request.getPatientId());
        Staff dentist = findDentist(request.getDentistId());
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

    @Override
    public AppointmentResponse updateStatus(Long id, UpdateAppointmentStatusRequest request, String changedBy, Set<String> roles) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        switch (request.getStatus()) {
            case IN_PROGRESS, COMPLETED ->
                    throw new AppException("Vui lòng xử lý trạng thái khám bệnh tại màn Khám bệnh", HttpStatus.BAD_REQUEST);
            case CANCELLED -> {
                return cancel(id, changedBy);
            }
            case CONFIRMED -> {
                requireStatus(appointment, "Chỉ lịch chờ xác nhận hoặc đã xác nhận mới được xác nhận",
                        AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
                requireArrivalStatus(appointment, "Không thể xác nhận lịch khi đã xử lý trạng thái đến khám",
                        AppointmentArrivalStatus.NOT_ARRIVED);
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                if (appointment.getArrivalStatus() == null) {
                    appointment.setArrivalStatus(AppointmentArrivalStatus.NOT_ARRIVED);
                }
            }
            case PENDING -> {
                requireStatus(appointment, "Chỉ lịch chờ xác nhận hoặc đã xác nhận mới được chuyển về chờ xác nhận",
                        AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
                requireArrivalStatus(appointment, "Không thể chuyển về chờ xác nhận khi đã xử lý trạng thái đến khám",
                        AppointmentArrivalStatus.NOT_ARRIVED);
                appointment.setStatus(AppointmentStatus.PENDING);
            }
        }
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse updateArrivalStatus(Long id, UpdateAppointmentArrivalStatusRequest request, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        appointment.setArrivalStatus(request.getArrivalStatus());
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }


    @Transactional
    @Override
    public void delete(Long id) {
        Appointment appointment = findAppointment(id);
        appointmentRepository.delete(appointment);
    }

    @Transactional
    @Override
    public AppointmentResponse confirm(Long id) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch chờ xác nhận mới được xác nhận", AppointmentStatus.PENDING);
        requireArrivalStatus(appointment, "Không thể xác nhận lịch khi đã xử lý trạng thái đến khám",
                AppointmentArrivalStatus.NOT_ARRIVED);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        if (appointment.getArrivalStatus() == null) {
            appointment.setArrivalStatus(AppointmentArrivalStatus.NOT_ARRIVED);
        }
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse checkIn(Long id, CheckInAppointmentRequest request, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch đã xác nhận mới được ghi nhận đến khám", AppointmentStatus.CONFIRMED);
        requireArrivalStatus(appointment, "Lịch này đã được xử lý trạng thái đến khám",
                AppointmentArrivalStatus.NOT_ARRIVED);
        if (request != null && request.getDentistId() != null) {
            Staff dentist = findDentist(request.getDentistId());
            validateDentistAvailable(dentist, appointment.getAppointmentDate(), durationOf(appointment), id);
            appointment.setDentist(dentist);
        }
        if (request != null && request.getReceptionistId() != null) {
            appointment.setReceptionist(findReceptionist(request.getReceptionistId()));
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setArrivalStatus(AppointmentArrivalStatus.ARRIVED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse start(Long id, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireOwnAppointmentIfDentist(appointment, changedBy);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch đã xác nhận mới được bắt đầu khám", AppointmentStatus.CONFIRMED);
        requireArrivalStatus(appointment, "Chỉ bệnh nhân đã đến mới được bắt đầu khám", AppointmentArrivalStatus.ARRIVED);
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setArrivalStatus(AppointmentArrivalStatus.ARRIVED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse done(Long id, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireOwnAppointmentIfDentist(appointment, changedBy);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch đang khám mới được kết thúc khám", AppointmentStatus.IN_PROGRESS);
        var treatment = treatmentRepository.findByAppointmentId(id).orElseThrow(
                () -> new AppException("Vui lòng lưu phiếu khám trước khi kết thúc khám", HttpStatus.BAD_REQUEST)
        );
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setArrivalStatus(AppointmentArrivalStatus.ARRIVED);
        treatment.setStatus(TreatmentStatus.COMPLETED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse cancel(Long id, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch chờ xác nhận hoặc đã xác nhận mới được hủy",
                AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        requireArrivalStatus(appointment, "Không thể hủy lịch khi đã xử lý trạng thái đến khám",
                AppointmentArrivalStatus.NOT_ARRIVED);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    @Override
    public AppointmentResponse noShow(Long id, String changedBy) {
        Appointment appointment = findAppointment(id);
        requireAppointmentToday(appointment);
        requireStatus(appointment, "Chỉ lịch đã xác nhận mới được đánh dấu không đến", AppointmentStatus.CONFIRMED);
        requireArrivalStatus(appointment, "Chỉ lịch chưa đến mới được đánh dấu không đến",
                AppointmentArrivalStatus.NOT_ARRIVED);
        appointment.setArrivalStatus(AppointmentArrivalStatus.NO_SHOW);
        return appointmentMapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }


    @Transactional(readOnly = true)
    @Override
    public AvailableSlotResponse getAvailableSlots(Long dentistId, LocalDate date, Integer estimatedDurationMinutes) {
        Staff dentist = findDentist(dentistId);
        int durationMinutes = normalizeDuration(estimatedDurationMinutes);
        if (date == null) {
            return new AvailableSlotResponse(dentistId, date, durationMinutes, SLOT_MINUTES, List.of(), List.of());
        }
        EffectiveWorkingSchedule workingSchedule = resolveWorkingSchedule(dentist, date.getDayOfWeek());
        if (!workingSchedule.working()) {
            return new AvailableSlotResponse(dentistId, date, durationMinutes, SLOT_MINUTES, List.of(), List.of());
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
        List<AvailableSlotItemResponse> slotDetails = new ArrayList<>();
        LocalTime cursor = workingSchedule.startTime();
        LocalDateTime now = LocalDateTime.now();
        while (!cursor.isAfter(workingSchedule.endTime().minusMinutes(SLOT_MINUTES))) {
            LocalDateTime slotStart = LocalDateTime.of(date, cursor);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            boolean inWorkingBlock = !slotEnd.toLocalTime().isAfter(workingSchedule.endTime());
            boolean afterCurrentTime = slotStart.isAfter(now);
            boolean isOccupied = bookedAppointments.stream().anyMatch(booked -> overlaps(slotStart, slotEnd, booked));
            boolean available = inWorkingBlock && afterCurrentTime && !isOccupied;
            String reason = null;
            if (!inWorkingBlock) {
                reason = "Ngoài giờ làm việc";
            } else if (!afterCurrentTime) {
                reason = "Đã qua giờ đặt lịch";
            } else if (isOccupied) {
                reason = "Bác sĩ đã có lịch";
            }
            if (available) {
                slots.add(cursor.format(TIME_FORMATTER));
            }
            if (inWorkingBlock) {
                slotDetails.add(new AvailableSlotItemResponse(
                        cursor.format(TIME_FORMATTER),
                        slotStart,
                        slotEnd,
                        available,
                        reason
                ));
            }
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return new AvailableSlotResponse(dentistId, date, durationMinutes, SLOT_MINUTES, slots, slotDetails);
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

    private Patient findOrCreatePublicPatient(PublicBookingRequest request) {
        String phone = request.getPhone().trim();
        return patientRepository.findFirstByPhoneOrGuardianPhone(phone, phone)
                .orElseGet(() -> createPublicPatient(request, phone));
    }

    private Patient createPublicPatient(PublicBookingRequest request, String phone) {
        Patient patient = Patient.builder()
                .code(sequenceService.generatePatientCode())
                .fullName(request.getFullName().trim())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();

        long age = ChronoUnit.YEARS.between(request.getDob(), LocalDate.now());
        if (age < MAX_AGE_UNDER_SUPERVISION) {
            if (!StringUtils.hasText(request.getGuardianName())) {
                throw new AppException("Guardian name is required for patients under 14", HttpStatus.BAD_REQUEST);
            }
            patient.setGuardianName(request.getGuardianName().trim());
            patient.setGuardianPhone(StringUtils.hasText(request.getGuardianPhone())
                    ? request.getGuardianPhone().trim()
                    : phone);
        } else {
            patient.setPhone(phone);
        }

        return patientRepository.save(patient);
    }

    private Procedure findActiveProcedure(Long id) {
        Procedure procedure = procedureRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find procedure with id: " + id, HttpStatus.BAD_REQUEST)
        );
        if (!Boolean.TRUE.equals(procedure.getIsActive())) {
            throw new AppException("Selected procedure is inactive", HttpStatus.BAD_REQUEST);
        }
        return procedure;
    }

    private int resolvePublicDuration(Integer requestedDuration, Procedure procedure) {
        if (requestedDuration != null) {
            return normalizeDuration(requestedDuration);
        }
        if (procedure != null && procedure.getDurationMinutes() != null) {
            return normalizeDuration(procedure.getDurationMinutes());
        }
        return DEFAULT_DURATION_MINUTES;
    }

    private Staff findDentist(Long id) {
        if (id == null) {
            throw new AppException("Vui lòng chọn nha sĩ", HttpStatus.BAD_REQUEST);
        }
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
    }

    private void validateClientBookingDate(LocalDateTime appointmentDate) {
        if (!appointmentDate.toLocalDate().isAfter(LocalDate.now())) {
            throw new AppException("Website khách hàng chỉ cho phép đặt lịch từ ngày mai", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDuration(Integer estimatedDurationMinutes) {
        if (estimatedDurationMinutes == null || estimatedDurationMinutes <= 0 || estimatedDurationMinutes % SLOT_MINUTES != 0) {
            throw new AppException("Estimated duration must be a positive multiple of 15 minutes", HttpStatus.BAD_REQUEST);
        }
    }

    private int normalizeDuration(Integer estimatedDurationMinutes) {
        int duration = Objects.requireNonNullElse(estimatedDurationMinutes, DEFAULT_DURATION_MINUTES);
        validateDuration(duration);
        return duration;
    }

    private void validateDentistAvailable(Staff dentist,
                                          LocalDateTime appointmentDate,
                                          Integer estimatedDurationMinutes,
                                          Long excludeId) {
        if (dentist == null) {
            return;
        }
        validateDentistWorkingTime(dentist, appointmentDate, estimatedDurationMinutes);
        LocalDate date = appointmentDate.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        LocalDateTime end = appointmentDate.plusMinutes(estimatedDurationMinutes);
        boolean hasConflict = appointmentRepository.findActiveByDentistAndDateRange(
                        dentist.getId(),
                        startOfDay,
                        endOfDay,
                        IGNORED_CONFLICT_STATUSES
                ).stream()
                .filter(existing -> !existing.getId().equals(excludeId))
                .anyMatch(existing -> overlaps(appointmentDate, end, existing));
        if (hasConflict) {
            throw new AppException("Nha sĩ đã có lịch trong khung giờ đã chọn", HttpStatus.CONFLICT);
        }
    }

    private void validateDentistWorkingTime(Staff dentist, LocalDateTime appointmentDate, Integer estimatedDurationMinutes) {
        EffectiveWorkingSchedule schedule = resolveWorkingSchedule(dentist, appointmentDate.getDayOfWeek());
        if (!schedule.working()) {
            throw new AppException("Nha sĩ không làm việc vào ngày đã chọn", HttpStatus.BAD_REQUEST);
        }

        LocalTime startTime = appointmentDate.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(estimatedDurationMinutes);
        if (startTime.isBefore(schedule.startTime()) || endTime.isAfter(schedule.endTime())) {
            throw new AppException("Thời gian hẹn nằm ngoài lịch làm việc của nha sĩ", HttpStatus.BAD_REQUEST);
        }
    }

    private EffectiveWorkingSchedule resolveWorkingSchedule(Staff staff, DayOfWeek dayOfWeek) {
        Map<DayOfWeek, StaffWorkingSchedule> scheduleByDay = staff.getWorkingSchedules() == null
                ? Map.of()
                : staff.getWorkingSchedules().stream()
                .filter(schedule -> schedule.getDayOfWeek() != null)
                .sorted(Comparator.comparing(StaffWorkingSchedule::getDayOfWeek))
                .collect(Collectors.toMap(StaffWorkingSchedule::getDayOfWeek, Function.identity(), (first, second) -> second));
        StaffWorkingSchedule schedule = scheduleByDay.get(dayOfWeek);
        if (schedule == null) {
            return new EffectiveWorkingSchedule(true, DEFAULT_WORK_START, DEFAULT_WORK_END);
        }
        return new EffectiveWorkingSchedule(
                Boolean.TRUE.equals(schedule.getWorking()),
                schedule.getStartTime() == null ? DEFAULT_WORK_START : schedule.getStartTime(),
                schedule.getEndTime() == null ? DEFAULT_WORK_END : schedule.getEndTime()
        );
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

    private void requireStatus(Appointment appointment, String message, AppointmentStatus... allowedStatuses) {
        if (!List.of(allowedStatuses).contains(appointment.getStatus())) {
            throw new AppException(message, HttpStatus.BAD_REQUEST);
        }
    }

    private void requireArrivalStatus(Appointment appointment,
                                      String message,
                                      AppointmentArrivalStatus... allowedArrivalStatuses) {
        AppointmentArrivalStatus arrivalStatus = Objects.requireNonNullElse(
                appointment.getArrivalStatus(),
                AppointmentArrivalStatus.NOT_ARRIVED
        );
        if (!List.of(allowedArrivalStatuses).contains(arrivalStatus)) {
            throw new AppException(message, HttpStatus.BAD_REQUEST);
        }
    }

    private void requireAppointmentToday(Appointment appointment) {
        if (appointment.getAppointmentDate() == null
                || !appointment.getAppointmentDate().toLocalDate().equals(LocalDate.now())) {
            throw new AppException(
                    "Chỉ được chuyển trạng thái lịch hẹn trong ngày hôm nay. Vui lòng cập nhật ngày giờ lịch hẹn trước.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void requireVisibleToCurrentUser(Appointment appointment, String username, Set<String> roles) {
        if (!isRestrictedDentist(username, roles)) {
            return;
        }
        if (!isAppointmentDentist(appointment, username)) {
            throw new AppException("Bạn chỉ được xem lịch hẹn của chính mình", HttpStatus.FORBIDDEN);
        }
    }

    private void requireOwnAppointmentIfDentist(Appointment appointment, String username) {
        if (!StringUtils.hasText(username) || "system".equals(username)) {
            return;
        }
        Staff staff = staffRepository.findByUsername(username).orElse(null);
        if (staff == null || staff.getStaffType() != StaffType.DENTIST) {
            return;
        }
        if (!isAppointmentDentist(appointment, username)) {
            throw new AppException("Bạn chỉ được xử lý lịch hẹn của chính mình", HttpStatus.FORBIDDEN);
        }
    }

    private boolean isRestrictedDentist(String username, Set<String> roles) {
        return StringUtils.hasText(username)
                && roles != null
                && roles.contains("DENTIST")
                && !roles.contains("ADMIN")
                && !roles.contains("MANAGER")
                && !roles.contains("RECEPTIONIST");
    }

    private boolean isAppointmentDentist(Appointment appointment, String username) {
        return appointment.getDentist() != null
                && appointment.getDentist().getUser() != null
                && username.equals(appointment.getDentist().getUser().getUsername());
    }

    private String preferredPhone(Patient patient) {
        return patient.getPhone() != null ? patient.getPhone() : patient.getGuardianPhone();
    }

    private Sort resolveSort(AppointmentSortOption sortBy) {
        if (sortBy == null) {
            return Sort.by("appointmentDate");
        }
        return switch (sortBy) {
            case APPOINTMENT_DATE -> Sort.by("appointmentDate");
            case APPOINTMENT_DATE_DESC -> Sort.by("appointmentDate").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };
    }

    private record EffectiveWorkingSchedule(boolean working, LocalTime startTime, LocalTime endTime) {
    }

}
