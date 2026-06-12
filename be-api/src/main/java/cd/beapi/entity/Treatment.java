package cd.beapi.entity;

import cd.beapi.enumerate.TreatmentStatus;
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
@Table(name = "treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE treatments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
public class Treatment extends BaseEntity {
    @Column(columnDefinition = "TEXT")
    String diagnosis;

    @Column(columnDefinition = "TEXT")
    String note;

    LocalDateTime treatmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TreatmentStatus status;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    Staff doctor;

    @OneToOne(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Prescription prescription;

    Instant deletedAt;

    @Version
    @ColumnDefault("0")
    Long version;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;
}
