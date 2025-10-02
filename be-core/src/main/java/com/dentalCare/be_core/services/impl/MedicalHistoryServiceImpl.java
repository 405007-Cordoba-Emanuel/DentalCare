package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.MedicalHistory;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.entities.Treatment;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.MedicalHistoryRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.PrescriptionRepository;
import com.dentalCare.be_core.services.FileStorageService;
import com.dentalCare.be_core.services.MedicalHistoryService;
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
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private com.dentalCare.be_core.repositories.TreatmentRepository treatmentRepository;

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
        medicalHistory.setEntryDate(requestDto.getEntryDate());
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
            
            if (treatment.getStatus().equals("pendiente")) {
                treatment.setStatus("en progreso");
            }
            treatment.setCompletedSessions(treatment.getCompletedSessions() + 1);
            treatmentRepository.save(treatment);
        }

        MedicalHistory savedEntry = medicalHistoryRepository.save(medicalHistory);

        // ✅ Manejo de archivo con Cloudinary
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
        MedicalHistory entry = medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));
        return mapToResponseDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDto getMedicalHistoryEntryByIdForPatient(Long entryId, Long patientId) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndPatientIdAndActiveTrue(entryId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));
        return mapToResponseDto(entry);
    }

    @Override
    public MedicalHistoryResponseDto updateMedicalHistoryEntry(Long entryId, Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file) {
        MedicalHistory entry = medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(entryId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));

        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + requestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        entry.setPatient(patient);
        entry.setEntryDate(requestDto.getEntryDate());
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

        // ✅ Si se reemplaza el archivo
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
                .orElseThrow(() -> new IllegalArgumentException("No medical history entry found with ID: " + entryId));

        // ✅ Marcamos como inactivo
        entry.setActive(false);

        // ✅ Eliminamos archivo de Cloudinary si existía
        if (entry.getFileUrl() != null) {
            fileStorageService.deleteFile(entry.getFileUrl());
            entry.setFileUrl(null);
            entry.setFileName(null);
            entry.setFileType(null);
        }

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
}
