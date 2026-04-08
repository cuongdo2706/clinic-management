package cd.beapi.repository.jpa;

import cd.beapi.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MedicineRepository extends
        JpaRepository<Medicine, Long>,
        QuerydslPredicateExecutor<Medicine> {
}


