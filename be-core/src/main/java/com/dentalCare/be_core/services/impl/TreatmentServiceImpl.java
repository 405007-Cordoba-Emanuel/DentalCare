package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.MedicalHistory;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Treatment;
import com.dentalCare.be_core.entities.TreatmentStatus;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.MedicalHistoryRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.TreatmentRepository;
import com.dentalCare.be_core.services.TreatmentService;
import com.dentalCare.be_core.services.UserServiceClient;
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
    private UserServiceClient userServiceClient;

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
        // El status se establece automáticamente en EN_CURSO por @PrePersist si es null
        treatment.setStatus(TreatmentStatus.EN_CURSO);
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

        // Si se proporciona un nuevo estado, actualizarlo
        if (requestDto.getStatus() != null && !requestDto.getStatus().isEmpty()) {
            TreatmentStatus treatmentStatus;
            try {
                treatmentStatus = TreatmentStatus.valueOf(requestDto.getStatus().toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Valid values: EN_CURSO, COMPLETADO, ABANDONADO");
            }
            
            treatment.setStatus(treatmentStatus);
            
            // Si se marca como COMPLETADO y no tiene fecha de finalización, establecerla
            if (treatmentStatus == TreatmentStatus.COMPLETADO && treatment.getActualEndDate() == null) {
                treatment.setActualEndDate(LocalDate.now());
            }
        }

        Treatment updatedTreatment = treatmentRepository.save(treatment);
        return mapToResponseDto(updatedTreatment);
    }

        @Override
    public TreatmentResponseDto updateTreatmentStatus(Long treatmentId, Long dentistId, String status) {                                                        
        Treatment treatment = treatmentRepository.findByIdAndDentistIdAndActiveTrue(treatmentId, dentistId)                                                     
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));                                                 

        // Convertir String a enum
        TreatmentStatus treatmentStatus;
        try {
            treatmentStatus = TreatmentStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values: EN_CURSO, COMPLETADO, ABANDONADO");                                  
        }

        treatment.setStatus(treatmentStatus);

        if (treatmentStatus == TreatmentStatus.COMPLETADO && treatment.getActualEndDate() == null) {                                                                              
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
        // Convertir String a enum
        TreatmentStatus treatmentStatus;
        try {
            treatmentStatus = TreatmentStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values: EN_CURSO, COMPLETADO, ABANDONADO");
        }
        
        List<Treatment> treatments = treatmentRepository.findByPatientIdAndStatusAndActiveTrue(patientId, treatmentStatus);                                              
        return treatments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private TreatmentResponseDto mapToResponseDto(Treatment treatment) {
        TreatmentResponseDto dto = modelMapperUtils.map(treatment, TreatmentResponseDto.class);
        
        UserDetailDto patientUser = userServiceClient.getUserById(treatment.getPatient().getUserId());
        UserDetailDto dentistUser = userServiceClient.getUserById(treatment.getDentist().getUserId());
        
        dto.setPatientId(treatment.getPatient().getId());
        dto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        dto.setDentistId(treatment.getDentist().getId());
        dto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());

                if (treatment.getTotalSessions() != null && treatment.getTotalSessions() > 0) {                                                                         
            double progress = (treatment.getCompletedSessions() * 100.0) / treatment.getTotalSessions();                                                        
            dto.setProgressPercentage(Math.round(progress * 100.0) / 100.0);    
        }

        // Convertir enum a String para el DTO
        dto.setStatus(treatment.getStatus() != null ? treatment.getStatus().name() : null);

        return dto;
    }

    private TreatmentDetailResponseDto mapToDetailResponseDto(Treatment treatment) {
        UserDetailDto patientUser = userServiceClient.getUserById(treatment.getPatient().getUserId());
        UserDetailDto dentistUser = userServiceClient.getUserById(treatment.getDentist().getUserId());
        
        TreatmentDetailResponseDto dto = new TreatmentDetailResponseDto();
        dto.setId(treatment.getId());
        dto.setPatientId(treatment.getPatient().getId());
        dto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        dto.setDentistId(treatment.getDentist().getId());
        dto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        dto.setName(treatment.getName());
        dto.setDescription(treatment.getDescription());
        dto.setStartDate(treatment.getStartDate());
        dto.setEstimatedEndDate(treatment.getEstimatedEndDate());
        dto.setActualEndDate(treatment.getActualEndDate());
        // El enum se serializa automáticamente a String en JSON
        dto.setStatus(treatment.getStatus() != null ? treatment.getStatus().name() : null);
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

        dto.setHasFile(entry.getFileUrl() != null);
        dto.setFileName(entry.getFileName());
        dto.setFileType(entry.getFileType());
        dto.setActive(entry.getActive());

        return dto;
    }

        @Override
    public void incrementTreatmentSessions(Long treatmentId) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));                                                 
        
        // El status ya debería estar en EN_CURSO o COMPLETADO, pero si por alguna razón está null, lo establecemos
        if (treatment.getStatus() == null) {
            treatment.setStatus(TreatmentStatus.EN_CURSO);
        }

        // Increment completed sessions
        treatment.setCompletedSessions(treatment.getCompletedSessions() + 1);   
        treatmentRepository.save(treatment);
    }

    @Override
    public Treatment getTreatmentEntityById(Long treatmentId) {
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("No treatment found with ID: " + treatmentId));
    }
}
