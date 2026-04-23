package cd.beapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "working_schedules")
@Getter
@EntityListeners(AuditingEntityListener.class)
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class WorkingSchedule extends BaseEntity{
    

    @ManyToOne
    @JoinColumn(name = "staff_id")
    Staff staff;
}
