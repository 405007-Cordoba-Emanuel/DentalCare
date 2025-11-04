package com.dentalCare.be_core.dtos.request.patient;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateRequestDto {

    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    @Pattern(regexp = "^(\\d+|)$", message = "DNI must contain only numbers or be empty")
    private String dni;

    private Boolean active;
}
