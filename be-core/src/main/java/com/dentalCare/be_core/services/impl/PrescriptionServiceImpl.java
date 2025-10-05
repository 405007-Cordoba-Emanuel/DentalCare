package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.request.prescription.PrescriptionRequestDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.PrescriptionRepository;
import com.dentalCare.be_core.services.PrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ModelMapperUtils modelMapperUtils;

    @Override
    public PrescriptionResponseDto createPrescriptionForDentist(Long dentistId, PrescriptionRequestDto prescriptionRequestDto) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));

        Patient patient = patientRepository.findById(prescriptionRequestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + prescriptionRequestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        // Validar que el paciente esté activo
        if (patient.getActive() == null || !patient.getActive()) {
            throw new IllegalArgumentException("The patient is not active");
        }

        Prescription prescription = modelMapperUtils.map(prescriptionRequestDto, Prescription.class);
        prescription.setId(null);
        prescription.setDentist(dentist);
        prescription.setPatient(patient);
        prescription.setActive(true);

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        return mapToResponseDto(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionsByDentistId(Long dentistId) {
        List<Prescription> prescriptions = prescriptionRepository.findByDentistIdAndActiveTrue(dentistId);
        return prescriptions.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionsByDentistIdAndPatientId(Long dentistId, Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findByDentistIdAndPatientIdAndActiveTrue(dentistId, patientId);
        return prescriptions.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionByIdAndDentistId(Long prescriptionId, Long dentistId) {
        Prescription prescription = prescriptionRepository.findByIdAndDentistIdAndActiveTrue(prescriptionId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId + " for dentist: " + dentistId));
        return mapToResponseDto(prescription);
    }

    @Override
    public PrescriptionResponseDto updatePrescription(Long prescriptionId, Long dentistId, PrescriptionRequestDto prescriptionRequestDto) {
        Prescription prescription = prescriptionRepository.findByIdAndDentistIdAndActiveTrue(prescriptionId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId + " for dentist: " + dentistId));

        Patient patient = patientRepository.findById(prescriptionRequestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + prescriptionRequestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        prescription.setPatient(patient);
        prescription.setPrescriptionDate(prescriptionRequestDto.getPrescriptionDate());
        prescription.setObservations(prescriptionRequestDto.getObservations());
        prescription.setMedications(prescriptionRequestDto.getMedications());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        return mapToResponseDto(updatedPrescription);
    }

    @Override
    public void deletePrescription(Long prescriptionId, Long dentistId) {
        Prescription prescription = prescriptionRepository.findByIdAndDentistIdAndActiveTrue(prescriptionId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId + " for dentist: " + dentistId));
        prescriptionRepository.delete(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPrescriptionsByPatientId(Long patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndActiveTrue(patientId);
        return prescriptions.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionByIdAndPatientId(Long prescriptionId, Long patientId) {
        Prescription prescription = prescriptionRepository.findByIdAndPatientIdAndActiveTrue(prescriptionId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId + " for patient: " + patientId));
        return mapToResponseDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPrescriptionsByDentistId(Long dentistId) {
        return prescriptionRepository.countByDentistIdAndActiveTrue(dentistId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.countByPatientIdAndActiveTrue(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Prescription getPrescriptionEntityById(Long prescriptionId, Long dentistId) {
        return prescriptionRepository.findByIdAndDentistIdAndActiveTrue(prescriptionId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId));
    }

    private PrescriptionResponseDto mapToResponseDto(Prescription prescription) {
        PrescriptionResponseDto responseDto = modelMapperUtils.map(prescription, PrescriptionResponseDto.class);
        
        responseDto.setPatientId(prescription.getPatient().getId());
        responseDto.setPatientName(prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName());
        responseDto.setPatientDni(prescription.getPatient().getDni());
        responseDto.setDentistId(prescription.getDentist().getId());
        responseDto.setDentistName(prescription.getDentist().getFirstName() + " " + prescription.getDentist().getLastName());
        responseDto.setDentistLicenseNumber(prescription.getDentist().getLicenseNumber());
        responseDto.setDentistSpecialty(prescription.getDentist().getSpecialty());
        responseDto.setLastUpdatedDatetime(LocalDateTime.now());
        
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Prescription getPrescriptionEntityById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + prescriptionId));
    }
}
