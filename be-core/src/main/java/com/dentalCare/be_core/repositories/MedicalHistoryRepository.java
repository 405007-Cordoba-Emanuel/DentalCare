package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    @Query("SELECT m FROM MedicalHistory m WHERE m.patient.id = :patientId AND m.active = true ORDER BY m.entryDate DESC")
    List<MedicalHistory> findByPatientIdAndActiveTrue(@Param("patientId") Long patientId);

    @Query("SELECT m FROM MedicalHistory m WHERE m.dentist.id = :dentistId AND m.patient.id = :patientId AND m.active = true ORDER BY m.entryDate DESC")
    List<MedicalHistory> findByDentistIdAndPatientIdAndActiveTrue(@Param("dentistId") Long dentistId, @Param("patientId") Long patientId);

    @Query("SELECT m FROM MedicalHistory m WHERE m.id = :id AND m.dentist.id = :dentistId AND m.active = true")
    Optional<MedicalHistory> findByIdAndDentistIdAndActiveTrue(@Param("id") Long id, @Param("dentistId") Long dentistId);

    @Query("SELECT m FROM MedicalHistory m WHERE m.id = :id AND m.patient.id = :patientId AND m.active = true")
    Optional<MedicalHistory> findByIdAndPatientIdAndActiveTrue(@Param("id") Long id, @Param("patientId") Long patientId);
}
