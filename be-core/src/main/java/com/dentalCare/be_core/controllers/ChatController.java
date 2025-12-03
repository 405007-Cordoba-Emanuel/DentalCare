package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.chat.ChatMessageRequestDto;
import com.dentalCare.be_core.dtos.response.chat.ChatMessageResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationSummaryDto;
import com.dentalCare.be_core.services.ChatService;
import com.dentalCare.be_core.services.DentistService;
import com.dentalCare.be_core.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core/chat")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API for chat management")
public class ChatController {

    private final ChatService chatService;
    private final DentistService dentistService;
    private final PatientService patientService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/conversations")
    @Operation(summary = "Get conversations for current user")
    public ResponseEntity<List<ConversationSummaryDto>> getConversations(
            @Parameter(description = "Search term for filtering conversations (dentist only)")
            @RequestParam(required = false) String searchTerm,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        String userRole = getUserRoleFromToken(authHeader);

        List<ConversationSummaryDto> conversations;
        if ("DENTIST".equals(userRole)) {
            Long dentistId = getDentistIdByUserId(userId);
            conversations = chatService.getConversationsForDentist(dentistId, searchTerm);
        } else {
            Long patientId = getPatientIdByUserId(userId);
            conversations = chatService.getConversationsForPatient(patientId);
        }

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get conversation by ID with messages")
    public ResponseEntity<ConversationResponseDto> getConversation(
            @PathVariable Long conversationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        String userRole = getUserRoleFromToken(authHeader);

        ConversationResponseDto conversation = chatService.getConversationById(conversationId, userId, userRole);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/conversations")
    @Operation(summary = "Get or create conversation between dentist and patient")
    public ResponseEntity<ConversationResponseDto> getOrCreateConversation(
            @RequestParam Long dentistId,
            @RequestParam Long patientId) {
        
        ConversationResponseDto conversation = chatService.getOrCreateConversation(dentistId, patientId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping(value = "/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Send a message")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @RequestPart("conversationId") String conversationId,
            @RequestPart(required = false) String messageText,
            @RequestPart(required = false) MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ChatMessageRequestDto request = new ChatMessageRequestDto();
        request.setConversationId(Long.parseLong(conversationId));
        request.setMessageText(messageText);
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        String userRole = getUserRoleFromToken(authHeader);

        ChatMessageResponseDto message = chatService.sendMessage(request, userId, userRole, file);

        // Enviar mensaje a través de WebSocket
        ConversationResponseDto conversation = chatService.getConversationById(
                request.getConversationId(), userId, userRole);
        
        // Determinar el destinatario
        String destination;
        if ("DENTIST".equals(userRole)) {
            destination = "/queue/chat/" + conversation.getPatientId();
        } else {
            destination = "/queue/chat/" + conversation.getDentistId();
        }
        
        messagingTemplate.convertAndSend(destination, message);
        messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), message);

        return ResponseEntity.ok(message);
    }

    @PostMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Mark conversation as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        String userRole = getUserRoleFromToken(authHeader);

        chatService.markConversationAsRead(conversationId, userId, userRole);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread conversations")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0L));
        }
        
        String userRole = getUserRoleFromToken(authHeader);

        Long count;
        if ("DENTIST".equals(userRole)) {
            Long dentistId = getDentistIdByUserId(userId);
            count = chatService.countUnreadConversationsForDentist(dentistId);
        } else {
            Long patientId = getPatientIdByUserId(userId);
            count = chatService.countUnreadConversationsForPatient(patientId);
        }

        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Helper methods
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            // Decode JWT payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // JWT format: {"id":"123","role":"DENTIST",...}
            // Look for "id" field (with quotes)
            int idStart = payload.indexOf("\"id\":\"") + 6;
            if (idStart < 6) {
                // Try without quotes (number)
                idStart = payload.indexOf("\"id\":") + 5;
                if (idStart >= 5) {
                    int idEnd = payload.indexOf(",", idStart);
                    if (idEnd == -1) idEnd = payload.indexOf("}", idStart);
                    String idStr = payload.substring(idStart, idEnd).trim().replace("\"", "");
                    return Long.parseLong(idStr);
                }
                return null;
            }
            
            int idEnd = payload.indexOf("\"", idStart);
            String idStr = payload.substring(idStart, idEnd);
            return Long.parseLong(idStr);
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            return null;
        }
    }
    
    private String getUserRoleFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "PATIENT";
        }
        
        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return "PATIENT";
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Look for "role" field
            int roleStart = payload.indexOf("\"role\":\"") + 8;
            if (roleStart < 8) {
                return "PATIENT";
            }
            
            int roleEnd = payload.indexOf("\"", roleStart);
            String role = payload.substring(roleStart, roleEnd);
            return role;
        } catch (Exception e) {
            log.error("Error parsing role from token: {}", e.getMessage());
            return "PATIENT";
        }
    }

    private Long getDentistIdByUserId(Long userId) {
        return dentistService.getDentistIdByUserId(userId);
    }

    private Long getPatientIdByUserId(Long userId) {
        return patientService.getPatientIdByUserId(userId);
    }
}

