package cd.beapi.entity;

import cd.beapi.enumerate.PermissionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "role_page_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_page_permissions_role_page",
                        columnNames = {"role_id", "page_id"}
                )
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePagePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    Page page;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_page_permission_details",
            joinColumns = @JoinColumn(name = "role_page_permission_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    Set<PermissionType> grantedPermissions = new HashSet<>();

    // ───── Check đơn lẻ ─────

    public boolean hasPermission(PermissionType type) {
        return grantedPermissions.contains(type);
    }

    public boolean hasAnyPermission(PermissionType... types) {
        return Arrays.stream(types).anyMatch(grantedPermissions::contains);
    }

    public boolean hasAllPermissions(PermissionType... types) {
        return grantedPermissions.containsAll(Arrays.asList(types));
    }

    // ───── Tất cả / Không có quyền nào ─────

    @Transient
    public boolean isFullAccess() {
        return grantedPermissions.containsAll(
                Arrays.asList(PermissionType.values())
        );
    }

    @Transient
    public boolean isEmpty() {
        return grantedPermissions.isEmpty();
    }

    // ───── Mutate ─────

    public void grant(PermissionType type) {
        grantedPermissions.add(type);
    }

    public void revoke(PermissionType type) {
        grantedPermissions.remove(type);
    }

    public void grantAll() {
        grantedPermissions.addAll(Arrays.asList(PermissionType.values()));
    }

    public void revokeAll() {
        grantedPermissions.clear();
    }

    // ───── Validate trước khi lưu ─────

    @PrePersist
    @PreUpdate
    public void validatePermissions() {
        // Loại bỏ permission không hợp lệ với page
        grantedPermissions.retainAll(page.getAllowedPermissions());
    }
}
