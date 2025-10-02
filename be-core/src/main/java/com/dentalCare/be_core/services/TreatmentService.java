package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;

import java.util.List;

public interface TreatmentService {

    TreatmentResponseDto createTreatment(Long dentistId, TreatmentRequestDto requestDto);

    List<TreatmentResponseDto> getTreatmentsByPatient(Long patientId);

    List<TreatmentResponseDto> getTreatmentsByDentistAndPatient(Long dentistId, Long patientId);

    TreatmentDetailResponseDto getTreatmentDetailById(Long treatmentId, Long dentistId);

    TreatmentDetailResponseDto getTreatmentDetailByIdForPatient(Long treatmentId, Long patientId);

    TreatmentResponseDto updateTreatment(Long treatmentId, Long dentistId, TreatmentRequestDto requestDto);

    TreatmentResponseDto updateTreatmentStatus(Long treatmentId, Long dentistId, String status);

    void deleteTreatment(Long treatmentId, Long dentistId);

    List<TreatmentResponseDto> getTreatmentsByPatientAndStatus(Long patientId, String status);
}
