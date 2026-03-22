package cd.beapi.repository.jpa;

import cd.beapi.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    @Query("SELECT pi FROM PrescriptionItem pi " +
            "LEFT JOIN FETCH pi.medicine " +
            "WHERE pi.prescription.id = :prescriptionId " +
            "ORDER BY pi.id")
    List<PrescriptionItem> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}


