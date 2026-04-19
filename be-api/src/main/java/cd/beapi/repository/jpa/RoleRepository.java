package cd.beapi.repository.jpa;

import cd.beapi.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    @EntityGraph(attributePaths = {"permissions", "permissions.page", "permissions.action"})
    @Query("""
            SELECT r FROM Role r
            WHERE r.id = :id
            """)
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);
}
