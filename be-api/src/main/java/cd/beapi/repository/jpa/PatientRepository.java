package cd.beapi.repository.jpa;

import cd.beapi.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends
        JpaRepository<Patient, Long>,
        QuerydslPredicateExecutor<Patient> {
    boolean existsByCode(String code);

    @Query("select p from Patient p join p.user u where u.username = :username")
    Optional<Patient> findByUsername(@Param("username") String username);

    Optional<Patient> findFirstByPhoneOrGuardianPhone(String phone, String guardianPhone);

    @EntityGraph(attributePaths = {"user", "user.role"})
    @Query("""
            SELECT p FROM Patient p
            WHERE p.user IS NOT NULL
            ORDER BY p.createdAt DESC
            """)
    List<Patient> findPatientAccounts();

    @EntityGraph(attributePaths = {"user", "user.role"})
    @Query("""
            SELECT p FROM Patient p
            WHERE p.user.id = :userId
            """)
    Optional<Patient> findByUserIdWithAccount(@Param("userId") Long userId);
}
