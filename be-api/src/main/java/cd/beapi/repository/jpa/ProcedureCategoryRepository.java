package cd.beapi.repository.jpa;

import cd.beapi.entity.ProcedureCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProcedureCategoryRepository extends
        JpaRepository<ProcedureCategory, Long>,
        QuerydslPredicateExecutor<ProcedureCategory> {
    boolean existsByCode(String code);
}

