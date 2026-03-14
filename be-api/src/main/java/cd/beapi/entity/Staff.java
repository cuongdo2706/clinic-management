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
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE staffs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Staff extends BaseEntity{
    @Column(unique = true,nullable = false)
    String code;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;
}
