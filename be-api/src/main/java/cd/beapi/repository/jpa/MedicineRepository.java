package cd.beapi.repository.jpa;

import cd.beapi.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("SELECT m FROM Medicine m WHERE m.id IN :ids")
    List<Medicine> findAllByIdIn(@Param("ids") Set<Long> ids);

    @Query("SELECT m FROM Medicine m " +
            "WHERE m.isActive = true " +
            "AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR m.code LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.name")
    List<Medicine> searchActiveByKeyword(@Param("keyword") String keyword);

    @Query("SELECT m FROM Medicine m WHERE m.isActive = true ORDER BY m.name")
    List<Medicine> findAllActive();
}


