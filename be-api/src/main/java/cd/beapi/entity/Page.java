package cd.beapi.entity;

import cd.beapi.enumerate.PageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "pages")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Short id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    PageType code;

    String name;
}
