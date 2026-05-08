package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateStaffRequest;
import cd.beapi.dto.request.SearchStaffRequest;
import cd.beapi.dto.request.UpdateStaffRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.StaffResponse;
import cd.beapi.entity.QStaff;
import cd.beapi.entity.Staff;
import cd.beapi.entity.WorkingSchedule;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.StaffMapper;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.repository.jpa.WorkingScheduleRepository;
import cd.beapi.service.ImageService;
import cd.beapi.service.StaffService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private static final String STAFF_FOLDER_NAME = "staff";

    private final StaffRepository staffRepository;
    private final WorkingScheduleRepository workingScheduleRepository;
    private final StaffMapper staffMapper;
    private final SequenceService sequenceService;
    private final ImageService imageService;

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
        if (StringUtils.hasText(searchStaffRequest.getEmailKeyword())) {
            whereClause.and(s.email.likeIgnoreCase("%" + searchStaffRequest.getEmailKeyword() + "%"));
        }
        if (searchStaffRequest.getStaffType() != null) {
            whereClause.and(s.staffType.eq(searchStaffRequest.getStaffType()));
        }

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
    public StaffResponse findById(Long id) {
        return staffMapper.toStaffResponse(
                staffRepository.findByIdWithWorkingSchedules(id).orElseThrow(
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
                .build();

        if (StringUtils.hasText(createStaffRequest.getCode())) {
            if (staffRepository.existsByCode(createStaffRequest.getCode())) {
                throw new AppException("This code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            newStaff.setCode(createStaffRequest.getCode());
        } else {
            newStaff.setCode(sequenceService.generateDentistCode());
        }
        if (file != null && !file.isEmpty()) {
            newStaff.setAvatarUrl(imageService.upload(file, STAFF_FOLDER_NAME));
        }
        Staff savedStaff = staffRepository.save(newStaff);
        savedStaff.setWorkingSchedules(saveCreateWorkingSchedules(savedStaff, createStaffRequest.getWorkingSchedules()));
        return staffMapper.toStaffResponse(savedStaff);
    }

    @Transactional
    @Override
    public StaffResponse update(Long id, UpdateStaffRequest updateStaffRequest, MultipartFile file) {
        Staff existedStaff = staffRepository.findByIdWithWorkingSchedules(id).orElseThrow(
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
        if (file != null && !file.isEmpty()) {
            existedStaff.setAvatarUrl(imageService.update(file, oldAvatarUrl, STAFF_FOLDER_NAME));
        }
        Staff savedStaff = staffRepository.save(existedStaff);
        if (updateStaffRequest.getWorkingSchedules() != null) {
            workingScheduleRepository.deleteByStaffId(savedStaff.getId());
            workingScheduleRepository.flush();
            savedStaff.setWorkingSchedules(saveUpdateWorkingSchedules(savedStaff, updateStaffRequest.getWorkingSchedules()));
        }

        return staffMapper.toStaffResponse(savedStaff);
    }

    @Override
    public void delete(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST);
        }
        Staff staff = staffRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find staff with id: " + id, HttpStatus.BAD_REQUEST));
        imageService.delete(staff.getAvatarUrl());
        staffRepository.deleteById(id);
    }

    private List<WorkingSchedule> saveCreateWorkingSchedules(Staff staff, List<CreateStaffRequest.WorkingScheduleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        validateWorkingSchedules(requests);
        List<WorkingSchedule> schedules = requests.stream()
                .map(request -> WorkingSchedule.builder()
                        .staff(staff)
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .build())
                .toList();
        return workingScheduleRepository.saveAll(schedules);
    }

    private List<WorkingSchedule> saveUpdateWorkingSchedules(Staff staff, List<UpdateStaffRequest.WorkingScheduleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        validateUpdateWorkingSchedules(requests);
        List<WorkingSchedule> schedules = requests.stream()
                .map(request -> WorkingSchedule.builder()
                        .staff(staff)
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .build())
                .toList();
        return workingScheduleRepository.saveAll(schedules);
    }

    private void validateWorkingSchedules(List<CreateStaffRequest.WorkingScheduleRequest> requests) {
        Set<DayOfWeek> days = new HashSet<>();
        for (CreateStaffRequest.WorkingScheduleRequest request : requests) {
            validateScheduleFields(request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), days);
        }
    }

    private void validateUpdateWorkingSchedules(List<UpdateStaffRequest.WorkingScheduleRequest> requests) {
        Set<DayOfWeek> days = new HashSet<>();
        for (UpdateStaffRequest.WorkingScheduleRequest request : requests) {
            validateScheduleFields(request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), days);
        }
    }

    private void validateScheduleFields(java.time.DayOfWeek dayOfWeek,
                                        java.time.LocalTime startTime,
                                        java.time.LocalTime endTime,
                                        Set<DayOfWeek> days) {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            throw new AppException("Working schedule day, start time and end time are required", HttpStatus.BAD_REQUEST);
        }
        if (!startTime.isBefore(endTime)) {
            throw new AppException("Working schedule start time must be before end time", HttpStatus.BAD_REQUEST);
        }
        if (!days.add(dayOfWeek)) {
            throw new AppException("Working schedule day must be unique for each staff", HttpStatus.BAD_REQUEST);
        }
    }
}
