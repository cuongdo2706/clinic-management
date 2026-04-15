package cd.beapi.repository.jpa;

import cd.beapi.entity.Permission;
import cd.beapi.enumerate.ActionType;
import cd.beapi.enumerate.PageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p " +
           "JOIN FETCH p.page pg " +
           "JOIN FETCH p.action ac " +
           "WHERE pg.code = :pageCode " +
           "AND ac.code = :actionCode"
    )
    Optional<Permission> findByPageAndAction(@Param("pageCode") PageType pageCode, @Param("actionCode") ActionType actionCode);
}

