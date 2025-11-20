package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.CreateDentistFromUserRequest;
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
public class DentistServiceImpl implements DentistService {

    private final DentistRepository dentistRepository;
    private final PatientRepository patientRepository;
    private final UserServiceClient userServiceClient;

    // ==================== CREATE OPERATIONS ====================

    @Override
    public DentistResponseDto createDentist(DentistRequestDto dentistRequestDto) {
        validateLicenseNotExists(dentistRequestDto.getLicenseNumber());
        UserDetailDto user = validateAndGetUser(dentistRequestDto.getUserId());
        
        Dentist dentist = buildDentistEntity(dentistRequestDto.getUserId(), 
                                           dentistRequestDto.getLicenseNumber(), 
                                           dentistRequestDto.getSpecialty());
        
        Dentist savedDentist = dentistRepository.save(dentist);
        return mapToResponseDto(savedDentist, user);
    }

    @Override
    public DentistResponseDto createDentistFromUser(CreateDentistFromUserRequest request) {
        UserDetailDto user = validateAndGetUser(request.getUserId());
        validateDentistNotExistsForUser(request.getUserId());
        validateLicenseNotExists(request.getLicenseNumber());
        
        Dentist dentist = buildDentistEntity(request.getUserId(), 
                                           request.getLicenseNumber(), 
                                           request.getSpecialty());
        
        Dentist savedDentist = dentistRepository.save(dentist);
        return mapToResponseDto(savedDentist, user);
    }

    @Override
    public PatientResponseDto createPatientForDentist(Long dentistId, PatientRequestDto patientRequestDto) {
        Dentist dentist = findDentistById(dentistId);
        UserDetailDto user = validateAndGetUser(patientRequestDto.getUserId());
        validateDniNotExists(patientRequestDto.getDni());
        
        Patient patient = buildPatientEntity(patientRequestDto.getUserId(),
                                           patientRequestDto.getDni(),
                                           patientRequestDto.getBirthDate(),
                                           dentist);
        
        Patient savedPatient = patientRepository.save(patient);
        return mapPatientToResponseDto(savedPatient, user);
    }

