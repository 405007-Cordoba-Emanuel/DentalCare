package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.PatientService;
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
    private ModelMapperUtils modelMapperUtils;


    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: "+ id));

        return modelMapperUtils.map(patient, PatientResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto searchByDni(String dni) {
        Patient patient = patientRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("No dni patient found:" + dni));

        return modelMapperUtils.map(patient, PatientResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDto> findAllActive() {
        List<Patient> patients = patientRepository.findAllActive();
        return patients.stream()
                .map(patient -> modelMapperUtils.map(patient, PatientResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDto updatePatient(Long id, PatientUpdateRequestDto patientUpdateRequestDto) {
        Patient patientExisting = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + id));

        if (!patientExisting.getDni().equals(patientUpdateRequestDto.getDni())) {
            if (existsByDni(patientUpdateRequestDto.getDni())) {
                throw new IllegalArgumentException("There is already a patient with the dni: " + patientUpdateRequestDto.getDni());
            }
        }

        if (patientUpdateRequestDto.getEmail() != null && !patientUpdateRequestDto.getEmail().trim().isEmpty()) {
            if (!patientUpdateRequestDto.getEmail().equals(patientExisting.getEmail()) &&
                existsByEmail(patientUpdateRequestDto.getEmail())) {
                throw new IllegalArgumentException("There is already a patient with the email: " + patientUpdateRequestDto.getEmail());
            }
        }

        patientExisting.setFirstName(patientUpdateRequestDto.getFirstName());
        patientExisting.setLastName(patientUpdateRequestDto.getLastName());
        patientExisting.setDni(patientUpdateRequestDto.getDni());
        patientExisting.setBirthDate(patientUpdateRequestDto.getBirthDate());
        patientExisting.setPhone(patientUpdateRequestDto.getPhone());
        patientExisting.setEmail(patientUpdateRequestDto.getEmail());
        patientExisting.setAddress(patientUpdateRequestDto.getAddress());
        patientExisting.setActive(patientUpdateRequestDto.getActive());

        Patient patientUpdated = patientRepository.save(patientExisting);
        return modelMapperUtils.map(patientUpdated, PatientResponseDto.class);
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
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return patientRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActivePatient() {
        return patientRepository.countActivePatient();
    }

        @Override
    public Patient validatePatientOwnershipAndActive(Long patientId, Long dentistId) {
        // Get patient entity
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + patientId));

        // Validate patient ownership
        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        // Validate patient is active
        if (patient.getActive() == null || !patient.getActive()) {
            throw new IllegalArgumentException("The patient is not active");
        }
        
        return patient;
    }

    @Override
    public Patient getPatientEntityById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + patientId));
    }

}
