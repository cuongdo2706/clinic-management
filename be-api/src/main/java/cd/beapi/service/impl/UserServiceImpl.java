package cd.beapi.service.impl;

import cd.beapi.entity.Role;
import cd.beapi.entity.User;
import cd.beapi.exception.AppException;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.repository.jpa.UserRepository;
import cd.beapi.service.UserService;
import cd.beapi.utility.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final int TEMPORARY_PASSWORD_LENGTH = 8;
    private static final String UPPERCASE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "23456789";
    private static final String SPECIAL_CHARS = "_@#$%";
    private static final String TEMPORARY_PASSWORD_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SPECIAL_CHARS;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CreatedStaffUser createStaffUser(String fullName, String staffCode, String roleCode) {
        if (!StringUtils.hasText(staffCode)) {
            throw new AppException("Staff code is required when creating staff account", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(roleCode)) {
            throw new AppException("Role is required when creating staff account", HttpStatus.BAD_REQUEST);
        }

        String username = generateUsername(fullName, staffCode);
        String temporaryPassword = generateTemporaryPassword();
        String normalizedRoleCode = roleCode.trim();
        if (ADMIN_ROLE_CODE.equals(normalizedRoleCode)) {
            throw new AppException("Admin role cannot be assigned when creating staff account", HttpStatus.BAD_REQUEST);
        }
        Role role = roleRepository.findByCode(normalizedRoleCode)
                .orElseThrow(() -> new AppException("Cannot find role with code: " + normalizedRoleCode, HttpStatus.BAD_REQUEST));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(temporaryPassword))
                .role(role)
                .isActive(true)
                .mustChangePassword(true)
                .build();

        return new CreatedStaffUser(userRepository.save(user), temporaryPassword);
    }

    private String generateUsername(String fullName, String staffCode) {
        String username = generateUsernameBase(fullName) + normalizeStaffCodeForUsername(staffCode);
        if (userRepository.existsByUsernameIncludingDeleted(username)) {
            throw new AppException("Username has already existed: " + username, HttpStatus.BAD_REQUEST);
        }

        return username;
    }

    private String generateUsernameBase(String fullName) {
        String normalizedName = StringUtil.normalizeKeyword(fullName)
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (!StringUtils.hasText(normalizedName)) {
            throw new AppException("Staff name must contain at least one letter", HttpStatus.BAD_REQUEST);
        }

        String[] nameParts = normalizedName.split(" ");
        return nameParts[nameParts.length - 1];
    }

    private String normalizeStaffCodeForUsername(String staffCode) {
        String normalizedCode = staffCode.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String prefix = normalizedCode.replaceAll("\\d+$", "");
        String number = normalizedCode.substring(prefix.length());
        if (number.isEmpty() || number.length() >= 6) {
            return normalizedCode;
        }
        return prefix + String.format("%06d", Long.parseLong(number));
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