    // ==================== READ OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchById(Long id) {
        Dentist dentist = findDentistById(id);
        UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
        return mapToResponseDto(dentist, user);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponseDto searchByLicenseNumber(String licenseNumber) {
        Dentist dentist = findDentistByLicenseNumber(licenseNumber);
        UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
        return mapToResponseDto(dentist, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> findAllActive() {
        List<Dentist> dentists = dentistRepository.findAllActive();
        return mapDentistsToResponseDtos(dentists);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponseDto> searchBySpecialty(String specialty) {
        List<Dentist> dentists = dentistRepository.findActiveBySpecialty(specialty);
        return mapDentistsToResponseDtos(dentists);
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getPatientsByDentistId(Long dentistId) {
        Dentist dentist = findDentistByIdWithPatients(dentistId);
        UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
        
        DentistPatientsResponseDto response = buildDentistPatientsResponse(dentist, dentistUser);
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = mapPatientsToSummaryDtos(dentist.getPatients());
        response.setPatients(patients);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DentistPatientsResponseDto getActivePatientsByDentistId(Long dentistId) {
        Dentist dentist = findDentistByIdWithActivePatients(dentistId);
        UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
        
        DentistPatientsResponseDto response = buildDentistPatientsResponse(dentist, dentistUser);
        List<DentistPatientsResponseDto.PatientSummaryDto> patients = mapPatientsToSummaryDtos(
            dentist.getPatients().stream()
                .filter(patient -> Boolean.TRUE.equals(patient.getActive()))
                .collect(Collectors.toList())
        );
        response.setPatients(patients);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDto> getAvailablePatientUsers() {
        // Buscar pacientes que existen en la tabla patients pero NO tienen dentista asignado (dentist_id IS NULL)
        List<Patient> patientsWithoutDentist = patientRepository.findAvailablePatients();
        
        // Mapear a PatientResponseDto con información completa del usuario y paciente
        return patientsWithoutDentist.stream()
                .map(patient -> {
                    UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
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
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.dentalCare.be_core.dtos.response.PagedResponse<PatientResponseDto> getAvailablePatientUsersPaged(
            int page, int size, String sortBy, String sortDirection) {
        
        // Validar y ajustar parámetros de paginación
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10; // Máximo 100 elementos por página
        
        // Crear Sort basado en sortBy y sortDirection
        org.springframework.data.domain.Sort.Direction direction = 
            "desc".equalsIgnoreCase(sortDirection) ? 
                org.springframework.data.domain.Sort.Direction.DESC : 
                org.springframework.data.domain.Sort.Direction.ASC;
        
        // Validar campo de ordenamiento
        String validSortBy = "lastName"; // Por defecto
        if (sortBy != null && !sortBy.isEmpty()) {
            // Permitir solo campos válidos de la entidad Patient
            if (sortBy.equals("dni") || sortBy.equals("active") || sortBy.equals("id")) {
                validSortBy = sortBy;
            } else {
                validSortBy = "id"; // Por defecto si no es válido
            }
        }
        
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(direction, validSortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        // Obtener página de pacientes disponibles
        org.springframework.data.domain.Page<Patient> patientPage = patientRepository.findAvailablePatients(pageable);
        
        // Mapear a PatientResponseDto con información completa del usuario y paciente
        List<PatientResponseDto> patientDtos = patientPage.getContent().stream()
                .map(patient -> {
                    UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
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
                })
                .collect(Collectors.toList());
        
        // Aplicar ordenamiento por firstName y lastName en memoria si es necesario
        // (ya que estos campos vienen del microservicio de usuarios)
        if (sortBy != null && (sortBy.equals("firstName") || sortBy.equals("lastName") || sortBy.equals("email"))) {
            java.util.Comparator<PatientResponseDto> comparator = null;
            
            if (sortBy.equals("firstName")) {
                comparator = java.util.Comparator.comparing(PatientResponseDto::getFirstName, String.CASE_INSENSITIVE_ORDER);
            } else if (sortBy.equals("lastName")) {
                comparator = java.util.Comparator.comparing(PatientResponseDto::getLastName, String.CASE_INSENSITIVE_ORDER);
            } else if (sortBy.equals("email")) {
                comparator = java.util.Comparator.comparing(PatientResponseDto::getEmail, String.CASE_INSENSITIVE_ORDER);
            }
            
            if (comparator != null) {
                if (direction == org.springframework.data.domain.Sort.Direction.DESC) {
                    comparator = comparator.reversed();
                }
                patientDtos.sort(comparator);
            }
        }
        
        // Crear respuesta paginada
        com.dentalCare.be_core.dtos.response.PagedResponse<PatientResponseDto> response = 
            new com.dentalCare.be_core.dtos.response.PagedResponse<>();
        response.setContent(patientDtos);
        response.setPageNumber(patientPage.getNumber());
        response.setPageSize(patientPage.getSize());
        response.setTotalElements(patientPage.getTotalElements());
        response.setTotalPages(patientPage.getTotalPages());
        response.setFirst(patientPage.isFirst());
        response.setLast(patientPage.isLast());
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDentistIdByUserId(Long userId) {
        return dentistRepository.findByUserId(userId)
                .map(Dentist::getId)
                .orElse(null);
    }

    // ==================== UPDATE OPERATIONS ====================

    @Override
    public DentistResponseDto updateDentist(Long id, DentistUpdateRequestDto dentistUpdateRequestDto) {
        Dentist dentist = findDentistById(id);
        validateLicenseUpdate(dentist, dentistUpdateRequestDto.getLicenseNumber());
        
        updateDentistFields(dentist, dentistUpdateRequestDto);
        Dentist updatedDentist = dentistRepository.save(dentist);
        
        UserDetailDto user = userServiceClient.getUserById(updatedDentist.getUserId());
        return mapToResponseDto(updatedDentist, user);
    }

    // ==================== DELETE OPERATIONS ====================

    @Override
    public void deleteDentist(Long id) {
        Dentist dentist = findDentistById(id);
        dentistRepository.delete(dentist);
    }

    // ==================== UTILITY OPERATIONS ====================

    @Override
    public boolean existsByLicense(String licenseNumber) {
        return dentistRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return false; // Implementación pendiente si es necesaria
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDentist() {
        return dentistRepository.countActiveDentist();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Dentist findDentistById(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + id));
    }

    private Dentist findDentistByLicenseNumber(String licenseNumber) {
        return dentistRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new IllegalArgumentException("No licensed dentist found: " + licenseNumber));
    }

    private Dentist findDentistByIdWithPatients(Long dentistId) {
        return dentistRepository.findByIdWithPatients(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));
    }

    private Dentist findDentistByIdWithActivePatients(Long dentistId) {
        return dentistRepository.findByIdWithActivePatients(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));
    }

    private UserDetailDto validateAndGetUser(Long userId) {
        UserDetailDto user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        return user;
    }

    private void validateLicenseNotExists(String licenseNumber) {
        if (existsByLicense(licenseNumber)) {
            throw new IllegalArgumentException("There is already a dentist with the license: " + licenseNumber);
        }
    }

    private void validateDentistNotExistsForUser(Long userId) {
        if (dentistRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Dentist already exists for user ID: " + userId);
        }
    }

    private void validateDniNotExists(String dni) {
        if (patientRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("There is already a patient with the DNI: " + dni);
        }
    }

    private void validateLicenseUpdate(Dentist dentist, String newLicenseNumber) {
        if (!dentist.getLicenseNumber().equals(newLicenseNumber)) {
            validateLicenseNotExists(newLicenseNumber);
        }
    }

    private Dentist buildDentistEntity(Long userId, String licenseNumber, String specialty) {
        Dentist dentist = new Dentist();
        dentist.setUserId(userId);
        dentist.setLicenseNumber(licenseNumber);
        dentist.setSpecialty(specialty);
        dentist.setActive(true);
        return dentist;
    }

    private Patient buildPatientEntity(Long userId, String dni, java.time.LocalDate birthDate, Dentist dentist) {
        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setDni(dni);
        patient.setDentist(dentist);
        patient.setActive(true);
        return patient;
    }

    private void updateDentistFields(Dentist dentist, DentistUpdateRequestDto updateDto) {
        dentist.setLicenseNumber(updateDto.getLicenseNumber());
        dentist.setSpecialty(updateDto.getSpecialty());
        dentist.setActive(updateDto.getActive());
    }

    private List<DentistResponseDto> mapDentistsToResponseDtos(List<Dentist> dentists) {
        return dentists.stream()
                .map(dentist -> {
                    UserDetailDto user = userServiceClient.getUserById(dentist.getUserId());
                    return mapToResponseDto(dentist, user);
                })
                .collect(Collectors.toList());
    }

    private List<DentistPatientsResponseDto.PatientSummaryDto> mapPatientsToSummaryDtos(List<Patient> patients) {
        return patients.stream()
                .map(patient -> {
                    UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
                    return buildPatientSummaryDto(patient, patientUser);
                })
                .collect(Collectors.toList());
    }

    private DentistPatientsResponseDto buildDentistPatientsResponse(Dentist dentist, UserDetailDto dentistUser) {
        DentistPatientsResponseDto response = new DentistPatientsResponseDto();
        response.setDentistId(dentist.getId());
        response.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        response.setLicenseNumber(dentist.getLicenseNumber());
        response.setSpecialty(dentist.getSpecialty());
        return response;
    }

    private DentistPatientsResponseDto.PatientSummaryDto buildPatientSummaryDto(Patient patient, UserDetailDto patientUser) {
        DentistPatientsResponseDto.PatientSummaryDto patientDto = new DentistPatientsResponseDto.PatientSummaryDto();
        patientDto.setId(patient.getId());
        patientDto.setFirstName(patientUser.getFirstName());
        patientDto.setLastName(patientUser.getLastName());
        patientDto.setDni(patient.getDni());
        patientDto.setEmail(patientUser.getEmail());
        patientDto.setPhone(patientUser.getPhone());
        patientDto.setActive(patient.getActive());
        return patientDto;
    }

    // ==================== MAPPING METHODS ====================

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
        dto.setActive(patient.getActive());
        return dto;
    }

}