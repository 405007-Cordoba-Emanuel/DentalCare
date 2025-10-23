package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {

    Optional<Dentist> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByUserId(Long userId);

    Optional<Dentist> findByUserId(Long userId);

    @Query("SELECT d FROM Dentist d WHERE d.specialty = :specialty AND d.active = true")
    java.util.List<Dentist> findActiveBySpecialty(@Param("specialty") String specialty);

    @Query("SELECT d FROM Dentist d WHERE d.active = true")
    java.util.List<Dentist> findAllActive();

    @Query("SELECT COUNT(o) FROM Dentist o WHERE o.active = true")
    long countActiveDentist();

    @Query("SELECT d FROM Dentist d LEFT JOIN FETCH d.patients WHERE d.id = :dentistId")
    Optional<Dentist> findByIdWithPatients(@Param("dentistId") Long dentistId);

    @Query("SELECT d FROM Dentist d LEFT JOIN FETCH d.patients p WHERE d.id = :dentistId AND p.active = true")
    Optional<Dentist> findByIdWithActivePatients(@Param("dentistId") Long dentistId);
}
