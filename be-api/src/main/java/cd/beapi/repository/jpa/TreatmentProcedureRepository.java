package cd.beapi.repository.jpa;

import cd.beapi.entity.TreatmentProcedure;
import cd.beapi.dto.response.ServiceUsageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TreatmentProcedureRepository extends JpaRepository<TreatmentProcedure, Long> {
    @EntityGraph(attributePaths = {"procedure"})
    List<TreatmentProcedure> findByTreatmentId(Long treatmentId);

    void deleteByTreatmentId(Long treatmentId);

    @Query("""
            select new cd.beapi.dto.response.ServiceUsageResponse(p.name, sum(tp.quantity))
            from TreatmentProcedure tp
            join tp.treatment t
            join tp.procedure p
            where t.deletedAt is null
              and p.deletedAt is null
            group by p.id, p.name
            order by sum(tp.quantity) desc, p.name asc
            """)
    List<ServiceUsageResponse> findTopServiceUsage(Pageable pageable);
}
