package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.prescription.PrescriptionRequestDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;

import java.util.List;

public interface PrescriptionService {

    PrescriptionResponseDto createPrescriptionForDentist(Long dentistId, PrescriptionRequestDto prescriptionRequestDto);

    List<PrescriptionResponseDto> getPrescriptionsByDentistId(Long dentistId);

    List<PrescriptionResponseDto> getPrescriptionsByDentistIdAndPatientId(Long dentistId, Long patientId);

    PrescriptionResponseDto getPrescriptionByIdAndDentistId(Long prescriptionId, Long dentistId);

    PrescriptionResponseDto updatePrescription(Long prescriptionId, Long dentistId, PrescriptionRequestDto prescriptionRequestDto);

    void deletePrescription(Long prescriptionId, Long dentistId);

    List<PrescriptionResponseDto> getPrescriptionsByPatientId(Long patientId);

    PrescriptionResponseDto getPrescriptionByIdAndPatientId(Long prescriptionId, Long patientId);

    long countPrescriptionsByDentistId(Long dentistId);

    long countPrescriptionsByPatientId(Long patientId);
}
