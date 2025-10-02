package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "License number cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "License number can only contain letters, numbers, and hyphens")
    private String licenseNumber;

    @Column(name = "specialty", nullable = false, length = 150)
    @NotBlank(message = "Specialty is required")
    @Size(max = 150, message = "Specialty cannot exceed 150 characters")
    private String specialty;

    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Column(name = "email", unique = true, length = 150)
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Column(name = "address", length = 255)
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private Boolean active;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Patient> patients;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistories;

    @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Treatment> treatments;

    /**
     * Gets the dentist's full name
     * @return concatenated full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public String toString() {
        return "Dentist{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", specialty='" + specialty + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                '}' + super.toString();
    }
}
