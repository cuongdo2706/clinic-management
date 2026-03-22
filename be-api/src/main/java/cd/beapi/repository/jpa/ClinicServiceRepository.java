package cd.beapi.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ClinicServiceRepository extends JpaRepository<cd.beapi.entity.Service, Long> {

    @Query("SELECT s FROM Service s WHERE s.id IN :ids")
    List<cd.beapi.entity.Service> findAllByIdIn(@Param("ids") Set<Long> ids);

    @Query("SELECT s FROM Service s " +
            "WHERE s.isActive = true " +
            "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR s.code LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.name")
    List<cd.beapi.entity.Service> searchActiveByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM Service s WHERE s.isActive = true ORDER BY s.name")
    List<cd.beapi.entity.Service> findAllActive();

    @Query("SELECT s FROM Service s " +
            "LEFT JOIN FETCH s.category " +
            "WHERE s.category.id = :categoryId " +
            "AND s.isActive = true " +
            "ORDER BY s.name")
    List<cd.beapi.entity.Service> findActiveByCategoryId(@Param("categoryId") Long categoryId);
}


