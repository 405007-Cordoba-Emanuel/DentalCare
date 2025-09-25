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
public class PatientUpdateRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "The first name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "The last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "DNI is required")
    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    @Pattern(regexp = "\\d+", message = "DNI must contain only numbers")
    private String dni;


    @NotNull(message = "Birth Date is required")
    @Past(message = "Birth Date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "The email cannot exceed 150 characters")
    private String email;

    @Size(max = 255, message = "The address cannot exceed 255 characters")
    private String address;

    private Boolean active;
}
