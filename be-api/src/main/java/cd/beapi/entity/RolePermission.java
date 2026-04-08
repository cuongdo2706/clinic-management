package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_page_action",
                        columnNames = {"page","action","role"}
                )
        })
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "page_id")
    Page page;

    @ManyToOne
    @JoinColumn(name = "action_id")
    Action action;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;
}
