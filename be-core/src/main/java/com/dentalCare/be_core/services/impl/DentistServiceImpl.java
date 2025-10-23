package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.CreateDentistFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.response.AvailableUserDto;
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
    public List<AvailableUserDto> getAvailablePatientUsers() {
        List<UserDetailDto> patientUsers = userServiceClient.getUsersByRole("PATIENT");
        List<Long> existingPatientUserIds = getExistingPatientUserIds();
        
        return patientUsers.stream()
                .filter(user -> user.getIsActive() && !existingPatientUserIds.contains(user.getUserId()))
                .map(this::mapToAvailableUserDto)
                .collect(Collectors.toList());
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
        patient.setBirthDate(birthDate);
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

    private List<Long> getExistingPatientUserIds() {
        return patientRepository.findAll()
                .stream()
                .map(Patient::getUserId)
                .collect(Collectors.toList());
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
        dto.setBirthDate(patient.getBirthDate().toString());
        dto.setActive(patient.getActive());
        return dto;
    }

    private AvailableUserDto mapToAvailableUserDto(UserDetailDto user) {
        AvailableUserDto dto = new AvailableUserDto();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPicture(user.getPicture());
        dto.setRole(user.getRole());
        dto.setActive(user.getIsActive());
        return dto;
    }
}