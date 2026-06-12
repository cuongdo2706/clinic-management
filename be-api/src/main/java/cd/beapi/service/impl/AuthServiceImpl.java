package cd.beapi.service.impl;

import cd.beapi.dto.request.ClientRegisterRequest;
import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Role;
import cd.beapi.entity.User;
import cd.beapi.dto.response.TokenResponse;
import cd.beapi.enumerate.AuthPortal;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.PatientMapper;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.repository.jpa.StaffRepository;
import cd.beapi.repository.jpa.UserRepository;
import cd.beapi.security.jwt.JwtTokenProvider;
import cd.beapi.security.service.RefreshTokenService;
import cd.beapi.security.service.TokenBlacklistService;
import cd.beapi.service.AuthService;
import cd.beapi.service.SequenceService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String PORTAL_CLAIM = "portal";
    private static final String PATIENT_ROLE_CODE = "PATIENT";
    private static final int MAX_AGE_UNDER_SUPERVISION = 14;
    private static final Set<String> CLINIC_ROLE_AUTHORITIES = Set.of(
            "ADMIN",
            "MANAGER",
            "DENTIST",
            "RECEPTIONIST"
    );

    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SequenceService sequenceService;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public PatientResponse registerClient(ClientRegisterRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsernameIncludingDeleted(username)) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        validatePatientContact(request);

        Role patientRole = roleRepository.findByCode(PATIENT_ROLE_CODE).orElse(null);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .role(patientRole)
                .build();

        Patient patient = Patient.builder()
                .code(sequenceService.generatePatientCode())
                .fullName(request.getFullName().trim())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null)
                .user(userRepository.save(user))
                .build();

        if (isChild(request.getDob())) {
            patient.setGuardianName(request.getGuardianName().trim());
            patient.setGuardianPhone(request.getGuardianPhone().trim());
        } else {
            patient.setPhone(request.getPhone().trim());
        }

        return patientMapper.toPatientResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public TokenResponse loginClient(LoginRequest loginRequest) {
        return login(loginRequest, AuthPortal.CLIENT);
    }

    @Override
    @Transactional
    public TokenResponse loginClinic(LoginRequest loginRequest) {
        return login(loginRequest, AuthPortal.CLINIC);
    }

    private TokenResponse login(LoginRequest loginRequest, AuthPortal portal) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            validatePortal(authentication, portal);
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> user.setLastLoginAt(java.time.Instant.now()));
            String accessToken = jwtTokenProvider.generateAccessToken(authentication, portal);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication, portal);
            refreshTokenService.saveRefreshToken(authentication.getName(), jwtTokenProvider.getClaims(refreshToken).getId());
            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessExpirationInSeconds(),
                    jwtTokenProvider.getRefreshExpirationInSeconds());
        } catch (BadCredentialsException e) {
            throw new AppException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException e) {
            throw new AppException("This account has been disabled", HttpStatus.UNAUTHORIZED);
        } catch (LockedException e) {
            throw new AppException("This account has been locked", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(String accessToken, String username) {
        Claims claims = jwtTokenProvider.getClaims(accessToken);
        tokenBlacklistService.blacklistToken(claims.getId(), claims.getExpiration());
        refreshTokenService.deleteRefreshToken(username);
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenRequest) {
        return refreshToken(refreshTokenRequest, null);
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenRequest, AuthPortal expectedPortal) {
        if (refreshTokenRequest == null)
            throw new AppException("Refresh token not found", HttpStatus.BAD_REQUEST);
        Claims claims = jwtTokenProvider.getClaims(refreshTokenRequest);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get("type", String.class)))
            throw new AppException("Invalid token type", HttpStatus.UNAUTHORIZED);
        AuthPortal portal = extractPortal(claims);
        if (expectedPortal != null && portal != expectedPortal) {
            throw new AppException("Refresh token is not allowed for this site", HttpStatus.UNAUTHORIZED);
        }
        String username = claims.getSubject();
        String jti = claims.getId();
        if (!refreshTokenService.verifyRefreshToken(username, jti)) {
            throw new AppException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        );
        validatePortal(authentication, portal);

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication, portal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication, portal);

        Claims newRefreshClaims = jwtTokenProvider.getClaims(newRefreshToken);
        refreshTokenService.saveRefreshToken(username, newRefreshClaims.getId());
        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationInSeconds(),
                jwtTokenProvider.getRefreshExpirationInSeconds());

    }

    private void validatePortal(Authentication authentication, AuthPortal portal) {
        String username = authentication.getName();
        boolean hasPatientProfile = patientRepository.findByUsername(username).isPresent();
        boolean hasStaffProfile = staffRepository.findByUsername(username).isPresent();

        if (portal == AuthPortal.CLIENT) {
            if (!hasPatientProfile || hasStaffProfile) {
                throw new AppException("Account is not allowed for client site", HttpStatus.FORBIDDEN);
            }
            return;
        }
        if (portal == AuthPortal.CLINIC) {
            if (hasPatientProfile || !hasClinicProfileOrAuthority(authentication)) {
                throw new AppException("Account is not allowed for clinic site", HttpStatus.FORBIDDEN);
            }
            return;
        }
        throw new AppException("Invalid login portal", HttpStatus.BAD_REQUEST);
    }

    private boolean hasClinicProfileOrAuthority(Authentication authentication) {
        String username = authentication.getName();
        boolean hasStaffProfile = staffRepository.findByUsername(username).isPresent();
        return hasStaffProfile || authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> CLINIC_ROLE_AUTHORITIES.contains(authority) || authority.contains(":"));
    }

    private AuthPortal extractPortal(Claims claims) {
        String portal = claims.get(PORTAL_CLAIM, String.class);
        if (portal == null) {
            throw new AppException("Refresh token portal not found", HttpStatus.UNAUTHORIZED);
        }
        try {
            return AuthPortal.valueOf(portal);
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid refresh token portal", HttpStatus.UNAUTHORIZED);
        }
    }

    private void validatePatientContact(ClientRegisterRequest request) {
        if (isChild(request.getDob())) {
            if (!StringUtils.hasText(request.getGuardianName()) || !StringUtils.hasText(request.getGuardianPhone())) {
                throw new AppException("Guardian information is required for patients under 14", HttpStatus.BAD_REQUEST);
            }
            String guardianPhone = request.getGuardianPhone().trim();
            if (patientRepository.findFirstByPhoneOrGuardianPhone(guardianPhone, guardianPhone).isPresent()) {
                throw new AppException("Phone number already exists", HttpStatus.BAD_REQUEST);
            }
            return;
        }

        if (!StringUtils.hasText(request.getPhone())) {
            throw new AppException("Phone is required", HttpStatus.BAD_REQUEST);
        }
        String phone = request.getPhone().trim();
        if (patientRepository.findFirstByPhoneOrGuardianPhone(phone, phone).isPresent()) {
            throw new AppException("Phone number already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isChild(LocalDate dob) {
        return ChronoUnit.YEARS.between(dob, LocalDate.now()) < MAX_AGE_UNDER_SUPERVISION;
    }
}
