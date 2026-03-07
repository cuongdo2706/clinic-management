package cd.beapi.repository.jpa;

import cd.beapi.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //    @Query(nativeQuery = true, value = """
//                    SELECT * FROM users
//                    WHERE (email = :username OR phone = :username)
//                    AND is_active = TRUE
//            """)
//    Optional<User> findByEmailOrPhone(@Param("username") String username);
    @EntityGraph(attributePaths = {"roles"})
    @Query(value = """
                    SELECT u FROM User u
                    WHERE u.username = :username
                    AND u.isActive = TRUE
            """)
    Optional<User> findByUsername(@Param("username") String username);
}
