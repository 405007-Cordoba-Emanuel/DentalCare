package com.dentalCare.be_core.dtos.request.prescription;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequestDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Prescription date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "America/Argentina/Buenos_Aires")
    private LocalDate prescriptionDate;

    @Size(max = 2000, message = "Observations cannot exceed 2000 characters")
    private String observations;

    @Size(max = 2000, message = "Medications cannot exceed 2000 characters")
    private String medications;
}
