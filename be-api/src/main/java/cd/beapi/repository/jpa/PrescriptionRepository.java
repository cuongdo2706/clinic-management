package cd.beapi.repository.jpa;

import cd.beapi.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p " +
            "LEFT JOIN FETCH p.patient " +
            "LEFT JOIN FETCH p.dentist " +
            "LEFT JOIN FETCH p.medicalRecord " +
            "WHERE p.medicalRecord.id = :medicalRecordId")
    Optional<Prescription> findByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    @Query("SELECT p FROM Prescription p " +
            "LEFT JOIN FETCH p.patient " +
            "LEFT JOIN FETCH p.dentist " +
            "LEFT JOIN FETCH p.medicalRecord " +
            "WHERE p.id = :id")
    Optional<Prescription> findByIdWithDetails(@Param("id") Long id);

    // Đơn thuốc theo bệnh nhân
    @Query("SELECT p FROM Prescription p " +
            "LEFT JOIN FETCH p.dentist " +
            "LEFT JOIN FETCH p.medicalRecord " +
            "WHERE p.patient.id = :patientId " +
            "ORDER BY p.createdAt DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);
}


