package cd.beapi.repository.jpa;

import cd.beapi.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Short> {
}

