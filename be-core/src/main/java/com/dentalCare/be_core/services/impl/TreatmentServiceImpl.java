package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.MedicalHistory;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Treatment;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.MedicalHistoryRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.TreatmentRepository;
import com.dentalCare.be_core.services.TreatmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TreatmentServiceImpl implements TreatmentService {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private ModelMapperUtils modelMapperUtils;

    @Override
    public TreatmentResponseDto createTreatment(Long dentistId, TreatmentRequestDto requestDto) {
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

        Treatment treatment = modelMapperUtils.map(requestDto, Treatment.class);
        treatment.setId(null);
        treatment.setDentist(dentist);
        treatment.setPatient(patient);
        treatment.setStatus("pendiente");
        treatment.setCompletedSessions(0);
        treatment.setActive(true);

        Treatment savedTreatment = treatmentRepository.save(treatment);
        return mapToResponseDto(savedTreatment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> getTreatmentsByPatient(Long patientId) {
        List<Treatment> treatments = treatmentRepository.findByPatientIdAndActiveTrue(patientId);
        return treatments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> getTreatmentsByDentistAndPatient(Long dentistId, Long patientId) {
        List<Treatment> treatments = treatmentRepository.findByDentistIdAndPatientIdAndActiveTrue(dentistId, patientId);
        return treatments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TreatmentDetailResponseDto getTreatmentDetailById(Long treatmentId, Long dentistId) {
        Treatment treatment = treatmentRepository.findByIdAndDentistIdAndActiveTrue(treatmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));
        return mapToDetailResponseDto(treatment);
    }

    @Override
    @Transactional(readOnly = true)
    public TreatmentDetailResponseDto getTreatmentDetailByIdForPatient(Long treatmentId, Long patientId) {
        Treatment treatment = treatmentRepository.findByIdAndPatientIdAndActiveTrue(treatmentId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));
        return mapToDetailResponseDto(treatment);
    }

    @Override
    public TreatmentResponseDto updateTreatment(Long treatmentId, Long dentistId, TreatmentRequestDto requestDto) {
        Treatment treatment = treatmentRepository.findByIdAndDentistIdAndActiveTrue(treatmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));

        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + requestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        treatment.setPatient(patient);
        treatment.setName(requestDto.getName());
        treatment.setDescription(requestDto.getDescription());
        treatment.setStartDate(requestDto.getStartDate());
        treatment.setEstimatedEndDate(requestDto.getEstimatedEndDate());
        treatment.setTotalSessions(requestDto.getTotalSessions());
        treatment.setNotes(requestDto.getNotes());

        Treatment updatedTreatment = treatmentRepository.save(treatment);
        return mapToResponseDto(updatedTreatment);
    }

    @Override
    public TreatmentResponseDto updateTreatmentStatus(Long treatmentId, Long dentistId, String status) {
        Treatment treatment = treatmentRepository.findByIdAndDentistIdAndActiveTrue(treatmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));

        List<String> validStatuses = List.of("pendiente", "en progreso", "completado", "cancelado");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status. Valid values: pendiente, en progreso, completado, cancelado");
        }

        treatment.setStatus(status);

        if (status.equals("completado") && treatment.getActualEndDate() == null) {
            treatment.setActualEndDate(LocalDate.now());
        }

        Treatment updatedTreatment = treatmentRepository.save(treatment);
        return mapToResponseDto(updatedTreatment);
    }

    @Override
    public void deleteTreatment(Long treatmentId, Long dentistId) {
        Treatment treatment = treatmentRepository.findByIdAndDentistIdAndActiveTrue(treatmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));
        treatment.setActive(false);
        treatmentRepository.save(treatment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponseDto> getTreatmentsByPatientAndStatus(Long patientId, String status) {
        List<Treatment> treatments = treatmentRepository.findByPatientIdAndStatusAndActiveTrue(patientId, status);
        return treatments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private TreatmentResponseDto mapToResponseDto(Treatment treatment) {
        TreatmentResponseDto dto = modelMapperUtils.map(treatment, TreatmentResponseDto.class);
        dto.setPatientId(treatment.getPatient().getId());
        dto.setPatientName(treatment.getPatient().getFirstName() + " " + treatment.getPatient().getLastName());
        dto.setDentistId(treatment.getDentist().getId());
        dto.setDentistName(treatment.getDentist().getFirstName() + " " + treatment.getDentist().getLastName());

        if (treatment.getTotalSessions() != null && treatment.getTotalSessions() > 0) {
            double progress = (treatment.getCompletedSessions() * 100.0) / treatment.getTotalSessions();
            dto.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
        }

        return dto;
    }

    private TreatmentDetailResponseDto mapToDetailResponseDto(Treatment treatment) {
        TreatmentDetailResponseDto dto = new TreatmentDetailResponseDto();
        dto.setId(treatment.getId());
        dto.setPatientId(treatment.getPatient().getId());
        dto.setPatientName(treatment.getPatient().getFirstName() + " " + treatment.getPatient().getLastName());
        dto.setDentistId(treatment.getDentist().getId());
        dto.setDentistName(treatment.getDentist().getFirstName() + " " + treatment.getDentist().getLastName());
        dto.setName(treatment.getName());
        dto.setDescription(treatment.getDescription());
        dto.setStartDate(treatment.getStartDate());
        dto.setEstimatedEndDate(treatment.getEstimatedEndDate());
        dto.setActualEndDate(treatment.getActualEndDate());
        dto.setStatus(treatment.getStatus());
        dto.setTotalSessions(treatment.getTotalSessions());
        dto.setCompletedSessions(treatment.getCompletedSessions());
        dto.setNotes(treatment.getNotes());
        dto.setActive(treatment.getActive());

        if (treatment.getTotalSessions() != null && treatment.getTotalSessions() > 0) {
            double progress = (treatment.getCompletedSessions() * 100.0) / treatment.getTotalSessions();
            dto.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
        }

        List<MedicalHistory> sessions = medicalHistoryRepository.findByPatientIdAndActiveTrue(treatment.getPatient().getId())
                .stream()
                .filter(mh -> mh.getTreatment() != null && mh.getTreatment().getId().equals(treatment.getId()))
                .collect(Collectors.toList());

        List<MedicalHistoryResponseDto> sessionDtos = sessions.stream()
                .map(this::mapMedicalHistoryToDto)
                .collect(Collectors.toList());

        dto.setSessions(sessionDtos);

        return dto;
    }

    private MedicalHistoryResponseDto mapMedicalHistoryToDto(MedicalHistory entry) {
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

        dto.setHasFile(entry.getFileUrl() != null);
        dto.setFileName(entry.getFileName());
        dto.setFileType(entry.getFileType());
        dto.setActive(entry.getActive());

        return dto;
    }
}
