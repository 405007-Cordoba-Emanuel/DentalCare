package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"dentist_id", "patient_id"})
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "last_message_datetime")
    private LocalDateTime lastMessageDatetime;

    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    @Column(name = "dentist_unread_count", nullable = false)
    private Integer dentistUnreadCount = 0;

    @Column(name = "patient_unread_count", nullable = false)
    private Integer patientUnreadCount = 0;

    @Column(name = "created_datetime", nullable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "last_updated_datetime", nullable = false)
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDatetime = now;
        this.lastUpdatedDatetime = now;
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.dentistUnreadCount == null) {
            this.dentistUnreadCount = 0;
        }
        if (this.patientUnreadCount == null) {
            this.patientUnreadCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedDatetime = LocalDateTime.now();
    }
}

