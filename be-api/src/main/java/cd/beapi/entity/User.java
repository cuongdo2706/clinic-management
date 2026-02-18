package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter
@Setter
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity implements UserDetails {
    @Column(unique = true)
    String username;

    @Column(unique = true)
    String phone;

    @Column(unique = true)
    String email;

    @Column(nullable = false)
    String password;

    Boolean isActive;

    Instant deletedAt;

    @CreationTimestamp
    @Column(updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant modifiedAt;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(role.getName())
        );
    }

    @Override
    public String getUsername() {
        return phone != null ? phone : email;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive) && deletedAt == null;
    }
}
