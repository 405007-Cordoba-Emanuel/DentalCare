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
@Table(name = "medical_history")
public class MedicalHistory {

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

    @Column(name = "entry_date", nullable = false)
    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @NotNull(message = "Description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

//    @Column(name = "file_public_id", length = 255)
//    private String filePublicId;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
    }
}
