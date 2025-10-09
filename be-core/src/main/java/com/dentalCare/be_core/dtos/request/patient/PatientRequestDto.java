package com.dentalCare.be_core.dtos.request.patient;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "DNI is required")
    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    @Pattern(regexp = "\\d+", message = "DNI must contain only numbers")
    private String dni;

    @NotNull(message = "Birth Date is required")
    @Past(message = "Birth Date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "Dentist ID is required")
    private Long dentistId;

}
