package com.dentalCare.be_core.dtos.request.dentist;

import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "The first name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "The last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "The license number cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "The license number can only contain letters, numbers, and hyphens")
    private String licenseNumber;

    @NotBlank(message = "Specialty is required")
    @Size(max = 150, message = "The specialty cannot exceed 150 characters")
    private String specialty;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "The email cannot exceed 150 characters")
    private String email;

    @Size(max = 255, message = "The address cannot exceed 255 characters")
    private String address;

    private Boolean active;
}
