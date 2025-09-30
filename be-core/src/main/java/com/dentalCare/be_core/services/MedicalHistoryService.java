package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MedicalHistoryService {

    MedicalHistoryResponseDto createMedicalHistoryEntry(Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file);

    List<MedicalHistoryResponseDto> getMedicalHistoryByPatient(Long patientId);

    List<MedicalHistoryResponseDto> getMedicalHistoryByDentistAndPatient(Long dentistId, Long patientId);

    MedicalHistoryResponseDto getMedicalHistoryEntryById(Long entryId, Long dentistId);

    MedicalHistoryResponseDto getMedicalHistoryEntryByIdForPatient(Long entryId, Long patientId);

    MedicalHistoryResponseDto updateMedicalHistoryEntry(Long entryId, Long dentistId, MedicalHistoryRequestDto requestDto, MultipartFile file);

    void deleteMedicalHistoryEntry(Long entryId, Long dentistId);
}
