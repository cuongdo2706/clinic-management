package cd.beapi.repository.jpa;

import cd.beapi.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN FETCH mr.patient " +
            "LEFT JOIN FETCH mr.staff " +
            "LEFT JOIN FETCH mr.appointment " +
            "LEFT JOIN FETCH mr.services " +
            "WHERE mr.id = :id")
    Optional<MedicalRecord> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN FETCH mr.patient " +
            "LEFT JOIN FETCH mr.staff " +
            "LEFT JOIN FETCH mr.services " +
            "WHERE mr.appointment.id = :appointmentId")
    Optional<MedicalRecord> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN FETCH mr.patient " +
            "LEFT JOIN FETCH mr.staff " +
            "LEFT JOIN FETCH mr.appointment " +
            "WHERE mr.code = :code")
    Optional<MedicalRecord> findByCode(@Param("code") String code);

    // Lịch sử bệnh án của bệnh nhân
    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN FETCH mr.staff " +
            "LEFT JOIN FETCH mr.appointment " +
            "LEFT JOIN FETCH mr.services " +
            "WHERE mr.patient.id = :patientId " +
            "ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByPatientId(@Param("patientId") Long patientId);

    // Bệnh án theo nha sĩ trong khoảng ngày
    @Query("SELECT mr FROM MedicalRecord mr " +
            "LEFT JOIN FETCH mr.patient " +
            "LEFT JOIN FETCH mr.appointment " +
            "WHERE mr.staff.id = :dentistId " +
            "AND mr.appointment.appointmentDate BETWEEN :fromDate AND :toDate " +
            "ORDER BY mr.createdAt DESC")
    List<MedicalRecord> findByDentistAndDateRange(
            @Param("dentistId") Long dentistId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}


