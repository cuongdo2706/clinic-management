package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_page_action",
                        columnNames = {"page_id", "action_id"}
                )
        })
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    Page page;

    @ManyToOne
    @JoinColumn(name = "action_id", nullable = false)
    Action action;
}
