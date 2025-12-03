package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // userId del remitente

    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole; // "DENTIST" o "PATIENT"

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType; // "image" o "pdf"

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_datetime", nullable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        // Obtener la hora actual en la zona horaria de Buenos Aires
        // Esto asegura que siempre guardemos en hora de Buenos Aires, independientemente de la zona horaria del servidor
        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        this.createdDatetime = ZonedDateTime.now(buenosAiresZone).toLocalDateTime();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
    }
}

