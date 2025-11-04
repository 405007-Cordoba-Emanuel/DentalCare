package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.patient.CreatePatientFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.DentistRepository;
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
    private final DentistRepository dentistRepository;
    private final UserServiceClient userServiceClient;

    // ==================== CREATE OPERATIONS ====================

    @Override
    public PatientResponseDto createPatientFromUser(CreatePatientFromUserRequest request) {
        UserDetailDto user = validateAndGetUser(request.getUserId());
        validatePatientNotExistsForUser(request.getUserId());
        validateDniNotExists(request.getDni());
        
        Patient patient = buildPatientEntity(request.getUserId(),
                                           request.getDni());
        
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
        try {
            log.info("=== INICIO UPDATE PATIENT ===");
            log.info("Actualizando paciente con ID: {}", id);
            log.info("Request DTO - DNI: '{}', Active: {}", patientUpdateRequestDto.getDni(), patientUpdateRequestDto.getActive());
            
            Patient patient = findPatientById(id);
            log.info("Paciente encontrado - ID: {}, DNI actual: '{}', Active: {}", 
                    patient.getId(), patient.getDni(), patient.getActive());
            
            // Solo validar DNI si se está cambiando a un valor diferente y tiene contenido
            if (patientUpdateRequestDto.getDni() != null && 
                !patientUpdateRequestDto.getDni().trim().isEmpty() && 
                !patient.getDni().equals(patientUpdateRequestDto.getDni().trim())) {
                log.info("Validando cambio de DNI de '{}' a '{}'", patient.getDni(), patientUpdateRequestDto.getDni());
                validateDniUpdate(patient, patientUpdateRequestDto.getDni().trim());
            } else {
                log.info("DNI no se está cambiando o viene vacío");
            }
            
            updatePatientFields(patient, patientUpdateRequestDto);
            
            log.info("Antes de save - DNI: '{}', Active: {}", patient.getDni(), patient.getActive());
            Patient updatedPatient = patientRepository.save(patient);
            log.info("Después de save - DNI: '{}', Active: {}", updatedPatient.getDni(), updatedPatient.getActive());
            
            // Forzar flush para asegurar que se guarde inmediatamente
            patientRepository.flush();
            log.info("Flush completado - DNI: '{}', Active: {}", updatedPatient.getDni(), updatedPatient.getActive());
            
            // Verificar que el DNI se guardó correctamente
            Patient verifyPatient = patientRepository.findById(updatedPatient.getId()).orElse(null);
            if (verifyPatient != null) {
                log.info("DNI verificado en BD: '{}'", verifyPatient.getDni());
            }
            
            UserDetailDto user = userServiceClient.getUserById(updatedPatient.getUserId());
            PatientResponseDto response = mapToResponseDto(updatedPatient, user);
            log.info("=== FIN UPDATE PATIENT EXITOSO ===");
            return response;
        } catch (Exception e) {
            log.error("=== ERROR EN UPDATE PATIENT ===", e);
            throw e;
        }
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

    private Patient buildPatientEntity(Long userId, String dni) {
        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setDni(dni);
        patient.setDentist(null); // Sin dentista asignado (aparece en lista de disponibles)
        patient.setActive(true);
        return patient;
    }

    private void updatePatientFields(Patient patient, PatientUpdateRequestDto updateDto) {
        log.info("Actualizando campos del paciente - DNI recibido: '{}', Active: {}", 
                updateDto.getDni(), updateDto.getActive());
        
        // Actualizar DNI siempre si viene un valor válido
        if (updateDto.getDni() != null) {
            String dniTrimmed = updateDto.getDni().trim();
            if (!dniTrimmed.isEmpty()) {
                String oldDni = patient.getDni();
                patient.setDni(dniTrimmed);
                log.info("DNI actualizado de '{}' a '{}'", oldDni, patient.getDni());
            } else {
                log.warn("DNI recibido vacío después de trim, manteniendo DNI actual: {}", patient.getDni());
            }
        } else {
            log.warn("DNI es null, manteniendo DNI actual: {}", patient.getDni());
        }
        
        // Solo actualizar active si no es null
        if (updateDto.getActive() != null) {
            patient.setActive(updateDto.getActive());
        }
        
        log.info("Paciente después de updatePatientFields - DNI: {}, Active: {}", 
                patient.getDni(), patient.getActive());
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
        dto.setActive(patient.getActive());
        return dto;
    }

    // ==================== ASSIGN PATIENT TO DENTIST ====================

    @Override
    public PatientResponseDto assignDentistToPatient(Long patientId, Long dentistId) {
        log.info("Asignando dentista {} al paciente {}", dentistId, patientId);
        
        // Validar que el paciente existe
        Patient patient = findPatientById(patientId);
        
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Dentista con ID " + dentistId + " no encontrado"));
        
        // Verificar que el paciente no tenga ya un dentista asignado
        if (patient.getDentist() != null && patient.getDentist().getId().equals(dentistId)) {
            log.warn("El paciente {} ya está asignado al dentista {}", patientId, dentistId);
            UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
            return mapToResponseDto(patient, user);
        }
        
        // Asignar dentista al paciente
        patient.setDentist(dentist);
        patient.setActive(true);
        
        Patient updatedPatient = patientRepository.save(patient);
        log.info("Paciente {} asignado exitosamente al dentista {}", patientId, dentistId);
        
        UserDetailDto user = userServiceClient.getUserById(updatedPatient.getUserId());
        return mapToResponseDto(updatedPatient, user);
    }
}