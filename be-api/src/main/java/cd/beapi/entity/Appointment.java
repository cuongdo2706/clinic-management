package cd.beapi.entity;

import cd.beapi.enumerate.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE appointments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Appointment extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    LocalDateTime appointmentDate;

    @Column(columnDefinition = "TEXT")
    String symptom;

    @Column(columnDefinition = "TEXT")
    String note;

    @Enumerated(EnumType.STRING)
    AppointmentStatus status;

    // Bác sĩ được chọn khi đặt lịch (nullable — walk-in có thể không chọn trước)
    @ManyToOne
    @JoinColumn(name = "dentist_id")
    Staff dentist;

    // Lễ tân tiếp nhận (set khi check-in)
    @ManyToOne
    @JoinColumn(name = "receptionist_id")
    Staff receptionist;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    // === CHECK-IN INFO (set khi BN có mặt tại phòng khám) ===
    Integer queueNumber;

    // Snapshot tên & SĐT tại thời điểm tiếp nhận — giữ nguyên dù BN sau này đổi thông tin
    String snapshotPatientName;
    String snapshotPatientPhone;


    // === META ===
    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    Instant deletedAt;

    @Version
    @ColumnDefault("0")
    Long version;
}
