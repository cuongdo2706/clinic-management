package cd.beapi.entity;

import cd.beapi.enumerate.ActionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "actions")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Action{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Short id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    ActionType code;

    String name;
}
