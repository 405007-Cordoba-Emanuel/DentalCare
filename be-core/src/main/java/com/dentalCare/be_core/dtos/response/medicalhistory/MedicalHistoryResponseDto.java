package com.dentalCare.be_core.dtos.response.medicalhistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponseDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientDni;
    private Long dentistId;
    private String dentistName;
    private String dentistLicenseNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private String description;
    
    private Long prescriptionId;
    private String prescriptionSummary;

    private Boolean hasFile;
    private String fileUrl;
    private String fileName;
    private String fileType;
    
    private Boolean active;
}
