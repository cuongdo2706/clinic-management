package cd.beapi.repository.jpa;

import cd.beapi.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SequenceRepository extends JpaRepository<Sequence, String> {
    @Transactional
    @Modifying
    @Query(value = "UPDATE sequences SET value = value + 1 WHERE name = :name RETURNING value", nativeQuery = true)
    Long incrementAndGet(@Param("name") String name);
}
