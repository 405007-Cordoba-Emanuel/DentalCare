package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.PatientService;
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
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: "+ id));

        UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
        return mapToResponseDto(patient, user);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchByDni(String dni) {
        Patient patient = patientRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("No dni patient found:" + dni));

        UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
        return mapToResponseDto(patient, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDto> findAllActive() {
        List<Patient> patients = patientRepository.findAllActive();
        return patients.stream()
                .map(patient -> {
                    UserDetailDto user = userServiceClient.getUserById(patient.getUserId());
                    return mapToResponseDto(patient, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDto updatePatient(Long id, PatientUpdateRequestDto patientUpdateRequestDto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + id));

        if (!patient.getDni().equals(patientUpdateRequestDto.getDni())) {
            if (existsByDni(patientUpdateRequestDto.getDni())) {
                throw new IllegalArgumentException("There is already a patient with the dni: " + patientUpdateRequestDto.getDni());
            }
        }

        patient.setDni(patientUpdateRequestDto.getDni());
        patient.setBirthDate(patientUpdateRequestDto.getBirthDate());
        patient.setActive(patientUpdateRequestDto.getActive());

        Patient updatedPatient = patientRepository.save(patient);
        UserDetailDto user = userServiceClient.getUserById(updatedPatient.getUserId());
        return mapToResponseDto(updatedPatient, user);
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + id));
        patientRepository.delete(patient);
    }

    @Override
    public boolean existsByDni(String dni) {
        return patientRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActivePatient() {
        return patientRepository.countActivePatient();
    }

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