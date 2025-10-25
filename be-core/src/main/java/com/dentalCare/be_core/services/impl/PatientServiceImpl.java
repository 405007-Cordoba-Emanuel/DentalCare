package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.patient.CreatePatientFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.PatientService;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserServiceClient userServiceClient;

    // ==================== CREATE OPERATIONS ====================

    @Override
    public PatientResponseDto createPatientFromUser(CreatePatientFromUserRequest request) {
        UserDetailDto user = validateAndGetUser(request.getUserId());
        validatePatientNotExistsForUser(request.getUserId());
        validateDniNotExists(request.getDni());
        
        Patient patient = buildPatientEntity(request.getUserId(),
                                           request.getDni(),
                                           request.getBirthDate());
        
        Patient savedPatient = patientRepository.save(patient);
        return mapToResponseDto(savedPatient, user);
    }

    // ==================== READ OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchById(Long id) {
        Patient patient = findPatientById(id);
        UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
        return mapToResponseDto(patient, user);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchByDni(String dni) {
        Patient patient = findPatientByDni(dni);
        UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
        return mapToResponseDto(patient, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDto> findAllActive() {
        List<Patient> patients = patientRepository.findAllActive();
        return mapPatientsToResponseDtos(patients);
    }

    // ==================== UPDATE OPERATIONS ====================

    @Override
    public PatientResponseDto updatePatient(Long id, PatientUpdateRequestDto patientUpdateRequestDto) {
        Patient patient = findPatientById(id);
        validateDniUpdate(patient, patientUpdateRequestDto.getDni());
        
        updatePatientFields(patient, patientUpdateRequestDto);
        Patient updatedPatient = patientRepository.save(patient);
        
        UserDetailDto user = userServiceClient.getUserById(updatedPatient.getUserId());
        return mapToResponseDto(updatedPatient, user);
    }

    // ==================== DELETE OPERATIONS ====================

    @Override
    public void deletePatient(Long id) {
        Patient patient = findPatientById(id);
        patientRepository.delete(patient);
    }

    // ==================== UTILITY OPERATIONS ====================

    @Override
    public boolean existsByDni(String dni) {
        return patientRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByEmail(String email) {
        return false; // Implementación pendiente si es necesaria
    }

    @Override
    @Transactional(readOnly = true)
    public long countActivePatient() {
        return patientRepository.countActivePatient();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPatientIdByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .map(Patient::getId)
                .orElse(null);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + id));
    }

    private Patient findPatientByDni(String dni) {
        return patientRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with DNI: " + dni));
    }

    private UserDetailDto validateAndGetUser(Long userId) {
        UserDetailDto user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        return user;
    }

    private void validatePatientNotExistsForUser(Long userId) {
        if (patientRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Patient already exists for user ID: " + userId);
        }
    }

    private void validateDniNotExists(String dni) {
        if (existsByDni(dni)) {
            throw new IllegalArgumentException("There is already a patient with the DNI: " + dni);
        }
    }

    private void validateDniUpdate(Patient patient, String newDni) {
        if (!patient.getDni().equals(newDni)) {
            validateDniNotExists(newDni);
        }
    }

    private Patient buildPatientEntity(Long userId, String dni, java.time.LocalDate birthDate) {
        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setDni(dni);
        patient.setBirthDate(birthDate);
        patient.setDentist(null); // Sin dentista asignado (aparece en lista de disponibles)
        patient.setActive(true);
        return patient;
    }

    private void updatePatientFields(Patient patient, PatientUpdateRequestDto updateDto) {
        patient.setDni(updateDto.getDni());
        patient.setBirthDate(updateDto.getBirthDate());
        patient.setActive(updateDto.getActive());
    }

    private List<PatientResponseDto> mapPatientsToResponseDtos(List<Patient> patients) {
        return patients.stream()
                .map(patient -> {
                    UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
                    return mapToResponseDto(patient, user);
                })
                .collect(Collectors.toList());
    }

    // ==================== MAPPING METHODS ====================

    private PatientResponseDto mapToResponseDto(Patient patient, UserDetailDto user) {
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