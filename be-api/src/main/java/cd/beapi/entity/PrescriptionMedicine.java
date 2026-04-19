package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prescription_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionMedicine extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "prescription_id")
    Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    Medicine medicine;

    Integer quantity;

    String dosage;

    @Column(columnDefinition = "TEXT")
    String instruction;
}
