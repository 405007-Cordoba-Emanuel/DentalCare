package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Odontogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdontogramRepository extends JpaRepository<Odontogram, Long> {
    
    // Buscar todos los odontogramas activos de un paciente
    List<Odontogram> findByPatientIdAndIsActiveTrueOrderByCreatedDatetimeDesc(Long patientId);
    
    // Buscar odontograma específico activo de un paciente
    Optional<Odontogram> findByIdAndPatientIdAndIsActiveTrue(Long id, Long patientId);
    
    // Contar odontogramas activos de un paciente
    long countByPatientIdAndIsActiveTrue(Long patientId);
}

