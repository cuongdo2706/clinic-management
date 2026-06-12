package cd.beapi.repository.jpa;

import cd.beapi.entity.Staff;
import cd.beapi.enumerate.StaffType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long>, QuerydslPredicateExecutor<Staff> {
    boolean existsByCode(String code);

    long countByStaffTypeAndIsActiveTrue(StaffType staffType);

    @Query("SELECT s FROM Staff s WHERE s.staffType = :type ORDER BY s.fullName")
    List<Staff> findByStaffType(@Param("type") StaffType staffType);

    @Query("SELECT s FROM Staff s WHERE s.staffType = :type AND s.isActive = true ORDER BY s.fullName")
    List<Staff> findActiveByStaffType(@Param("type") StaffType staffType);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM Staff s WHERE s.id = :id")
    Optional<Staff> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT s FROM Staff s " +
            "WHERE s.staffType = :type " +
            "AND (LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR s.code LIKE CONCAT('%', :keyword, '%'))")
    List<Staff> searchByTypeAndKeyword(@Param("type") StaffType staffType,
                                       @Param("keyword") String keyword);

    @Query("SELECT s FROM Staff s WHERE s.user.id = :userId")
    Optional<Staff> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Staff s JOIN s.user u WHERE u.username = :username")
    Optional<Staff> findByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"user", "user.role"})
    @Query("""
            SELECT s FROM Staff s
            WHERE s.user IS NOT NULL
            ORDER BY s.createdAt DESC
            """)
    List<Staff> findStaffAccounts();

    @EntityGraph(attributePaths = {"user", "user.role"})
    @Query("""
            SELECT s FROM Staff s
            WHERE s.user.id = :userId
            """)
    Optional<Staff> findByUserIdWithAccount(@Param("userId") Long userId);
}


