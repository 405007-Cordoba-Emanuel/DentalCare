package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.MedicalHistory;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.entities.Treatment;
import com.dentalCare.be_core.entities.TreatmentStatus;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.MedicalHistoryRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.PrescriptionRepository;
import com.dentalCare.be_core.repositories.TreatmentRepository;
import com.dentalCare.be_core.services.FileStorageService;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public MedicalHistoryResponseDto createMedicalHistoryEntry(Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));

        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + requestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        if (patient.getActive() == null || !patient.getActive()) {
            throw new IllegalArgumentException("The patient is not active");
        }

        MedicalHistory medicalHistory = new MedicalHistory();
        medicalHistory.setPatient(patient);
        medicalHistory.setDentist(dentist);
        // Entry date will be set automatically in @PrePersist, but we can also set it from DTO if provided
        if (requestDto.getEntryDate() != null) {
            medicalHistory.setEntryDate(requestDto.getEntryDate());
        }
        // Otherwise, it will be set to LocalDate.now() in @PrePersist
        medicalHistory.setDescription(requestDto.getDescription());
        medicalHistory.setActive(true);

        if (requestDto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.findById(requestDto.getPrescriptionId())
                    .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + requestDto.getPrescriptionId()));
            medicalHistory.setPrescription(prescription);
        }

        if (requestDto.getTreatmentId() != null) {
            Treatment treatment = treatmentRepository.findById(requestDto.getTreatmentId())
                    .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + requestDto.getTreatmentId()));
            medicalHistory.setTreatment(treatment);
            
            // Si el status es null, establecerlo a EN_CURSO (el tratamiento ya debería estar en curso cuando se agrega una sesión)
            if (treatment.getStatus() == null) {
                treatment.setStatus(TreatmentStatus.EN_CURSO);
            }
            treatment.setCompletedSessions(treatment.getCompletedSessions() + 1);
            treatmentRepository.save(treatment);
        }

        MedicalHistory savedEntry = medicalHistoryRepository.save(medicalHistory);

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.storeFile(file, patient.getId(), savedEntry.getId());
            savedEntry.setFileUrl(fileUrl);
            savedEntry.setFileName(file.getOriginalFilename());
            savedEntry.setFileType(file.getContentType());
            savedEntry = medicalHistoryRepository.save(savedEntry);
        }

        return mapToResponseDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> getMedicalHistoryByPatient(Long patientId) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByPatientIdAndActiveTrue(patientId);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> getMedicalHistoryByDentistAndPatient(Long dentistId, Long patientId) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrue(dentistId, patientId);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDto getMedicalHistoryEntryById(Long entryId, Long dentistId) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No clinical history entry found with ID: " + entryId));
        return mapToResponseDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDto getMedicalHistoryEntryByIdForPatient(Long entryId, Long patientId) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndPatientIdAndActiveTrue(entryId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No clinical history entry found with ID: " + entryId));
        return mapToResponseDto(entry);
    }

    @Override
    public MedicalHistoryResponseDto updateMedicalHistoryEntry(Long entryId, Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No clinical history entry found with ID: " + entryId));

        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + requestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        entry.setPatient(patient);
        // Entry date is not updatable - it's set once at creation
        // entry.setEntryDate(requestDto.getEntryDate()); // Removed - entry date cannot be changed
        entry.setDescription(requestDto.getDescription());

        if (requestDto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.findById(requestDto.getPrescriptionId())
                    .orElseThrow(() -> new IllegalArgumentException("No prescription found with ID: " + requestDto.getPrescriptionId()));
            entry.setPrescription(prescription);
        } else {
            entry.setPrescription(null);
        }

        if (requestDto.getTreatmentId() != null) {
            Treatment treatment = treatmentRepository.findById(requestDto.getTreatmentId())
                    .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + requestDto.getTreatmentId()));
            entry.setTreatment(treatment);
        } else {
            entry.setTreatment(null);
        }

        if (file != null && !file.isEmpty()) {
            if (entry.getFileUrl() != null) {
                fileStorageService.deleteFile(entry.getFileUrl());
            }
            String fileUrl = fileStorageService.storeFile(file, patient.getId(), entry.getId());
            entry.setFileUrl(fileUrl);
            entry.setFileName(file.getOriginalFilename());
            entry.setFileType(file.getContentType());
        }

        MedicalHistory updatedEntry = medicalHistoryRepository.save(entry);
        return mapToResponseDto(updatedEntry);
    }

    @Override
    public void deleteMedicalHistoryEntry(Long entryId, Long dentistId) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No clinical history entry found with ID: " + entryId));
        entry.setActive(false);
        if (entry.getFileUrl() != null) {
            fileStorageService.deleteFile(entry.getFileUrl());
        }
        medicalHistoryRepository.save(entry);
    }

    private MedicalHistoryResponseDto mapToResponseDto(MedicalHistory entry) {
        UserDetailDto patientUser = userServiceClient.getUserById(entry.getPatient().getUserId());
        UserDetailDto dentistUser = userServiceClient.getUserById(entry.getDentist().getUserId());
        
        MedicalHistoryResponseDto dto = new MedicalHistoryResponseDto();
        dto.setId(entry.getId());
        dto.setPatientId(entry.getPatient().getId());
        dto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        dto.setPatientDni(entry.getPatient().getDni());
        dto.setDentistId(entry.getDentist().getId());
        dto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
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

    // Métodos de búsqueda
    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByText(Long patientId, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getMedicalHistoryByPatient(patientId);
        }
        List<MedicalHistory> entries = medicalHistoryRepository.findByPatientIdAndActiveTrueAndDescriptionContaining(patientId, searchText.trim());
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByText(Long dentistId, Long patientId, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getMedicalHistoryByDentistAndPatient(dentistId, patientId);
        }
        List<MedicalHistory> entries = medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrueAndDescriptionContaining(dentistId, patientId, searchText.trim());
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByDate(Long patientId, LocalDate entryDate) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByPatientIdAndActiveTrueAndEntryDate(patientId, entryDate);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByDate(Long dentistId, Long patientId, LocalDate entryDate) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrueAndEntryDate(dentistId, patientId, entryDate);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByPatientIdAndActiveTrueAndEntryDateBetween(patientId, startDate, endDate);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDto> searchByDateRange(Long dentistId, Long patientId, LocalDate startDate, LocalDate endDate) {
        List<MedicalHistory> entries = medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrueAndEntryDateBetween(dentistId, patientId, startDate, endDate);
        return entries.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
}