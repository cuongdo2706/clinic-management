package cd.beapi.repository.jpa;

import cd.beapi.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM users
                WHERE username = :username
            )
            """, nativeQuery = true)
    boolean existsByUsernameIncludingDeleted(@Param("username") String username);

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

    @EntityGraph(attributePaths = {"role"})
    @Query("""
            SELECT u FROM User u
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithRole(@Param("id") Long id);
}
