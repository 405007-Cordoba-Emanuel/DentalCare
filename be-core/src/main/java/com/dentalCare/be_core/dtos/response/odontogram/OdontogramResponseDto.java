package com.dentalCare.be_core.dtos.response.odontogram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OdontogramResponseDto {

    private Long id;
    private Long patientId;
    private String dentitionType; // adult o child
    private String teethData; // JSON string
    private LocalDateTime createdDatetime;
    private Boolean isActive;
}

