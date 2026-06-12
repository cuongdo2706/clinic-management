package cd.beapi.repository.jpa;

import cd.beapi.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface ProcedureRepository extends
        JpaRepository<Procedure, Long>,
        QuerydslPredicateExecutor<Procedure> {
    boolean existsByCode(String code);

    long countByIsActiveTrue();

    List<Procedure> findByIsActiveTrueOrderByName();
}
