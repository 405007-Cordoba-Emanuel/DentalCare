package com.dentalCare.be_core.dtos.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDto {

    private Long id;
    private Long conversationId;
    private String contactName;
    private String contactInitials;
    private Long contactId;
    private String contactRole; // "DENTIST" o "PATIENT"

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageDatetime;

    private String lastMessagePreview;
    private Integer unreadCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String lastMessageTime; // Formato HH:mm para mostrar
}

