package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

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

    @Column(name = "start_datetime", nullable = false)
    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime", nullable = false)
    @NotNull(message = "End datetime is required")
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    @Column(name = "reason", length = 200)
    @Size(max = 200, message = "Reason cannot exceed 200 characters")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    private Boolean active;

    /**
     * Calcula la duración del turno en minutos
     * @return duración en minutos
     */
    public int getDurationMinutes() {
        if (startDateTime != null && endDateTime != null) {
            return (int) ChronoUnit.MINUTES.between(startDateTime, endDateTime);
        }
        return 0;
    }

    protected void onCreate() {
        super.onCreate();
        if (this.active == null) {
            this.active = true;
        }
        if (this.status == null) {
            this.status = AppointmentStatus.SCHEDULED;
        }
    }
}
