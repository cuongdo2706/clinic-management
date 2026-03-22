package cd.beapi.repository.jpa;

import cd.beapi.entity.Appointment;
import cd.beapi.enumerate.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.code = :code")
    Optional<Appointment> findByCode(@Param("code") String code);

    // Kiểm tra nha sĩ bị trùng lịch
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.staff.id = :dentistId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN :statuses " +
            "AND a.startTime < :endTime " +
            "AND a.endTime > :startTime")
    List<Appointment> findConflictingAppointments(
            @Param("dentistId") Long dentistId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<AppointmentStatus> statuses);

    // Lịch hẹn của nha sĩ trong ngày
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "WHERE a.staff.id = :dentistId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN :statuses " +
            "ORDER BY a.startTime")
    List<Appointment> findByDentistAndDateAndStatuses(
            @Param("dentistId") Long dentistId,
            @Param("date") LocalDate date,
            @Param("statuses") List<AppointmentStatus> statuses);

    // Lịch hẹn trong ngày theo trạng thái
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.appointmentDate = :date " +
            "AND a.status = :status " +
            "ORDER BY a.startTime")
    List<Appointment> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status);

    // Lịch hẹn trong khoảng ngày
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.appointmentDate BETWEEN :fromDate AND :toDate " +
            "ORDER BY a.appointmentDate, a.startTime")
    List<Appointment> findByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Lịch hẹn của bệnh nhân
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.staff " +
            "WHERE a.patient.id = :patientId " +
            "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    // Đếm lịch hẹn theo trạng thái trong ngày
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentDate = :date AND a.status = :status")
    Long countByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status);
}


