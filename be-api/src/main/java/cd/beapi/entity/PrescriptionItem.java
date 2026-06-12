package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    Medicine medicine;

    Integer quantity;

    String dosage;

    String frequency;

    String duration;

    @Column(columnDefinition = "TEXT")
    String instruction;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;
}
