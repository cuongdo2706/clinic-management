package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "treatments")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE treatments SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Treatment extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    BigDecimal price;

    String unit;

    Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "treatment_category_id")
    TreatmentCategory treatmentCategory;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;
}
