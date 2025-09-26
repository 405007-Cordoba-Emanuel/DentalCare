package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p WHERE p.dentist.id = :dentistId AND p.active = true ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDentistIdAndActiveTrue(@Param("dentistId") Long dentistId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.active = true ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByPatientIdAndActiveTrue(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.dentist.id = :dentistId AND p.patient.id = :patientId AND p.active = true ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDentistIdAndPatientIdAndActiveTrue(@Param("dentistId") Long dentistId, @Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.id = :id AND p.dentist.id = :dentistId AND p.active = true")
    Optional<Prescription> findByIdAndDentistIdAndActiveTrue(@Param("id") Long id, @Param("dentistId") Long dentistId);

    @Query("SELECT p FROM Prescription p WHERE p.id = :id AND p.patient.id = :patientId AND p.active = true")
    Optional<Prescription> findByIdAndPatientIdAndActiveTrue(@Param("id") Long id, @Param("patientId") Long patientId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.dentist.id = :dentistId AND p.active = true")
    long countByDentistIdAndActiveTrue(@Param("dentistId") Long dentistId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patient.id = :patientId AND p.active = true")
    long countByPatientIdAndActiveTrue(@Param("patientId") Long patientId);
}
