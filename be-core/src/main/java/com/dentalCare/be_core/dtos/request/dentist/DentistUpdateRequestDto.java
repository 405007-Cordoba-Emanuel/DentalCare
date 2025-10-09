package com.dentalCare.be_core.dtos.request.dentist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentistUpdateRequestDto {

    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "The license number cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "The license number can only contain letters, numbers, and hyphens")
    private String licenseNumber;

    @NotBlank(message = "Specialty is required")
    @Size(max = 150, message = "The specialty cannot exceed 150 characters")
    private String specialty;

    private Boolean active;
}
