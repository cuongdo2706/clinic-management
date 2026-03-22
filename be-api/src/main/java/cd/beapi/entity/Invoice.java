package cd.beapi.entity;

import cd.beapi.enumerate.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "UPDATE invoices SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Invoice extends BaseEntity {
    @Column(unique = true, nullable = false)
    String code;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    Patient patient;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    Appointment appointment;

    BigDecimal totalAmount;

    BigDecimal discountAmount;

    BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    InvoiceStatus status;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<InvoiceItem> items;

    Instant deletedAt;

    @CreatedDate
    @Column(updatable = false)
    Instant createdAt;

    @LastModifiedDate
    Instant modifiedAt;
}
