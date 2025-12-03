package com.dentalCare.be_core.dtos.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    private String messageText;

    private String fileUrl;

    private String fileName;

    private String fileType; // "image" o "pdf"
}

