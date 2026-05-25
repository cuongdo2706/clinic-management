package cd.beapi.entity;

import cd.beapi.enumerate.StaffType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Staff extends BaseEntity{
    @Column(unique = true,nullable = false)
    String code;

    String fullName;

    String phone;

    String email;

    LocalDate dob;

    Boolean gender;

    String address;

    String avatarUrl;

    @Enumerated(EnumType.STRING)
    StaffType staffType;

    @Builder.Default
    Boolean isActive = true;

    @Version
    @ColumnDefault("0")
    Long version;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;

    @OneToOne
    @JoinColumn(name = "user_id",unique = true)
    User user;
}
