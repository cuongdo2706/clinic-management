package cd.beapi.repository.jpa;

import cd.beapi.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"role"})
    @Query(value = """
                    SELECT u FROM User u
                    WHERE u.username = :username
                    AND u.isActive = TRUE
            """)
    Optional<User> findByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"role", "role.permissions", "role.permissions.page", "role.permissions.action"})
    @Query(value = """
                    SELECT u FROM User u
                    WHERE u.username = :username
                    AND u.isActive = TRUE
            """)
    Optional<User> findByUsernameWithRolePermissions(@Param("username") String username);
}
