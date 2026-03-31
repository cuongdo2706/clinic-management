package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@EntityListeners(AuditingEntityListener.class)
@Setter
@Builder
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Column(unique = true, nullable = false)
    String username;

    @Column(nullable = false)
    String password;

    @Builder.Default
    Boolean isActive = true;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;
}