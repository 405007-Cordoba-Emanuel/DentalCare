package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.chat.ChatMessageRequestDto;
import com.dentalCare.be_core.dtos.response.chat.ChatMessageResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationSummaryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {

    ConversationResponseDto getOrCreateConversation(Long dentistId, Long patientId);

    ConversationResponseDto getConversationById(Long conversationId, Long userId, String userRole);

    List<ConversationSummaryDto> getConversationsForDentist(Long dentistId, String searchTerm);

    List<ConversationSummaryDto> getConversationsForPatient(Long patientId);

    ChatMessageResponseDto sendMessage(ChatMessageRequestDto request, Long senderId, String senderRole, MultipartFile file);

    void markConversationAsRead(Long conversationId, Long userId, String userRole);

    Long countUnreadConversationsForDentist(Long dentistId);

    Long countUnreadConversationsForPatient(Long patientId);
}

