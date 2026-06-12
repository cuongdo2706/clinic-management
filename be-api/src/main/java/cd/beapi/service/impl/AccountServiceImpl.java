package cd.beapi.service.impl;

import cd.beapi.dto.request.SearchAccountRequest;
import cd.beapi.dto.request.UpdateAccountRoleRequest;
import cd.beapi.dto.request.UpdateAccountStatusRequest;
import cd.beapi.dto.response.AccountResponse;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.ResetPasswordResponse;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Role;
import cd.beapi.entity.Staff;
import cd.beapi.entity.User;
import cd.beapi.enumerate.AccountStatus;
import cd.beapi.enumerate.AccountType;
import cd.beapi.exception.AppException;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.repository.jpa.UserRepository;
import cd.beapi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final String PATIENT_ROLE_CODE = "PATIENT";
    private static final int TEMPORARY_PASSWORD_LENGTH = 8;
    private static final String UPPERCASE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "23456789";
    private static final String SPECIAL_CHARS = "_@#$%";
    private static final String TEMPORARY_PASSWORD_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SPECIAL_CHARS;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public PageData<AccountResponse> search(SearchAccountRequest request) {
        AccountType type = request.getType() == null ? AccountType.STAFF : request.getType();
        List<AccountResponse> accounts = type == AccountType.STAFF
                ? staffRepository.findStaffAccounts().stream().map(this::toStaffAccount).toList()
                : patientRepository.findPatientAccounts().stream().map(this::toPatientAccount).toList();

        String keyword = normalizeKeyword(request.getKeyword());
        List<AccountResponse> filteredAccounts = accounts.stream()
                .filter(account -> matchesKeyword(account, keyword))
                .filter(account -> request.getStatus() == null || account.status() == request.getStatus())
                .filter(account -> type != AccountType.STAFF
                        || !StringUtils.hasText(request.getRoleCode())
                        || Objects.equals(account.roleCode(), request.getRoleCode().trim()))
                .sorted(Comparator.comparing(AccountResponse::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 10 : request.getSize();
        int fromIndex = Math.min(page * size, filteredAccounts.size());
        int toIndex = Math.min(fromIndex + size, filteredAccounts.size());
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) filteredAccounts.size() / size);

        return new PageData<>(
                filteredAccounts.subList(fromIndex, toIndex),
                page,
                size,
                (long) filteredAccounts.size(),
                totalPages
        );
    }

    @Transactional
    @Override
    public AccountResponse updateStatus(Long id, UpdateAccountStatusRequest request) {
        User user = findUser(id);
        switch (request.getStatus()) {
            case ACTIVE -> {
                user.setIsActive(true);
                user.setLocked(false);
            }
            case LOCKED -> {
                user.setIsActive(true);
                user.setLocked(true);
            }
            case DISABLED -> {
                user.setIsActive(false);
                user.setLocked(false);
            }
        }
        return toAccountResponse(user);
    }

    @Transactional
    @Override
    public AccountResponse updateRole(Long id, UpdateAccountRoleRequest request) {
        User user = findUser(id);
        if (findPatientProfile(user).isPresent()) {
            throw new AppException("Không thể đổi vai trò tài khoản khách hàng", HttpStatus.BAD_REQUEST);
        }
        String roleCode = request.getRoleCode().trim();
        if (ADMIN_ROLE_CODE.equals(roleCode) || PATIENT_ROLE_CODE.equals(roleCode)) {
            throw new AppException("Vai trò này không được gán cho tài khoản nhân viên", HttpStatus.BAD_REQUEST);
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new AppException("Cannot find role with code: " + roleCode, HttpStatus.BAD_REQUEST));
        user.setRole(role);
        return toAccountResponse(user);
    }

    @Transactional
    @Override
    public ResetPasswordResponse resetPassword(Long id) {
        User user = findUser(id);
        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setPasswordChangedAt(Instant.now());
        return new ResetPasswordResponse(temporaryPassword);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User user = findUser(id);
        findStaffProfile(user).ifPresent(staff -> staff.setUser(null));
        findPatientProfile(user).ifPresent(patient -> patient.setUser(null));
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new AppException("Cannot find account with id: " + id, HttpStatus.BAD_REQUEST));
    }

    private AccountResponse toAccountResponse(User user) {
        return findStaffProfile(user)
                .map(this::toStaffAccount)
                .or(() -> findPatientProfile(user).map(this::toPatientAccount))
                .orElseThrow(() -> new AppException("Tài khoản chưa liên kết nhân viên hoặc khách hàng", HttpStatus.BAD_REQUEST));
    }

    private Optional<Staff> findStaffProfile(User user) {
        return staffRepository.findByUserIdWithAccount(user.getId());
    }

    private Optional<Patient> findPatientProfile(User user) {
        return patientRepository.findByUserIdWithAccount(user.getId());
    }

    private AccountResponse toStaffAccount(Staff staff) {
        User user = staff.getUser();
        return new AccountResponse(
                user.getId(),
                AccountType.STAFF,
                user.getUsername(),
                staff.getId(),
                staff.getFullName(),
                staff.getCode(),
                staff.getPhone(),
                staff.getEmail(),
                user.getRole() == null ? null : user.getRole().getCode(),
                user.getRole() == null ? null : user.getRole().getName(),
                toStatus(user),
                Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    private AccountResponse toPatientAccount(Patient patient) {
        User user = patient.getUser();
        String phone = StringUtils.hasText(patient.getPhone()) ? patient.getPhone() : patient.getGuardianPhone();
        return new AccountResponse(
                user.getId(),
                AccountType.CUSTOMER,
                user.getUsername(),
                patient.getId(),
                patient.getFullName(),
                patient.getCode(),
                phone,
                null,
                user.getRole() == null ? null : user.getRole().getCode(),
                user.getRole() == null ? null : user.getRole().getName(),
                toStatus(user),
                Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    private AccountStatus toStatus(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return AccountStatus.DISABLED;
        }
        return Boolean.TRUE.equals(user.getLocked()) ? AccountStatus.LOCKED : AccountStatus.ACTIVE;
    }

    private boolean matchesKeyword(AccountResponse account, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return contains(account.username(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String generateTemporaryPassword() {
        List<Character> passwordChars = new ArrayList<>(TEMPORARY_PASSWORD_LENGTH);
        passwordChars.add(randomChar(UPPERCASE_CHARS));
        passwordChars.add(randomChar(LOWERCASE_CHARS));
        passwordChars.add(randomChar(DIGIT_CHARS));
        passwordChars.add(randomChar(SPECIAL_CHARS));

        for (int i = passwordChars.size(); i < TEMPORARY_PASSWORD_LENGTH; i++) {
            passwordChars.add(randomChar(TEMPORARY_PASSWORD_CHARS));
        }
        Collections.shuffle(passwordChars, SECURE_RANDOM);

        StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (Character passwordChar : passwordChars) {
            password.append(passwordChar);
        }
        return password.toString();
    }

    private char randomChar(String chars) {
        int index = SECURE_RANDOM.nextInt(chars.length());
        return chars.charAt(index);
    }
}
