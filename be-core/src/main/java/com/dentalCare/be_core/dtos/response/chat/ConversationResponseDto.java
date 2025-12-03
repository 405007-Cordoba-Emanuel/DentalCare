package com.dentalCare.be_core.dtos.response.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDto {

    private Long id;
    private Long dentistId;
    private String dentistName;
    private String dentistLicenseNumber;
    private Long patientId;
    private String patientName;
    private String patientDni;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageDatetime;

    private String lastMessagePreview;
    private Integer dentistUnreadCount;
    private Integer patientUnreadCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDatetime;

    private List<ChatMessageResponseDto> messages;
}

