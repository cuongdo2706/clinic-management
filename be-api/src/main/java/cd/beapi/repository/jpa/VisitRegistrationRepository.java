package cd.beapi.repository.jpa;

import cd.beapi.entity.VisitRegistration;
import cd.beapi.enumerate.VisitRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitRegistrationRepository extends JpaRepository<VisitRegistration, Long> {

    @Query("SELECT vr FROM VisitRegistration vr " +
            "LEFT JOIN FETCH vr.appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE vr.appointment.id = :appointmentId")
    Optional<VisitRegistration> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    // Số thứ tự lớn nhất trong ngày
    @Query("SELECT COALESCE(MAX(vr.queueNumber), 0) FROM VisitRegistration vr " +
            "WHERE vr.appointment.appointmentDate = :date")
    Integer findMaxQueueNumberByDate(@Param("date") LocalDate date);

    // Danh sách chờ khám trong ngày (sắp theo số thứ tự)
    @Query("SELECT vr FROM VisitRegistration vr " +
            "LEFT JOIN FETCH vr.appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.appointmentDate = :date " +
            "AND vr.status = :status " +
            "ORDER BY vr.queueNumber ASC")
    List<VisitRegistration> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") VisitRegistrationStatus status);

    // Đếm số người chờ trong ngày
    @Query("SELECT COUNT(vr) FROM VisitRegistration vr " +
            "WHERE vr.appointment.appointmentDate = :date " +
            "AND vr.status = :status")
    Long countByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") VisitRegistrationStatus status);
}


