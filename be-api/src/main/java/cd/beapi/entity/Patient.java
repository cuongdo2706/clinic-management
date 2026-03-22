package cd.beapi.entity;

import cd.beapi.enumerate.GuardianRelationship;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE patients SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Patient extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String identityNumber;

    String email;

    String province;

    String commune;

    String address;

    /**
     * true  = Bệnh nhân walk-in (lễ tân tạo nhanh tại quầy, chưa có tài khoản)
     * false = Bệnh nhân đã đăng ký tài khoản (đặt lịch online)
     */
    @Builder.Default
    Boolean isWalkIn = false;

    // ═══════════════════════════════════════════════
    //  THÔNG TIN NGƯỜI GIÁM HỘ (cho BN chưa đủ tuổi)
    // ═══════════════════════════════════════════════

    /**
     * true  = Bệnh nhân là trẻ em / người chưa đủ tuổi (cần có người giám hộ)
     * false = Bệnh nhân tự đi khám (default)
     */
    @Builder.Default
    Boolean isMinor = false;

    /**
     * Self-referencing FK: khi người giám hộ CŨNG LÀ bệnh nhân trong hệ thống.
     * VD: Mẹ (BN-000010) đưa con (BN-000042) đi khám
     * → BN-000042.guardian = BN-000010
     * <p>
     * Có thể NULL nếu người giám hộ không phải bệnh nhân → dùng guardianName/guardianPhone
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    Patient guardian;

    /**
     * Họ tên người giám hộ (dùng khi guardian KHÔNG phải bệnh nhân trong hệ thống).
     * VD: Lễ tân ghi nhanh "Nguyễn Văn A" khi tiếp nhận walk-in.
     */
    String guardianName;

    /**
     * SĐT liên hệ người giám hộ.
     * Đây là SĐT chính để liên lạc về BN nhỏ tuổi.
     */
    String guardianPhone;

    /**
     * Quan hệ với BN: CHA, MẸ, ÔNG, BÀ, ANH_CHỊ...
     */
    @Enumerated(EnumType.STRING)
    GuardianRelationship guardianRelationship;

    // ═══════════════════════════════════════════════

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    User user;
}
