package com.dentalCare.be_core.dtos.response.odontogram;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDatetime;
    
    private Boolean isActive;
}

