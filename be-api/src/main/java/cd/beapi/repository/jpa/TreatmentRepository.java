package cd.beapi.repository.jpa;

import cd.beapi.entity.Treatment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    @EntityGraph(attributePaths = {"patient", "doctor", "appointment", "prescription", "prescription.doctor"})
    @Query("select t from Treatment t where t.id = :id")
    Optional<Treatment> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"patient", "doctor", "appointment", "prescription"})
    List<Treatment> findByPatientIdOrderByTreatmentDateDesc(Long patientId);

    Optional<Treatment> findByAppointmentId(Long appointmentId);
}
