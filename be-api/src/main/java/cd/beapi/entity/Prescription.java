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

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE prescriptions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Prescription extends BaseEntity{
    @Column(unique = true,nullable = false)
    String code;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdDate;

    @LastModifiedDate
    Instant modifiedAt;


}
