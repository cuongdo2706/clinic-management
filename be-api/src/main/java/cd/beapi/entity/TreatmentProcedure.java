package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "treatment_procedures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentProcedure extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "treatment_id", nullable = false)
    Treatment treatment;

    @ManyToOne
    @JoinColumn(name = "procedure_id", nullable = false)
    Procedure procedure;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    BigDecimal unitPrice;

    @Column(columnDefinition = "TEXT")
    String note;
}
