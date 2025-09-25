package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;

import java.util.List;

/**
 * Interface que define los servicios disponibles para la gestión de odontólogos
 */
public interface DentistService {

    DentistResponseDto createDentist(DentistRequestDto dentistRequestDto);

    DentistResponseDto searchById(Long id);

    DentistResponseDto searchByLicenseNumber(String licenseNumber);

    List<DentistResponseDto> findAllActive();

    List<DentistResponseDto> searchBySpecialty(String specialty);

    DentistResponseDto updateDentist(Long id, DentistUpdateRequestDto dentistUpdateRequestDto);

    void deleteDentist(Long id);

    boolean existsByLicense(String licenseNumber);

    boolean existsByEmail(String email);

    long countActiveDentist();

    DentistPatientsResponseDto getPatientsByDentistId(Long dentistId);

    DentistPatientsResponseDto getActivePatientsByDentistId(Long dentistId);

    PatientResponseDto createPatientForDentist(Long dentistId, PatientRequestDto patientRequestDto);

}
