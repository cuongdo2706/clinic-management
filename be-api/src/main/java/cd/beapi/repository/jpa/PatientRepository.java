package cd.beapi.repository.jpa;

import cd.beapi.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCode(String code);

    Optional<Patient> findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("SELECT p FROM Patient p " +
            "WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR p.phone LIKE CONCAT('%', :keyword, '%') " +
            "OR p.code LIKE CONCAT('%', :keyword, '%')")
    List<Patient> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Patient p WHERE p.isWalkIn = true ORDER BY p.createdAt DESC")
    List<Patient> findWalkInPatients();

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isWalkIn = :isWalkIn")
    Long countByWalkIn(@Param("isWalkIn") Boolean isWalkIn);

    // ── Claim Patient: tìm BN walk-in chưa có tài khoản theo SĐT ──
    @Query("""
            SELECT p FROM Patient p
            WHERE p.phone = :phone
            AND p.user IS NULL
            AND p.isWalkIn = TRUE
            """)
    Optional<Patient> findUnclaimedByPhone(@Param("phone") String phone);

    // ── Claim Patient: tìm BN walk-in chưa có tài khoản theo Email ──
    @Query("""
            SELECT p FROM Patient p
            WHERE p.email = :email
            AND p.user IS NULL
            AND p.isWalkIn = TRUE
            """)
    Optional<Patient> findUnclaimedByEmail(@Param("email") String email);

    // ── Claim Patient: tìm BN walk-in chưa có tài khoản theo SĐT hoặc CMND/CCCD ──
    @Query("""
            SELECT p FROM Patient p
            WHERE (p.phone = :phone OR p.identityNumber = :identityNumber)
            AND p.user IS NULL
            AND p.isWalkIn = TRUE
            """)
    Optional<Patient> findUnclaimedByPhoneOrIdentity(@Param("phone") String phone,
                                                      @Param("identityNumber") String identityNumber);

    // ── Kiểm tra SĐT đã được link với tài khoản chưa ──
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END
            FROM Patient p
            WHERE p.phone = :phone
            AND p.user IS NOT NULL
            """)
    boolean isPhoneAlreadyLinked(@Param("phone") String phone);

    // ── Tìm Patient theo User (để BN đăng nhập xem lịch sử) ──
    @Query("SELECT p FROM Patient p WHERE p.user.id = :userId")
    Optional<Patient> findByUserId(@Param("userId") Long userId);

    // ═══════════════════════════════════════════════
    //  GUARDIAN / MINOR QUERIES
    // ═══════════════════════════════════════════════

    // ── Tìm tất cả BN con/người phụ thuộc theo Guardian ID ──
    @Query("SELECT p FROM Patient p WHERE p.guardian.id = :guardianId ORDER BY p.createdAt DESC")
    List<Patient> findDependentsByGuardianId(@Param("guardianId") Long guardianId);

    // ── Tìm tất cả BN nhỏ tuổi theo SĐT người giám hộ (chưa link guardian FK) ──
    @Query("""
            SELECT p FROM Patient p
            WHERE p.isMinor = TRUE
            AND p.guardianPhone = :guardianPhone
            AND p.guardian IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<Patient> findMinorsByGuardianPhone(@Param("guardianPhone") String guardianPhone);

    // ── Tìm tất cả BN nhỏ tuổi chưa link guardian (cho admin gắn thủ công) ──
    @Query("""
            SELECT p FROM Patient p
            WHERE p.isMinor = TRUE
            AND p.guardian IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<Patient> findUnlinkedMinors();
}


