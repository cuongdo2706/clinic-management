package cd.beapi.repository.jpa;

import cd.beapi.entity.Payment;
import cd.beapi.enumerate.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}


