package cd.beapi.repository.jpa;

import cd.beapi.entity.WorkingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, Long> {
    void deleteByStaffId(Long staffId);
}
