package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
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
import com.dentalCare.be_core.services.UserServiceClient;
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
    private UserServiceClient userServiceClient;

    @Override
    public DentistResponseDto createDentist(DentistRequestDto dentistRequestDto) {
        if (existsByLicense(dentistRequestDto.getLicenseNumber())) {
            throw new IllegalArgumentException("There is already a dentist with the license: "
                    + dentistRequestDto.getLicenseNumber());
        }

        UserDetailDto user = userServiceClient.getUserById(dentistRequestDto.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + dentistRequestDto.getUserId());
        }

        Dentist dentist = new Dentist();
        dentist.setUserId(dentistRequestDto.getUserId());
        dentist.setLicenseNumber(dentistRequestDto.getLicenseNumber());
        dentist.setSpecialty(dentistRequestDto.getSpecialty());
        dentist.setActive(true);

        Dentist savedDentist = dentistRepository.save(dentist);
        return mapToResponseDto(savedDentist, user);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchById(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: "+ id));
        
        UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
        return mapToResponseDto(dentist, user);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchByLicenseNumber(String licenseNumber) {
        Dentist dentist = dentistRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new IllegalArgumentException("No licensed dentist found:" + licenseNumber));

        UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
        return mapToResponseDto(dentist, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> findAllActive() {
        List<Dentist> dentists = dentistRepository.findAllActive();
        return dentists.stream()
                .map(dentist -> {
                    UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
                    return mapToResponseDto(dentist, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> searchBySpecialty(String specialty) {
        List<Dentist> dentists = dentistRepository.findActiveBySpecialty(specialty);
        return dentists.stream()
                .map(dentist -> {
                    UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
                    return mapToResponseDto(dentist, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public DentistResponseDto updateDentist(Long id, DentistUpdateRequestDto dentistUpdateRequestDto) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));

        if (!dentist.getLicenseNumber().equals(dentistUpdateRequestDto.getLicenseNumber())) {
            if (existsByLicense(dentistUpdateRequestDto.getLicenseNumber())) {
                throw new IllegalArgumentException("There is already a dentist with the license: " + dentistUpdateRequestDto.getLicenseNumber());
            }
        }

        dentist.setLicenseNumber(dentistUpdateRequestDto.getLicenseNumber());
        dentist.setSpecialty(dentistUpdateRequestDto.getSpecialty());
        dentist.setActive(dentistUpdateRequestDto.getActive());

        Dentist updatedDentist = dentistRepository.save(dentist);
        UserDetailDto user = userServiceClient.getUserById(updatedDentist.getUserId());
        return mapToResponseDto(updatedDentist, user);
    }

    @Override
    public void deleteDentist(Long id) {
        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));
        dentistRepository.delete(dentist);
    }

    @Override
    public boolean existsByLicense(String licenseNumber) {
        return dentistRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDentist() {
        return dentistRepository.countActiveDentist();
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getPatientsByDentistId(Long dentistId) {
        Dentist dentist = dentistRepository.findByIdWithPatients(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));
        
        UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
        
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = dentist.getPatients().stream()
                .map(patient -> {
                    UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
                    DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
                    patientDto.setId(patient.getId());
                    patientDto.setFirstName(patientUser.getFirstName());
                    patientDto.setLastName(patientUser.getLastName());
                    patientDto.setDni(patient.getDni());
                    patientDto.setEmail(patientUser.getEmail());
                    patientDto.setPhone(patientUser.getPhone());
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
        Dentist dentist = dentistRepository.findByIdWithActivePatients(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));
        
        UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
        
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = dentist.getPatients().stream()
                .filter(patient -> Boolean.TRUE.equals(patient.getActive()))
                .map(patient -> {
                    UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
                    DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
                    patientDto.setId(patient.getId());
                    patientDto.setFirstName(patientUser.getFirstName());
                    patientDto.setLastName(patientUser.getLastName());
                    patientDto.setDni(patient.getDni());
                    patientDto.setEmail(patientUser.getEmail());
                    patientDto.setPhone(patientUser.getPhone());
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

        UserDetailDto user = userServiceClient.getUserById(patientRequestDto.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + patientRequestDto.getUserId());
        }

        if (patientRepository.existsByDni(patientRequestDto.getDni())) {
            throw new IllegalArgumentException("There is already a patient with the DNI: " + patientRequestDto.getDni());
        }

        Patient patient = new Patient();
        patient.setUserId(patientRequestDto.getUserId());
        patient.setDni(patientRequestDto.getDni());
        patient.setBirthDate(patientRequestDto.getBirthDate());
        patient.setDentist(dentist);
        patient.setActive(true);

        Patient savedPatient = patientRepository.save(patient);
        return mapPatientToResponseDto(savedPatient, user);
    }

    private DentistResponseDto mapToResponseDto(Dentist dentist, UserDetailDto user) {
        DentistResponseDto dto = new DentistResponseDto();
        dto.setId(dentist.getId());
        dto.setUserId(dentist.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setLicenseNumber(dentist.getLicenseNumber());
        dto.setSpecialty(dentist.getSpecialty());
        dto.setActive(dentist.getActive());
        return dto;
    }

    private PatientResponseDto mapPatientToResponseDto(Patient patient, UserDetailDto user) {
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setDni(patient.getDni());
        dto.setBirthDate(patient.getBirthDate().toString());
        dto.setActive(patient.getActive());
        return dto;
    }
}