package cd.beapi.repository.jpa;

import cd.beapi.entity.Invoice;
import cd.beapi.enumerate.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.appointment " +
            "LEFT JOIN FETCH i.items " +
            "WHERE i.id = :id")
    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.appointment " +
            "WHERE i.appointment.id = :appointmentId")
    Optional<Invoice> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.appointment " +
            "WHERE i.code = :code")
    Optional<Invoice> findByCode(@Param("code") String code);

    // Hóa đơn chưa thanh toán
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.patient " +
            "LEFT JOIN FETCH i.appointment " +
            "WHERE i.status IN :statuses " +
            "ORDER BY i.createdAt DESC")
    List<Invoice> findByStatuses(@Param("statuses") List<InvoiceStatus> statuses);

    // Hóa đơn của bệnh nhân
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.appointment " +
            "WHERE i.patient.id = :patientId " +
            "ORDER BY i.createdAt DESC")
    List<Invoice> findByPatientId(@Param("patientId") Long patientId);

    // Tổng doanh thu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(i.finalAmount), 0) FROM Invoice i " +
            "WHERE i.status = cd.beapi.enumerate.InvoiceStatus.PAID " +
            "AND i.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByDateRange(
            @Param("from") Instant from,
            @Param("to") Instant to);

    // Đếm hóa đơn theo trạng thái
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") InvoiceStatus status);
}


