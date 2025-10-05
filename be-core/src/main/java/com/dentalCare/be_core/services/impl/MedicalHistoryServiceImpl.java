package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.MedicalHistory;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.entities.Treatment;
import com.dentalCare.be_core.repositories.MedicalHistoryRepository;
import com.dentalCare.be_core.services.DentistService;
import com.dentalCare.be_core.services.FileStorageService;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.PatientService;
import com.dentalCare.be_core.services.PrescriptionService;
import com.dentalCare.be_core.services.TreatmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private DentistService dentistService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private FileStorageService fileStorageService;


    @Override
    public MedicalHistoryResponseDto createMedicalHistoryEntry(Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file) {
        Dentist dentist = dentistService.findDentistById(dentistId);
        Patient patient = patientService.validatePatientOwnershipAndActive(requestDto.getPatientId(), dentistId);

        MedicalHistory medicalHistory = new MedicalHistory();
        medicalHistory.setPatient(patient);
        medicalHistory.setDentist(dentist);
        medicalHistory.setEntryDate(requestDto.getEntryDate());
        medicalHistory.setDescription(requestDto.getDescription());
        medicalHistory.setActive(true);

        // Handle prescription if provided
        if (requestDto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionService.getPrescriptionEntityById(requestDto.getPrescriptionId());
            medicalHistory.setPrescription(prescription);
        }

        // Handle treatment if provided
        if (requestDto.getTreatmentId() != null) {
            Treatment treatment = treatmentService.getTreatmentEntityById(requestDto.getTreatmentId());
            medicalHistory.setTreatment(treatment);
            // Update treatment progress
            treatmentService.incrementTreatmentSessions(requestDto.getTreatmentId());
        }

        MedicalHistory savedEntry = medicalHistoryRepository.save(medicalHistory);

        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            savedEntry = handleFileUpload(savedEntry, file, patient.getId());
        }

        return mapToResponseDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> getMedicalHistoryByPatient(Long patientId) {
        return medicalHistoryRepository.findByPatientIdAndActiveTrue(patientId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> getMedicalHistoryByDentistAndPatient(Long dentistId, Long patientId) {
        return medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrue(dentistId, patientId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDto getMedicalHistoryEntryById(Long entryId, Long dentistId) {
        MedicalHistory entry = findMedicalHistoryEntryByIdAndDentist(entryId, dentistId);
        return mapToResponseDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDto getMedicalHistoryEntryByIdForPatient(Long entryId, Long patientId) {
        MedicalHistory entry = findMedicalHistoryEntryByIdAndPatient(entryId, patientId);
        return mapToResponseDto(entry);
    }

    @Override
    public MedicalHistoryResponseDto updateMedicalHistoryEntry(Long entryId, Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file) {
        MedicalHistory entry = findMedicalHistoryEntryByIdAndDentist(entryId, dentistId);
        Patient patient = patientService.validatePatientOwnershipAndActive(requestDto.getPatientId(), dentistId);

        entry.setPatient(patient);
        entry.setEntryDate(requestDto.getEntryDate());
        entry.setDescription(requestDto.getDescription());

        // Handle prescription
        if (requestDto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionService.getPrescriptionEntityById(requestDto.getPrescriptionId());
            entry.setPrescription(prescription);
        } else {
            entry.setPrescription(null);
        }

        // Handle treatment
        if (requestDto.getTreatmentId() != null) {
            Treatment treatment = treatmentService.getTreatmentEntityById(requestDto.getTreatmentId());
            entry.setTreatment(treatment);
        } else {
            entry.setTreatment(null);
        }

        // Handle file replacement if provided
        if (file != null && !file.isEmpty()) {
            entry = handleFileReplacement(entry, file, patient.getId());
        }

        MedicalHistory updatedEntry = medicalHistoryRepository.save(entry);
        return mapToResponseDto(updatedEntry);
    }

    @Override
    public void deleteMedicalHistoryEntry(Long entryId, Long dentistId) {
        MedicalHistory entry = findMedicalHistoryEntryByIdAndDentist(entryId, dentistId);
        
        // Mark as inactive
        entry.setActive(false);
        
        // Delete file if exists
        handleFileDeletion(entry);
        
        medicalHistoryRepository.save(entry);
    }

    private MedicalHistoryResponseDto mapToResponseDto(MedicalHistory entry) {
        MedicalHistoryResponseDto dto = new MedicalHistoryResponseDto();
        dto.setId(entry.getId());
        dto.setPatientId(entry.getPatient().getId());
        dto.setPatientName(entry.getPatient().getFirstName() + " " + entry.getPatient().getLastName());
        dto.setPatientDni(entry.getPatient().getDni());
        dto.setDentistId(entry.getDentist().getId());
        dto.setDentistName(entry.getDentist().getFirstName() + " " + entry.getDentist().getLastName());
        dto.setDentistLicenseNumber(entry.getDentist().getLicenseNumber());
        dto.setEntryDate(entry.getEntryDate());
        dto.setDescription(entry.getDescription());

        if (entry.getPrescription() != null) {
            dto.setPrescriptionId(entry.getPrescription().getId());
            dto.setPrescriptionSummary("Prescription dated " + entry.getPrescription().getPrescriptionDate());
        }

        if (entry.getTreatment() != null) {
            dto.setTreatmentId(entry.getTreatment().getId());
            dto.setTreatmentName(entry.getTreatment().getName());
        }

        dto.setHasFile(entry.getFileUrl() != null);
        dto.setFileUrl(entry.getFileUrl());
        dto.setFileName(entry.getFileName());
        dto.setFileType(entry.getFileType());
        dto.setActive(entry.getActive());

        return dto;
    }

    
    /**
     * Finds a medical history entry by ID and dentist ID
     */
    private MedicalHistory findMedicalHistoryEntryByIdAndDentist(Long entryId, Long dentistId) {
        return medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));
    }
    
    /**
     * Finds a medical history entry by ID and patient ID
     */
    private MedicalHistory findMedicalHistoryEntryByIdAndPatient(Long entryId, Long patientId) {
        return medicalHistoryRepository.findByIdAndPatientIdAndActiveTrue(entryId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));
    }
    
    /**
     * Handles file upload for medical history entry
     */
    private MedicalHistory handleFileUpload(MedicalHistory entry, MultipartFile file, Long patientId) {
        String fileUrl = fileStorageService.storeFile(file, patientId, entry.getId());
        entry.setFileUrl(fileUrl);
        entry.setFileName(file.getOriginalFilename());
        entry.setFileType(file.getContentType());
        return medicalHistoryRepository.save(entry);
    }
    
    /**
     * Handles file replacement for medical history entry
     */
    private MedicalHistory handleFileReplacement(MedicalHistory entry, MultipartFile file, Long patientId) {
        if (entry.getFileUrl() != null) {
            fileStorageService.deleteFile(entry.getFileUrl());
        }
        String fileUrl = fileStorageService.storeFile(file, patientId, entry.getId());
        entry.setFileUrl(fileUrl);
        entry.setFileName(file.getOriginalFilename());
        entry.setFileType(file.getContentType());
        return entry;
    }
    
    /**
     * Handles file deletion for medical history entry
     */
    private void handleFileDeletion(MedicalHistory entry) {
        if (entry.getFileUrl() != null) {
            fileStorageService.deleteFile(entry.getFileUrl());
            entry.setFileUrl(null);
            entry.setFileName(null);
            entry.setFileType(null);
        }
    }
}
