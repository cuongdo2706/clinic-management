package cd.beapi.repository.jpa;

import cd.beapi.entity.Permission;
import cd.beapi.enumerate.ActionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @EntityGraph(attributePaths = {"page", "action"})
    @Query("SELECT p FROM Permission p " +
           "WHERE p.page.code = :pageCode " +
           "AND p.action.code = :actionCode")
    Optional<Permission> findByPageAndAction(@Param("pageCode") String pageCode, @Param("actionCode") ActionType actionCode);
}

