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
public class ChatMessageResponseDto {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderRole;
    private String messageText;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Boolean isRead;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDatetime;
}

