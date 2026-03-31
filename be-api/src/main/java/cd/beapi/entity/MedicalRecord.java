package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE medical_records SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class MedicalRecord extends BaseEntity {
    @Column(nullable = false, unique = true)
    String code;

    @Column(columnDefinition = "TEXT")
    String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    String diagnosis;

    @Column(columnDefinition = "TEXT")
    String treatmentPlan;

    @Column(columnDefinition = "TEXT")
    String notes;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    Patient patient;

    @ManyToOne
    @JoinColumn(name = "dentist_id")
    Staff staff;

    @OneToOne
    @JoinColumn(name = "visit_registration_id", unique = true)
    VisitRegistration visitRegistration;

    @ManyToMany
    @JoinTable(
            name = "medical_record_services",
            joinColumns = @JoinColumn(name = "medical_record_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    Set<Service> services;
}
