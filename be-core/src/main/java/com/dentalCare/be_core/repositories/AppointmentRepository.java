package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Appointment;
import com.dentalCare.be_core.entities.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    List<Appointment> findByDentistIdAndActiveTrueOrderByStartDateTimeAsc(Long dentistId);

    List<Appointment> findByPatientIdAndActiveTrueOrderByStartDateTimeAsc(Long patientId);

    List<Appointment> findByDentistIdAndPatientIdAndActiveTrueOrderByStartDateTimeAsc(Long dentistId, Long patientId);

    Optional<Appointment> findByIdAndDentistIdAndActiveTrue(Long appointmentId, Long dentistId);

    Optional<Appointment> findByIdAndPatientIdAndActiveTrue(Long appointmentId, Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.dentist.id = :dentistId AND a.active = true " +
           "AND a.startDateTime >= :startDate AND a.startDateTime < :endDate " +
           "ORDER BY a.startDateTime ASC")
    List<Appointment> findByDentistIdAndDateRange(@Param("dentistId") Long dentistId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.dentist.id = :dentistId AND a.active = true " +
           "AND YEAR(a.startDateTime) = :year AND MONTH(a.startDateTime) = :month " +
           "ORDER BY a.startDateTime ASC")
    List<Appointment> findByDentistIdAndMonth(@Param("dentistId") Long dentistId,
                                             @Param("year") int year,
                                             @Param("month") int month);

    @Query("SELECT a FROM Appointment a WHERE a.dentist.id = :dentistId AND a.active = true " +
           "AND DATE(a.startDateTime) = :date " +
           "ORDER BY a.startDateTime ASC")
    List<Appointment> findByDentistIdAndDate(@Param("dentistId") Long dentistId,
                                           @Param("date") java.time.LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.active = true " +
           "AND a.startDateTime >= :currentDateTime " +
           "ORDER BY a.startDateTime ASC")
    List<Appointment> findUpcomingByPatientId(@Param("patientId") Long patientId,
                                            @Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.active = true " +
           "AND a.startDateTime < :currentDateTime " +
           "ORDER BY a.startDateTime DESC")
    List<Appointment> findPastByPatientId(@Param("patientId") Long patientId,
                                         @Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.dentist.id = :dentistId AND a.active = true " +
           "AND a.status NOT IN (com.dentalCare.be_core.entities.AppointmentStatus.CANCELADO, com.dentalCare.be_core.entities.AppointmentStatus.AUSENTE) " +
           "AND ((a.startDateTime < :endTime AND a.endDateTime > :startTime))")
    boolean existsTimeConflict(@Param("dentistId") Long dentistId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.dentist.id = :dentistId AND a.active = true " +
           "AND a.id != :excludeAppointmentId " +
           "AND a.status NOT IN (com.dentalCare.be_core.entities.AppointmentStatus.CANCELADO, com.dentalCare.be_core.entities.AppointmentStatus.AUSENTE) " +
           "AND ((a.startDateTime < :endTime AND a.endDateTime > :startTime))")
    boolean existsTimeConflictExcluding(@Param("dentistId") Long dentistId,
                                       @Param("excludeAppointmentId") Long excludeAppointmentId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    long countByDentistIdAndStatusAndActiveTrue(Long dentistId, AppointmentStatus status);

    long countByPatientIdAndStatusAndActiveTrue(Long patientId, AppointmentStatus status);

    long countByDentistIdAndActiveTrue(Long dentistId);

    long countByPatientIdAndActiveTrue(Long patientId);
}
