package cd.beapi.repository.jpa;

import cd.beapi.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
    @EntityGraph(attributePaths = {"medicine"})
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    void deleteByPrescriptionId(Long prescriptionId);
}
