package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE medicines SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Medicine extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    String name;

    String unit;

    @Column(columnDefinition = "TEXT")
    String description;

    @Builder.Default
    Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "medicine_category_id")
    MedicineCategory category;

    Instant deletedAt;

    @Version
    @ColumnDefault("0")
    Long version;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;
}
