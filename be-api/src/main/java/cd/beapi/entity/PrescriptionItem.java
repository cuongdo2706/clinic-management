package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItem extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "prescription_id")
    Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "medicines_id")
    Medicine medicine;

    @Column(columnDefinition = "TEXT")
    String instruction;
}
