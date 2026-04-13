package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "treatment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "treatment_id")
    Treatment treatment;

    @Column(nullable = false)
    Integer quantity;

    /**
     * Snapshot giá dịch vụ tại thời điểm khám.
     * Tránh bị ảnh hưởng khi giá dịch vụ thay đổi về sau.
     */
    @Column(nullable = false)
    BigDecimal unitPrice;

    @Column(columnDefinition = "TEXT")
    String note;
}


