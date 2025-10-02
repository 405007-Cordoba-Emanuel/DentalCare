package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    @Query("SELECT t FROM Treatment t WHERE t.patient.id = :patientId AND t.active = true ORDER BY t.startDate DESC")
    List<Treatment> findByPatientIdAndActiveTrue(@Param("patientId") Long patientId);

    @Query("SELECT t FROM Treatment t WHERE t.dentist.id = :dentistId AND t.patient.id = :patientId AND t.active = true ORDER BY t.startDate DESC")
    List<Treatment> findByDentistIdAndPatientIdAndActiveTrue(@Param("dentistId") Long dentistId, @Param("patientId") Long patientId);

    @Query("SELECT t FROM Treatment t WHERE t.id = :id AND t.dentist.id = :dentistId AND t.active = true")
    Optional<Treatment> findByIdAndDentistIdAndActiveTrue(@Param("id") Long id, @Param("dentistId") Long dentistId);

    @Query("SELECT t FROM Treatment t WHERE t.id = :id AND t.patient.id = :patientId AND t.active = true")
    Optional<Treatment> findByIdAndPatientIdAndActiveTrue(@Param("id") Long id, @Param("patientId") Long patientId);

    @Query("SELECT t FROM Treatment t WHERE t.patient.id = :patientId AND t.status = :status AND t.active = true ORDER BY t.startDate DESC")
    List<Treatment> findByPatientIdAndStatusAndActiveTrue(@Param("patientId") Long patientId, @Param("status") String status);

    @Query("SELECT COUNT(t) FROM Treatment t WHERE t.patient.id = :patientId AND t.status = :status AND t.active = true")
    long countByPatientIdAndStatus(@Param("patientId") Long patientId, @Param("status") String status);
}
