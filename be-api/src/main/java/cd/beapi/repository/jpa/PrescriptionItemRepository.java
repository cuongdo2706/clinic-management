package cd.beapi.repository.jpa;

import cd.beapi.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
}


