package cd.beapi.entity;

import cd.beapi.enumerate.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
public class Appointment extends BaseEntity{
    @Column(unique = true,nullable = false)
    String code;

    LocalDateTime appointmentDate;

    LocalTime startTime;

    LocalTime endTime;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @Column(columnDefinition = "TEXT")
    String note;

    @Enumerated(EnumType.STRING)
    AppointmentStatus status;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    Staff staff;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    Patient patient;

//    @ManyToOne
//    @JoinColumn(name = "room_id")
//    Room room;

    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;

    @OneToOne(mappedBy = "appointment")
    VisitRegistration visitRegistration;
}
