package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateStaffRequest;
import cd.beapi.dto.request.SearchStaffRequest;
import cd.beapi.dto.request.StaffWorkingScheduleRequest;
import cd.beapi.dto.request.UpdateStaffRequest;
import cd.beapi.dto.request.UpdateStaffStatusRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PublicDentistResponse;
import cd.beapi.dto.response.StaffResponse;
import cd.beapi.entity.QStaff;
import cd.beapi.entity.Staff;
import cd.beapi.entity.StaffWorkingSchedule;
import cd.beapi.enumerate.StaffType;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.StaffMapper;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.service.ImageService;
import cd.beapi.service.StaffService;
import cd.beapi.service.SequenceService;
import cd.beapi.service.UserService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private static final String STAFF_FOLDER_NAME = "staff";
    private static final int SLOT_MINUTES = 30;
    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(8, 30);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(20, 0);

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final SequenceService sequenceService;
    private final ImageService imageService;
    private final UserService userService;

    @Transactional(readOnly = true)
    @Override
    public PageData<StaffResponse> search(SearchStaffRequest searchStaffRequest) {
        QStaff s = QStaff.staff;
        BooleanBuilder whereClause = new BooleanBuilder();

        if (StringUtils.hasText(searchStaffRequest.getCodeKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", s.code)
                            .like("%" + StringUtil.normalizeKeyword(searchStaffRequest.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchStaffRequest.getNameKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", s.fullName)
                            .like("%" + StringUtil.normalizeKeyword(searchStaffRequest.getNameKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchStaffRequest.getPhoneKeyword())) {
            whereClause.and(s.phone.like("%" + searchStaffRequest.getPhoneKeyword() + "%"));
        }
        if (searchStaffRequest.getStaffType() != null) {
            whereClause.and(s.staffType.eq(searchStaffRequest.getStaffType()));
        }
        whereClause.and(s.isActive.eq(searchStaffRequest.getIsActive() == null || searchStaffRequest.getIsActive()));

        Sort sort = switch (searchStaffRequest.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("fullName");
            case NAME_DESC -> Sort.by("fullName").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(searchStaffRequest.getPage(), searchStaffRequest.getSize(), sort);
        Page<Staff> pages = staffRepository.findAll(whereClause, pageable);

        return new PageData<>(
                staffMapper.toStaffResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicDentistResponse> findDentistOptions() {
        return staffRepository.findActiveByStaffType(StaffType.DENTIST).stream()
                .map(staff -> new PublicDentistResponse(
                        staff.getId(),
                        staff.getCode(),
                        staff.getFullName(),
                        staff.getAvatarUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public StaffResponse findCurrentDentist(String username) {
        Staff staff = staffRepository.findByUsername(username).orElseThrow(
                () -> new AppException("Không tìm thấy nha sĩ ứng với tài khoản hiện tại", HttpStatus.BAD_REQUEST)
        );
        if (staff.getStaffType() != StaffType.DENTIST || !Boolean.TRUE.equals(staff.getIsActive())) {
            throw new AppException("Tài khoản hiện tại không phải nha sĩ đang hoạt động", HttpStatus.BAD_REQUEST);
        }
        return staffMapper.toStaffResponse(staff);
    }

    @Transactional(readOnly = true)
    @Override
    public StaffResponse findById(Long id) {
        return staffMapper.toStaffResponse(
                staffRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Transactional
    @Override
    public StaffResponse save(CreateStaffRequest createStaffRequest, MultipartFile file) {
        Staff newStaff = Staff.builder()
                .fullName(createStaffRequest.getFullName())
                .dob(createStaffRequest.getDob())
                .gender(createStaffRequest.getGender())
                .phone(createStaffRequest.getPhone())
                .email(createStaffRequest.getEmail())
                .address(createStaffRequest.getAddress())
                .staffType(createStaffRequest.getStaffType())
                .isActive(true)
                .build();

        if (StringUtils.hasText(createStaffRequest.getCode())) {
            if (staffRepository.existsByCode(createStaffRequest.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            newStaff.setCode(createStaffRequest.getCode());
        } else {
            newStaff.setCode(sequenceService.generateStaffCode());
        }
        if (file != null && !file.isEmpty()) {
            newStaff.setAvatarUrl(imageService.upload(file, STAFF_FOLDER_NAME));
        }
        replaceWorkingSchedules(newStaff, createStaffRequest.getWorkingSchedules());
        UserService.CreatedStaffUser createdUser = null;
        if (StringUtils.hasText(createStaffRequest.getRoleCode())) {
            createdUser = userService.createStaffUser(
                    createStaffRequest.getFullName(),
                    newStaff.getCode(),
                    createStaffRequest.getRoleCode()
            );
            newStaff.setUser(createdUser.user());
        }
        Staff savedStaff = staffRepository.save(newStaff);
        StaffResponse response = staffMapper.toStaffResponse(savedStaff);
        return createdUser == null ? response : withTemporaryPassword(response, createdUser.temporaryPassword());
    }

    private StaffResponse withTemporaryPassword(StaffResponse response, String temporaryPassword) {
        return new StaffResponse(
                response.id(),
                response.code(),
                response.fullName(),
                response.phone(),
                response.email(),
                response.dob(),
                response.gender(),
                response.address(),
                response.avatarUrl(),
                response.staffType(),
                response.isActive(),
                response.version(),
                response.createdAt(),
                response.modifiedAt(),
                response.userId(),
                response.username(),
                response.workingSchedules(),
                temporaryPassword
        );
    }

    @Transactional
    @Override
    public StaffResponse update(Long id, UpdateStaffRequest updateStaffRequest, MultipartFile file) {
        Staff existedStaff = staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST));
        String oldAvatarUrl = existedStaff.getAvatarUrl();

        if (StringUtils.hasText(updateStaffRequest.getCode()) && !updateStaffRequest.getCode().equals(existedStaff.getCode())) {
            if (staffRepository.existsByCode(updateStaffRequest.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existedStaff.setCode(updateStaffRequest.getCode());
        }

        existedStaff.setFullName(updateStaffRequest.getFullName());
        existedStaff.setDob(updateStaffRequest.getDob());
        existedStaff.setGender(updateStaffRequest.getGender());
        existedStaff.setPhone(updateStaffRequest.getPhone());
        existedStaff.setEmail(updateStaffRequest.getEmail());
        existedStaff.setAddress(updateStaffRequest.getAddress());
        existedStaff.setStaffType(updateStaffRequest.getStaffType());
        existedStaff.setVersion(updateStaffRequest.getVersion());
        replaceWorkingSchedules(existedStaff, updateStaffRequest.getWorkingSchedules());
        if (file != null && !file.isEmpty()) {
            existedStaff.setAvatarUrl(imageService.update(file, oldAvatarUrl, STAFF_FOLDER_NAME));
        }
        Staff savedStaff = staffRepository.save(existedStaff);
        return staffMapper.toStaffResponse(savedStaff);
    }

    @Transactional
    @Override
    public StaffResponse updateStatus(Long id, UpdateStaffStatusRequest updateStaffStatusRequest) {
        Staff existedStaff = staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST));

        existedStaff.setIsActive(updateStaffStatusRequest.getIsActive());
        if (updateStaffStatusRequest.getVersion() != null) {
            existedStaff.setVersion(updateStaffStatusRequest.getVersion());
        }

        return staffMapper.toStaffResponse(staffRepository.save(existedStaff));
    }

    private void replaceWorkingSchedules(Staff staff, List<StaffWorkingScheduleRequest> requests) {
        if (staff.getWorkingSchedules() == null) {
            staff.setWorkingSchedules(new java.util.ArrayList<>());
        }

        Map<DayOfWeek, StaffWorkingSchedule> existingScheduleByDay = staff.getWorkingSchedules().stream()
                .filter(schedule -> schedule.getDayOfWeek() != null)
                .collect(Collectors.toMap(StaffWorkingSchedule::getDayOfWeek, Function.identity(), (first, second) -> first));
        Map<DayOfWeek, StaffWorkingSchedule> normalizedScheduleByDay = normalizeWorkingSchedules(requests).stream()
                .collect(Collectors.toMap(StaffWorkingSchedule::getDayOfWeek, Function.identity()));

        staff.getWorkingSchedules().removeIf(schedule -> !normalizedScheduleByDay.containsKey(schedule.getDayOfWeek()));
        normalizedScheduleByDay.forEach((dayOfWeek, normalizedSchedule) -> {
            StaffWorkingSchedule existingSchedule = existingScheduleByDay.get(dayOfWeek);
            if (existingSchedule == null) {
                normalizedSchedule.setStaff(staff);
                staff.getWorkingSchedules().add(normalizedSchedule);
                return;
            }
            existingSchedule.setWorking(normalizedSchedule.getWorking());
            existingSchedule.setStartTime(normalizedSchedule.getStartTime());
            existingSchedule.setEndTime(normalizedSchedule.getEndTime());
        });
    }

    private List<StaffWorkingSchedule> normalizeWorkingSchedules(List<StaffWorkingScheduleRequest> requests) {
        Map<DayOfWeek, StaffWorkingScheduleRequest> requestByDay = requests == null
                ? Map.of()
                : requests.stream()
                .filter(request -> request.getDayOfWeek() != null)
                .collect(Collectors.toMap(StaffWorkingScheduleRequest::getDayOfWeek, Function.identity(), (first, second) -> second));

        return List.of(DayOfWeek.values()).stream()
                .map(dayOfWeek -> toWorkingSchedule(dayOfWeek, requestByDay.get(dayOfWeek)))
                .toList();
    }

    private StaffWorkingSchedule toWorkingSchedule(DayOfWeek dayOfWeek, StaffWorkingScheduleRequest request) {
        boolean working = request == null || request.getWorking() == null || request.getWorking();
        LocalTime startTime = request == null || request.getStartTime() == null ? DEFAULT_WORK_START : request.getStartTime();
        LocalTime endTime = request == null || request.getEndTime() == null ? DEFAULT_WORK_END : request.getEndTime();
        validateWorkingSchedule(dayOfWeek, working, startTime, endTime);

        return StaffWorkingSchedule.builder()
                .dayOfWeek(dayOfWeek)
                .working(working)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    private void validateWorkingSchedule(DayOfWeek dayOfWeek, boolean working, LocalTime startTime, LocalTime endTime) {
        if (!working) {
            return;
        }
        if (!startTime.isBefore(endTime)) {
            throw new AppException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc trong lịch làm việc " + dayOfWeek, HttpStatus.BAD_REQUEST);
        }
        if (startTime.isBefore(DEFAULT_WORK_START) || endTime.isAfter(DEFAULT_WORK_END)) {
            throw new AppException("Giờ làm việc phải trong khoảng 08:30 đến 20:00", HttpStatus.BAD_REQUEST);
        }
        if (!isSlotAligned(startTime) || !isSlotAligned(endTime)) {
            throw new AppException("Giờ làm việc phải theo mốc 30 phút", HttpStatus.BAD_REQUEST);
        }
        if (startTime.plusMinutes(SLOT_MINUTES).isAfter(endTime)) {
            throw new AppException("Giờ bắt đầu và giờ kết thúc phải cách nhau ít nhất 30 phút", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isSlotAligned(LocalTime time) {
        return time.getMinute() % SLOT_MINUTES == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }
}
