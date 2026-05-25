package cd.beapi.service.impl;

import cd.beapi.dto.request.CreateStaffRequest;
import cd.beapi.dto.request.SearchStaffRequest;
import cd.beapi.dto.request.UpdateStaffRequest;
import cd.beapi.dto.request.UpdateStaffStatusRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.StaffResponse;
import cd.beapi.entity.QStaff;
import cd.beapi.entity.Staff;
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

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private static final String STAFF_FOLDER_NAME = "staff";

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
}
