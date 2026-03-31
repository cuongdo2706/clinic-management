package cd.beapi.repository.jpa;

import cd.beapi.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface PatientRepository extends
        JpaRepository<Patient, Long>,
        QuerydslPredicateExecutor<Patient> {

    Optional<Patient> findByPhone(String phone);
}

