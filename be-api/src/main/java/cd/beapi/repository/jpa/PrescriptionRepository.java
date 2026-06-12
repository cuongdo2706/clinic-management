package cd.beapi.repository.jpa;

import cd.beapi.entity.Prescription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    @EntityGraph(attributePaths = {"treatment", "treatment.patient", "treatment.doctor", "doctor"})
    Optional<Prescription> findByTreatmentId(Long treatmentId);

    @EntityGraph(attributePaths = {"treatment", "treatment.patient", "treatment.doctor", "doctor"})
    List<Prescription> findByTreatmentPatientIdOrderByPrescribedAtDesc(Long patientId);
}


