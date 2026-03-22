package cd.beapi.service.impl;

import cd.beapi.dto.request.ClaimPatientRequest;
import cd.beapi.dto.request.LinkPatientToUserRequest;
import cd.beapi.dto.response.ClaimPatientResponse;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.entity.Patient;
import cd.beapi.entity.Role;
import cd.beapi.entity.User;
import cd.beapi.exception.AppException;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.repository.jpa.UserRepository;
import cd.beapi.service.PatientClaimService;
import cd.beapi.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientClaimServiceImpl implements PatientClaimService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequenceService sequenceService;

    private static final String ROLE_PATIENT = "PATIENT";

    // ──────────────────────────────────────────────
    // ① ĐĂNG KÝ + TỰ ĐỘNG CLAIM (BN tự đăng ký online)
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public ClaimPatientResponse registerAndClaimPatient(ClaimPatientRequest request) {
        String phone = request.phone().trim();

        // ── Kiểm tra SĐT đã được link với tài khoản chưa ──
        if (patientRepository.isPhoneAlreadyLinked(phone)) {
            throw new AppException(
                    "Số điện thoại này đã được liên kết với tài khoản khác",
                    HttpStatus.CONFLICT);
        }

        // ── Kiểm tra username (phone) đã tồn tại trong User chưa ──
        if (userRepository.findByUsername(phone).isPresent()) {
            throw new AppException(
                    "Tài khoản với số điện thoại này đã tồn tại",
                    HttpStatus.CONFLICT);
        }

        // ── Tạo User mới ──
        Role patientRole = roleRepository.findByCode(ROLE_PATIENT)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy role PATIENT trong hệ thống",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        User newUser = new User();
        newUser.setUsername(phone); // SĐT làm username
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setIsActive(true);
        newUser.setRole(patientRole);
        newUser = userRepository.save(newUser);

        // ── Tìm Patient walk-in chưa có tài khoản (theo SĐT) ──
        var existingPatient = patientRepository.findUnclaimedByPhone(phone);

        Patient claimedPatient;
        boolean claimed;

        if (existingPatient.isPresent()) {
            // ══ CASE A: CLAIM — Link User vào Patient cũ ══
            claimedPatient = existingPatient.get();
            claimedPatient.setUser(newUser);
            claimedPatient.setIsWalkIn(false);

            // Cập nhật email nếu Patient cũ chưa có
            if (claimedPatient.getEmail() == null && request.email() != null) {
                claimedPatient.setEmail(request.email());
            }

            claimedPatient = patientRepository.save(claimedPatient);
            claimed = true;
        } else {
            // ══ CASE B: TẠO MỚI — Chưa từng khám ══
            claimedPatient = Patient.builder()
                    .code(sequenceService.generatePatientCode())
                    .fullName(request.fullName())
                    .phone(phone)
                    .email(request.email())
                    .isWalkIn(false)
                    .user(newUser)
                    .build();

            claimedPatient = patientRepository.save(claimedPatient);
            claimed = false;
        }

        // ══ AUTO-LINK BN CON/MINOR ══
        // Tìm các BN nhỏ tuổi walk-in có guardianPhone = SĐT phụ huynh vừa đăng ký
        // → tự động gắn guardian FK = patient (phụ huynh)
        List<Patient> unlinkedMinors = patientRepository.findMinorsByGuardianPhone(phone);
        for (Patient minor : unlinkedMinors) {
            minor.setGuardian(claimedPatient);
            patientRepository.save(minor);
        }

        return new ClaimPatientResponse(
                newUser.getId(),
                claimedPatient.getId(),
                claimedPatient.getCode(),
                claimedPatient.getFullName(),
                claimedPatient.getPhone(),
                claimed
        );
    }

    // ──────────────────────────────────────────────
    // ② ADMIN LINK THỦ CÔNG
    // ──────────────────────────────────────────────
    @Override
    @Transactional
    public PatientResponse linkPatientToUser(LinkPatientToUserRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy bệnh nhân với ID: " + request.patientId(),
                        HttpStatus.NOT_FOUND));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy user với ID: " + request.userId(),
                        HttpStatus.NOT_FOUND));

        // Kiểm tra Patient đã có tài khoản chưa
        if (patient.getUser() != null) {
            throw new AppException(
                    "Bệnh nhân " + patient.getCode() + " đã được liên kết với tài khoản khác",
                    HttpStatus.CONFLICT);
        }

        // Kiểm tra User đã được link với Patient nào chưa
        patientRepository.findByUserId(user.getId()).ifPresent(p -> {
            throw new AppException(
                    "User này đã được liên kết với bệnh nhân: " + p.getCode(),
                    HttpStatus.CONFLICT);
        });

        // Link
        patient.setUser(user);
        patient.setIsWalkIn(false);
        patient = patientRepository.save(patient);

        return toPatientResponse(patient);
    }

    // ──────────────────────────────────────────────
    // ③ BN XEM HỒ SƠ CỦA MÌNH (by userId)
    // ──────────────────────────────────────────────
    @Override
    public PatientResponse getMyPatientProfile(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy hồ sơ bệnh nhân cho tài khoản này",
                        HttpStatus.NOT_FOUND));
        return toPatientResponse(patient);
    }

    // ──────────────────────────────────────────────
    // ④ BN XEM HỒ SƠ CỦA MÌNH (by username / JWT subject)
    // ──────────────────────────────────────────────
    @Override
    public PatientResponse getMyPatientProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy tài khoản: " + username,
                        HttpStatus.NOT_FOUND));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy hồ sơ bệnh nhân cho tài khoản này",
                        HttpStatus.NOT_FOUND));
        return toPatientResponse(patient);
    }

    // ──────────────────────────────────────────────
    // ⑤ PHỤ HUYNH XEM DANH SÁCH BN CON
    // ──────────────────────────────────────────────
    @Override
    public List<PatientResponse> getMyDependents(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy tài khoản: " + username,
                        HttpStatus.NOT_FOUND));

        Patient guardian = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy hồ sơ bệnh nhân cho tài khoản này",
                        HttpStatus.NOT_FOUND));

        // Tìm tất cả BN con đã link guardian FK
        List<Patient> dependents = patientRepository.findDependentsByGuardianId(guardian.getId());

        return dependents.stream().map(this::toPatientResponse).toList();
    }

    // ──────────────────────────────────────────────
    // MAPPER
    // ──────────────────────────────────────────────
    private PatientResponse toPatientResponse(Patient p) {
        Patient g = p.getGuardian();
        return new PatientResponse(
                p.getId(), p.getCode(), p.getFullName(), p.getPhone(),
                p.getDob() != null ? p.getDob().toString() : null,
                p.getGender(), p.getEmail(), p.getAddress(),
                p.getIsWalkIn(), p.getIsMinor(),
                // Guardian info
                g != null ? g.getId() : null,
                g != null ? g.getCode() : null,
                g != null ? g.getFullName() : p.getGuardianName(),
                g != null ? g.getPhone() : p.getGuardianPhone(),
                p.getGuardianRelationship() != null ? p.getGuardianRelationship().name() : null,
                p.getGuardianRelationship() != null ? p.getGuardianRelationship().getLabel() : null,
                p.getCreatedAt());
    }
}


