package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.DentistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class DentistServiceImpl implements DentistService {

    @Autowired
    private DentistRepository dentistRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private ModelMapperUtils modelMapperUtils;

    @Override
    public DentistResponseDto createDentist(DentistRequestDto dentistRequestDto) {
        validateLicenseNumber(dentistRequestDto.getLicenseNumber());
        validateEmailIfPresent(dentistRequestDto.getEmail());

        // Mapear DTO a Entity
        Dentist dentist = modelMapperUtils.map(dentistRequestDto, Dentist.class);
        dentist.setActive(true);
        // Guardar en BD
        Dentist dentistGuardado = dentistRepository.save(dentist);
        // Mapear Entity a Response DTO
		return modelMapperUtils.map(dentistGuardado, DentistResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchById(Long id) {
        Dentist dentist = findDentistById(id);
        return modelMapperUtils.map(dentist, DentistResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchByLicenseNumber(String licenseNumber) {
        Dentist dentist = dentistRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("No licensed dentist found:" + licenseNumber);
                });

        return modelMapperUtils.map(dentist, DentistResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> findAllActive() {
        List<Dentist> dentists = dentistRepository.findAllActive();
        return dentists.stream()
                .map(dentist -> modelMapperUtils.map(dentist, DentistResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> searchBySpecialty(String specialty) {

        List<Dentist> dentists = dentistRepository.findActiveBySpecialty(specialty);

        return dentists.stream()
                .map(dentist -> modelMapperUtils.map(dentist, DentistResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public DentistResponseDto updateDentist(Long id, DentistUpdateRequestDto dentistUpdateRequestDto) {
        Dentist dentistExisting = findDentistById(id);

        validateLicenseNumberForUpdate(dentistExisting.getLicenseNumber(), dentistUpdateRequestDto.getLicenseNumber());
        validateEmailForUpdate(dentistExisting.getEmail(), dentistUpdateRequestDto.getEmail());

        dentistExisting.setFirstName(dentistUpdateRequestDto.getFirstName());
        dentistExisting.setLastName(dentistUpdateRequestDto.getLastName());
        dentistExisting.setLicenseNumber(dentistUpdateRequestDto.getLicenseNumber());
        dentistExisting.setSpecialty(dentistUpdateRequestDto.getSpecialty());
        dentistExisting.setPhone(dentistUpdateRequestDto.getPhone());
        dentistExisting.setEmail(dentistUpdateRequestDto.getEmail());
        dentistExisting.setAddress(dentistUpdateRequestDto.getAddress());
        dentistExisting.setActive(dentistUpdateRequestDto.getActive());

        Dentist dentistUpdated = dentistRepository.save(dentistExisting);
        return modelMapperUtils.map(dentistUpdated, DentistResponseDto.class);
    }

    @Override
    public void deleteDentist(Long id) {
        Dentist dentist = findDentistById(id);
        dentistRepository.delete(dentist);
    }
    @Override
    public boolean existsByLicense(String licenseNumber) {
        return dentistRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return dentistRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDentist() {
        return dentistRepository.countActiveDentist();
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getPatientsByDentistId(Long dentistId) {
        Dentist dentist = findDentistByIdWithPatients(dentistId);
        return buildDentistPatientsResponse(dentist, false);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getActivePatientsByDentistId(Long dentistId) {
        Dentist dentist = findDentistByIdWithActivePatients(dentistId);
        return buildDentistPatientsResponse(dentist, true);
    }

    @Override
    public PatientResponseDto createPatientForDentist(Long dentistId, PatientRequestDto patientRequestDto) {
        Dentist dentist = findDentistById(dentistId);

        if (patientRepository.existsByDni(patientRequestDto.getDni())) {
            throw new IllegalArgumentException("There is already a patient with the DNI: " + patientRequestDto.getDni());
        }

        if (patientRequestDto.getEmail() != null && !patientRequestDto.getEmail().trim().isEmpty()) {
            if (patientRepository.existsByEmail(patientRequestDto.getEmail())) {
                throw new IllegalArgumentException("There is already a patient with the email: " + patientRequestDto.getEmail());
            }
        }

        Patient patient = modelMapperUtils.map(patientRequestDto, Patient.class);
        patient.setId(null);
        patient.setDentist(dentist);
        patient.setActive(true);

        Patient savedPatient = patientRepository.save(patient);
        return modelMapperUtils.map(savedPatient, PatientResponseDto.class);
    }
    
    /**
     * Finds a dentist by ID or throws an exception if not found
     */
    @Override
    public Dentist findDentistById(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));
    }
    
    /**
     * Finds a dentist by ID with patients or throws an exception if not found
     */
    private Dentist findDentistByIdWithPatients(Long id) {
        return dentistRepository.findByIdWithPatients(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));
    }
    
    /**
     * Finds a dentist by ID with active patients or throws an exception if not found
     */
    private Dentist findDentistByIdWithActivePatients(Long id) {
        return dentistRepository.findByIdWithActivePatients(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));
    }
    
    /**
     * Validates that a license number is unique
     */
    private void validateLicenseNumber(String licenseNumber) {
        if (existsByLicense(licenseNumber)) {
            throw new IllegalArgumentException("There is already a dentist with the license: " + licenseNumber);
        }
    }
    
    /**
     * Validates email if present and not empty
     */
    private void validateEmailIfPresent(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (existsByEmail(email)) {
                throw new IllegalArgumentException("There is already a dentist with the email: " + email);
            }
        }
    }
    
    /**
     * Validates license number for update (only if different from existing)
     */
    private void validateLicenseNumberForUpdate(String existingLicense, String newLicense) {
        if (!existingLicense.equals(newLicense)) {
            validateLicenseNumber(newLicense);
        }
    }
    
    /**
     * Validates email for update (only if different from existing and not empty)
     */
    private void validateEmailForUpdate(String existingEmail, String newEmail) {
        if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(existingEmail)) {
            if (existsByEmail(newEmail)) {
                throw new IllegalArgumentException("There is already a dentist with the email: " + newEmail);
            }
        }
    }
    
    /**
     * Maps a patient entity to PatientSummaryDto
     */
    private DentistPatientsResponseDto.PatientSummaryDto mapPatientToSummaryDto(Patient patient) {
        DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
        patientDto.setId(patient.getId());
        patientDto.setFirstName(patient.getFirstName());
        patientDto.setLastName(patient.getLastName());
        patientDto.setDni(patient.getDni());
        patientDto.setEmail(patient.getEmail());
        patientDto.setPhone(patient.getPhone());
        patientDto.setActive(patient.getActive());
        return patientDto;
    }
    
    /**
     * Builds DentistPatientsResponseDto from dentist and optionally filters for active patients only
     */
    private DentistPatientsResponseDto buildDentistPatientsResponse(Dentist dentist, boolean activeOnly) {
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentist.getFullName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = dentist.getPatients().stream()
                .filter(patient -> !activeOnly || Boolean.TRUE.equals(patient.getActive()))
                .map(this::mapPatientToSummaryDto)
                .collect(Collectors.toList());
        
        response.setPatients(patients);
        return response;
    }
}
