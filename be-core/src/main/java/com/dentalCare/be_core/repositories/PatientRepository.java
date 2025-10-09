package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByDni(String dni);

    boolean existsByDni(String dni);

    Optional<Patient> findByUserId(Long userId);

    @Query("SELECT p FROM Patient p WHERE p.active = true")
    java.util.List<Patient> findAllActive();

    @Query("SELECT COUNT(o) FROM Patient o WHERE o.active = true")
    long countActivePatient();
}
