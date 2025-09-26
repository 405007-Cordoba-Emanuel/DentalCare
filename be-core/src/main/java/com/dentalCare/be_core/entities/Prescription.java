package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prescriptions")
public class Prescription{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    @NotNull(message = "Dentist is required")
    private Dentist dentist;

    @Column(name = "prescription_date", nullable = false)
    @NotNull(message = "Prescription date is required")
    private LocalDate prescriptionDate;

    @Column(name = "observations", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Observations cannot exceed 2000 characters")
    private String observations;

    @Column(name = "medications", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Medications cannot exceed 2000 characters")
    private String medications;

    private Boolean active;
}
