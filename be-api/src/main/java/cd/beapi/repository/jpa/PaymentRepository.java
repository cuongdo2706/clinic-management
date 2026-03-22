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

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.invoice " +
            "LEFT JOIN FETCH p.cashier " +
            "WHERE p.invoice.id = :invoiceId " +
            "ORDER BY p.paidAt DESC")
    List<Payment> findByInvoiceId(@Param("invoiceId") Long invoiceId);

    // Tổng đã thanh toán cho 1 hóa đơn
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.invoice.id = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") Long invoiceId);

    // Lịch sử thanh toán trong khoảng thời gian
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.invoice i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH p.cashier " +
            "WHERE p.paidAt BETWEEN :from AND :to " +
            "ORDER BY p.paidAt DESC")
    List<Payment> findByDateRange(
            @Param("from") Instant from,
            @Param("to") Instant to);

    // Tổng thu theo phương thức thanh toán
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.paymentMethod = :method " +
            "AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumByMethodAndDateRange(
            @Param("method") PaymentMethod method,
            @Param("from") Instant from,
            @Param("to") Instant to);

    // Tổng thu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.paidAt BETWEEN :from AND :to")
    BigDecimal sumByDateRange(
            @Param("from") Instant from,
            @Param("to") Instant to);
}


