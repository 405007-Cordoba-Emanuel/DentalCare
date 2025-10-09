package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dentists")
public class Dentist{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User ID is required")
    private Long userId;

    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "License number cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "License number can only contain letters, numbers, and hyphens")
    private String licenseNumber;

    @Column(name = "specialty", nullable = false, length = 150)
    @NotBlank(message = "Specialty is required")
    @Size(max = 150, message = "Specialty cannot exceed 150 characters")
    private String specialty;

    private Boolean active;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistories;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Treatment> treatments;
}
