package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;

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

}
