package cd.beapi.entity;

import cd.beapi.enumerate.VisitRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "visit_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE visit_registrations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class VisitRegistration extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    @Enumerated(EnumType.STRING)
    VisitRegistrationStatus status;

    Integer queueNumber;

    @Column(columnDefinition = "TEXT")
    String note;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "receptionist_id")
    Staff receptionist;
}
