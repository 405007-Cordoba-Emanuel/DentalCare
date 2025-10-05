package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Patient;

import java.util.List;

/**
 * Interface que define los servicios disponibles para la gestión de odontólogos
 */
public interface PatientService {


    PatientResponseDto searchById(Long id);

    PatientResponseDto searchByDni(String dni);

    List<PatientResponseDto> findAllActive();

    PatientResponseDto updatePatient(Long id, PatientUpdateRequestDto patientUpdateRequestDto);

    void deletePatient(Long id);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    long countActivePatient();

    /**
     * Validates that a patient belongs to a specific dentist and is active
     * Returns the patient entity for further use
     */
    Patient validatePatientOwnershipAndActive(Long patientId, Long dentistId);

    /**
     * Gets patient entity by ID for cross-service relationships
     */
    Patient getPatientEntityById(Long patientId);
}
