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
import java.util.Optional;


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
        if (existsByLicense(dentistRequestDto.getLicenseNumber())) {
            throw new IllegalArgumentException("There is already a dentist with the license: "
                    + dentistRequestDto.getLicenseNumber());
        }
        if (dentistRequestDto.getEmail() != null && !dentistRequestDto.getEmail().trim().isEmpty()) {
            if (existsByEmail(dentistRequestDto.getEmail())) {
                throw new IllegalArgumentException("There is already a dentist with the email: "
                        + dentistRequestDto.getEmail());
            }
        }

        // Mapear DTO a Entity
        Dentist dentist = modelMapperUtils.map(dentistRequestDto, Dentist.class);
        dentist.setActive(true);
        // Guardar en BD
        Dentist dentistGuardado = dentistRepository.save(dentist);
        // Mapear Entity a Response DTO
        DentistResponseDto responseDto = modelMapperUtils.map(dentistGuardado, DentistResponseDto.class);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchById(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("No dentist found with ID: "+ id);
                });

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
        Dentist dentistExisting = dentistRepository.findById(id)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("No dentist found with ID: " + id);
                });

        if (!dentistExisting.getLicenseNumber().equals(dentistUpdateRequestDto.getLicenseNumber())) {
            if (existsByLicense(dentistUpdateRequestDto.getLicenseNumber())) {
                throw new IllegalArgumentException("There is already a dentist with the license: " + dentistUpdateRequestDto.getLicenseNumber());
            }
        }

        if (dentistUpdateRequestDto.getEmail() != null && !dentistUpdateRequestDto.getEmail().trim().isEmpty()) {
            if (!dentistUpdateRequestDto.getEmail().equals(dentistExisting.getEmail()) &&
                existsByEmail(dentistUpdateRequestDto.getEmail())) {
                throw new IllegalArgumentException("There is already a dentist with the email: " + dentistUpdateRequestDto.getEmail());
            }
        }

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
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("No dentist found with ID: " + id);
                });
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
        Optional<Dentist> dentistOpt = dentistRepository.findByIdWithPatients(dentistId);
        if (dentistOpt.isEmpty()) {
            throw new IllegalArgumentException("No dentist found with ID: " + dentistId);
        }
        
        Dentist dentist = dentistOpt.get();
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentist.getFullName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = dentist.getPatients().stream()
                .map(patient -> {
                    DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
                    patientDto.setId(patient.getId());
                    patientDto.setFirstName(patient.getFirstName());
                    patientDto.setLastName(patient.getLastName());
                    patientDto.setDni(patient.getDni());
                    patientDto.setEmail(patient.getEmail());
                    patientDto.setPhone(patient.getPhone());
                    patientDto.setActive(patient.getActive());
                    return patientDto;
                })
                .collect(Collectors.toList());
        
        response.setPatients(patients);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getActivePatientsByDentistId(Long dentistId) {
        Optional<Dentist> dentistOpt = dentistRepository.findByIdWithActivePatients(dentistId);
        if (dentistOpt.isEmpty()) {
            throw new IllegalArgumentException("No dentist found with ID: " + dentistId);
        }
        
        Dentist dentist = dentistOpt.get();
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentist.getFullName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = dentist.getPatients().stream()
                .filter(patient -> Boolean.TRUE.equals(patient.getActive()))
                .map(patient -> {
                    DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
                    patientDto.setId(patient.getId());
                    patientDto.setFirstName(patient.getFirstName());
                    patientDto.setLastName(patient.getLastName());
                    patientDto.setDni(patient.getDni());
                    patientDto.setEmail(patient.getEmail());
                    patientDto.setPhone(patient.getPhone());
                    patientDto.setActive(patient.getActive());
                    return patientDto;
                })
                .collect(Collectors.toList());
        
        response.setPatients(patients);
        return response;
    }

    @Override
    public PatientResponseDto createPatientForDentist(Long dentistId, PatientRequestDto patientRequestDto) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));

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

}
