package com.dentalCare.be_core.dtos.response.prescription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientDni;
    private Long dentistId;
    private String dentistName;
    private String dentistLicenseNumber;
    private String dentistSpecialty;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate prescriptionDate;
    private String observations;
    private String medications;
    private Boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedDatetime;
}
