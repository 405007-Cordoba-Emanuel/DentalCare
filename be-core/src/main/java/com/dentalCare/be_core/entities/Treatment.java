package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treatments")
public class Treatment {

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

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Treatment name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "estimated_end_date")
    private LocalDate estimatedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TreatmentStatus status;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "completed_sessions")
    private Integer completedSessions;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Notes cannot exceed 5000 characters")
    private String notes;

    private Boolean active;

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> sessions;

    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.status == null) {
            this.status = TreatmentStatus.EN_CURSO;
        }
        if (this.completedSessions == null) {
            this.completedSessions = 0;
        }
    }
}
