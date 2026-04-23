package cd.beapi.repository.jpa;

import cd.beapi.entity.TreatmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TreatmentCategoryRepository extends
        JpaRepository<TreatmentCategory, Long>,
        QuerydslPredicateExecutor<TreatmentCategory> {
    boolean existsByCode(String code);
}

