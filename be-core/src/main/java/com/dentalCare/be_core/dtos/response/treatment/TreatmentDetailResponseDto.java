package com.dentalCare.be_core.dtos.response.treatment;

import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentDetailResponseDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private String name;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate estimatedEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    private String status;
    private Integer totalSessions;
    private Integer completedSessions;
    private Double progressPercentage;
    private String notes;
    private Boolean active;
    private List<MedicalHistoryResponseDto> sessions;
}
