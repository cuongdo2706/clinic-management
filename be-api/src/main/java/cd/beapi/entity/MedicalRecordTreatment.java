package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "medical_record_treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecordTreatment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "treatment_id")
    Treatment treatment;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    BigDecimal unitPrice;

    @Column(columnDefinition = "TEXT")
    String note;
}


