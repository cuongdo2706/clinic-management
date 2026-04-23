package cd.beapi.repository.jpa;

import cd.beapi.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TreatmentRepository extends
        JpaRepository<Treatment, Long>,
        QuerydslPredicateExecutor<Treatment> {
    boolean existsByCode(String code);
}

