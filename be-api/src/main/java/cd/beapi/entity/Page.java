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
@Table(name = "pages")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Page extends BaseEntity{
    String code;

    String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "page_allowed_permissions",
            joinColumns = @JoinColumn(name = "page_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    Set<PermissionType> allowedPermissions = new HashSet<>();
}
