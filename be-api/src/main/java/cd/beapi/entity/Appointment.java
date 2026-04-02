package cd.beapi.entity;

import cd.beapi.enumerate.AppointmentStatus;
import cd.beapi.enumerate.BookingChannel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @Column(columnDefinition = "TEXT")
    String symptom;

    @Column(columnDefinition = "TEXT")
    String note;

    @Enumerated(EnumType.STRING)
    AppointmentStatus status;

    Instant deletedAt;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    Staff staff;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    Patient patient;

//    @ManyToOne
//    @JoinColumn(name = "room_id")
//    Room room;


    @OneToOne(mappedBy = "appointment")
    VisitRegistration visitRegistration;
}
