package cd.beapi.repository.jpa;

import cd.beapi.entity.Appointment;
import cd.beapi.enumerate.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, QuerydslPredicateExecutor<Appointment> {

    @EntityGraph(attributePaths = {"patient", "dentist", "receptionist"})
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findByIdWithRelations(@Param("id") Long id);

    @Query("""
            select count(a) from Appointment a
            where a.dentist.id = :dentistId
              and a.appointmentDate = :appointmentDate
              and (:excludeId is null or a.id <> :excludeId)
              and a.status not in :ignoredStatuses
            """)
    long countActiveAtDateTime(@Param("dentistId") Long dentistId,
                               @Param("appointmentDate") LocalDateTime appointmentDate,
                               @Param("excludeId") Long excludeId,
                               @Param("ignoredStatuses") Collection<AppointmentStatus> ignoredStatuses);

    @Query("""
            select a from Appointment a
            where a.dentist.id = :dentistId
              and a.appointmentDate >= :start
              and a.appointmentDate < :end
              and a.status not in :ignoredStatuses
            """)
    List<Appointment> findActiveByDentistAndDateRange(@Param("dentistId") Long dentistId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end,
                                                      @Param("ignoredStatuses") Collection<AppointmentStatus> ignoredStatuses);
}

