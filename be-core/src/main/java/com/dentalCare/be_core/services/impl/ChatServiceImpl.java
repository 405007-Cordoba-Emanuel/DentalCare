package com.dentalCare.be_core.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.chat.ChatMessageRequestDto;
import com.dentalCare.be_core.dtos.response.chat.ChatMessageResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationResponseDto;
import com.dentalCare.be_core.dtos.response.chat.ConversationSummaryDto;
import com.dentalCare.be_core.entities.ChatMessage;
import com.dentalCare.be_core.entities.Conversation;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.ChatMessageRepository;
import com.dentalCare.be_core.repositories.ConversationRepository;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.ChatService;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DentistRepository dentistRepository;
    private final PatientRepository patientRepository;
    private final UserServiceClient userServiceClient;
    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB para chat
    private static final List<String> ALLOWED_FILE_TYPES = 
            List.of("image/jpeg", "image/jpg", "image/png", "application/pdf");

    @Override
    public ConversationResponseDto getOrCreateConversation(Long dentistId, Long patientId) {
        Conversation conversation = conversationRepository
                .findByDentistIdAndPatientIdAndIsActiveTrue(dentistId, patientId)
                .orElseGet(() -> createNewConversation(dentistId, patientId));
        
        return mapToConversationResponseDto(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponseDto getConversationById(Long conversationId, Long userId, String userRole) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Verificar que el usuario tiene acceso a esta conversación
        if ("DENTIST".equals(userRole)) {
            if (!conversation.getDentist().getId().equals(getDentistIdByUserId(userId))) {
                throw new IllegalArgumentException("Access denied to this conversation");
            }
        } else if ("PATIENT".equals(userRole)) {
            if (!conversation.getPatient().getId().equals(getPatientIdByUserId(userId))) {
                throw new IllegalArgumentException("Access denied to this conversation");
            }
        }

        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdAndIsActiveTrueOrderByCreatedDatetimeAsc(conversationId);
        
        ConversationResponseDto dto = mapToConversationResponseDto(conversation);
        dto.setMessages(messages.stream()
                .map(this::mapToChatMessageResponseDto)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryDto> getConversationsForDentist(Long dentistId, String searchTerm) {
        // Obtener conversaciones existentes
        List<Conversation> existingConversations = conversationRepository
                .findByDentistIdOrderByLastMessageDatetimeDesc(dentistId);
        
        // Obtener todos los pacientes activos del dentista usando JOIN FETCH
        Dentist dentist = dentistRepository.findByIdWithActivePatients(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found"));
        
        // Verificar que la lista de pacientes no sea null
        List<Patient> activePatients = dentist.getPatients() != null 
                ? dentist.getPatients().stream()
                    .filter(patient -> Boolean.TRUE.equals(patient.getActive()))
                    .collect(Collectors.toList())
                : List.of();
        
        // Crear un mapa de patientId -> Conversation para acceso rápido
        Map<Long, Conversation> conversationMap = existingConversations.stream()
                .collect(Collectors.toMap(conv -> conv.getPatient().getId(), conv -> conv));
        
        // Para cada paciente activo, usar la conversación existente o crear una vacía
        List<ConversationSummaryDto> result = activePatients.stream()
                .filter(patient -> {
                    // Filtrar por término de búsqueda si existe
                    if (searchTerm == null || searchTerm.trim().isEmpty()) {
                        return true;
                    }
                    try {
                        UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
                        String fullName = (patientUser.getFirstName() + " " + patientUser.getLastName()).toLowerCase();
                        return fullName.contains(searchTerm.toLowerCase().trim());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(patient -> {
                    Conversation conversation = conversationMap.get(patient.getId());
                    if (conversation != null) {
                        return mapToConversationSummaryDtoForDentist(conversation);
                    } else {
                        // Crear un DTO vacío para pacientes sin conversación
                        return createEmptyConversationSummaryForPatient(patient);
                    }
                })
                .filter(dto -> dto != null) // Filtrar nulls en caso de error
                .collect(Collectors.toList());
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryDto> getConversationsForPatient(Long patientId) {
        // Obtener conversaciones existentes
        List<Conversation> existingConversations = conversationRepository
                .findByPatientIdOrderByLastMessageDatetimeDesc(patientId);
        
        // Si hay conversaciones, devolverlas
        if (!existingConversations.isEmpty()) {
            return existingConversations.stream()
                    .map(this::mapToConversationSummaryDtoForPatient)
                    .collect(Collectors.toList());
        }
        
        // Si no hay conversaciones, obtener el dentista asignado y crear un DTO vacío
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        // Si el paciente tiene dentista asignado, crear conversación vacía
        if (patient.getDentist() != null) {
            // Cargar el dentista completo para obtener su información
            Dentist dentist = dentistRepository.findById(patient.getDentist().getId())
                    .orElse(null);
            if (dentist != null) {
                ConversationSummaryDto emptyConversation = createEmptyConversationSummaryForDentist(dentist);
                if (emptyConversation != null) {
                    return List.of(emptyConversation);
                }
            }
        }
        
        return List.of();
    }

    @Override
    public ChatMessageResponseDto sendMessage(ChatMessageRequestDto request, Long senderId, String senderRole, MultipartFile file) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);

        // Manejar archivo si existe
        if (file != null && !file.isEmpty()) {
            validateFile(file);
            String fileUrl = uploadFileToCloudinary(file, conversation.getId());
            message.setFileUrl(fileUrl);
            message.setFileName(file.getOriginalFilename());
            message.setFileType(determineFileType(file.getContentType()));
            message.setMessageText(request.getMessageText()); // Puede ser null si solo se envía archivo
        } else {
            message.setMessageText(request.getMessageText());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Actualizar conversación
        updateConversationAfterMessage(conversation, savedMessage, senderRole);

        return mapToChatMessageResponseDto(savedMessage);
    }

    @Override
    public void markConversationAsRead(Long conversationId, Long userId, String userRole) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Marcar mensajes como leídos
        chatMessageRepository.markMessagesAsRead(conversationId, userId);

        // Actualizar contador de no leídos
        if ("DENTIST".equals(userRole)) {
            conversation.setDentistUnreadCount(0);
        } else if ("PATIENT".equals(userRole)) {
            conversation.setPatientUnreadCount(0);
        }

        conversationRepository.save(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadConversationsForDentist(Long dentistId) {
        return conversationRepository.countUnreadConversationsByDentistId(dentistId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadConversationsForPatient(Long patientId) {
        return conversationRepository.countUnreadConversationsByPatientId(patientId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Conversation createNewConversation(Long dentistId, Long patientId) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Conversation conversation = new Conversation();
        conversation.setDentist(dentist);
        conversation.setPatient(patient);
        return conversationRepository.save(conversation);
    }

    private void updateConversationAfterMessage(Conversation conversation, ChatMessage message, String senderRole) {
        conversation.setLastMessageDatetime(message.getCreatedDatetime());
        
        String preview = message.getMessageText();
        if (preview == null || preview.isEmpty()) {
            if (message.getFileType() != null) {
                preview = message.getFileType().equals("image") ? "📷 Imagen" : "📄 Archivo PDF";
            } else {
                preview = "Archivo";
            }
        } else {
            preview = preview.length() > 200 ? preview.substring(0, 200) : preview;
        }
        conversation.setLastMessagePreview(preview);

        // Actualizar contadores de no leídos
        if ("DENTIST".equals(senderRole)) {
            conversation.setPatientUnreadCount(conversation.getPatientUnreadCount() + 1);
        } else if ("PATIENT".equals(senderRole)) {
            conversation.setDentistUnreadCount(conversation.getDentistUnreadCount() + 1);
        }

        conversationRepository.save(conversation);
    }

    private String uploadFileToCloudinary(MultipartFile file, Long conversationId) {
        try {
            // Determinar el tipo de recurso según el tipo de archivo
            String resourceType = "auto";
            String contentType = file.getContentType();
            
            // Para PDFs, usar "raw" explícitamente para mejor compatibilidad
            if (contentType != null && contentType.equals("application/pdf")) {
                resourceType = "raw";
            }
            
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "dental-care/chat/conversation_" + conversationId,
                            "public_id", System.currentTimeMillis() + "_" + file.getOriginalFilename(),
                            "resource_type", resourceType,
                            "access_mode", "public", // Asegurar que los archivos sean públicos
                            "invalidate", true // Invalidar caché para asegurar acceso inmediato
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not upload file to Cloudinary", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (!isValidFileType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG and PDF are allowed");
        }
        if (!isValidFileSize(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
    }

    private boolean isValidFileType(String fileType) {
        return ALLOWED_FILE_TYPES.contains(fileType);
    }

    private boolean isValidFileSize(long fileSize) {
        return fileSize <= MAX_FILE_SIZE;
    }

    private String determineFileType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return "image";
        } else if (contentType != null && contentType.equals("application/pdf")) {
            return "pdf";
        }
        return "unknown";
    }

    private ConversationResponseDto mapToConversationResponseDto(Conversation conversation) {
        UserDetailDto dentistUser = userServiceClient.getUserById(conversation.getDentist().getUserId());
        UserDetailDto patientUser = userServiceClient.getUserById(conversation.getPatient().getUserId());

        ConversationResponseDto dto = new ConversationResponseDto();
        dto.setId(conversation.getId());
        dto.setDentistId(conversation.getDentist().getId());
        dto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        dto.setDentistLicenseNumber(conversation.getDentist().getLicenseNumber());
        dto.setPatientId(conversation.getPatient().getId());
        dto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        dto.setPatientDni(conversation.getPatient().getDni());
        dto.setLastMessageDatetime(conversation.getLastMessageDatetime());
        dto.setLastMessagePreview(conversation.getLastMessagePreview());
        dto.setDentistUnreadCount(conversation.getDentistUnreadCount());
        dto.setPatientUnreadCount(conversation.getPatientUnreadCount());
        dto.setCreatedDatetime(conversation.getCreatedDatetime());
        return dto;
    }

    private ConversationSummaryDto mapToConversationSummaryDtoForDentist(Conversation conversation) {
        UserDetailDto patientUser = userServiceClient.getUserById(conversation.getPatient().getUserId());
        
        ConversationSummaryDto dto = new ConversationSummaryDto();
        dto.setId(conversation.getId());
        dto.setConversationId(conversation.getId());
        dto.setContactName(patientUser.getFirstName() + " " + patientUser.getLastName());
        dto.setContactInitials(getInitials(patientUser.getFirstName(), patientUser.getLastName()));
        dto.setContactId(conversation.getPatient().getId());
        dto.setContactRole("PATIENT");
        dto.setLastMessageDatetime(conversation.getLastMessageDatetime());
        dto.setLastMessagePreview(conversation.getLastMessagePreview());
        dto.setUnreadCount(conversation.getDentistUnreadCount());
        dto.setLastMessageTime(formatTime(conversation.getLastMessageDatetime()));
        return dto;
    }

    private ConversationSummaryDto mapToConversationSummaryDtoForPatient(Conversation conversation) {
        UserDetailDto dentistUser = userServiceClient.getUserById(conversation.getDentist().getUserId());
        
        ConversationSummaryDto dto = new ConversationSummaryDto();
        dto.setId(conversation.getId());
        dto.setConversationId(conversation.getId());
        String fullName = dentistUser.getFirstName() + " " + dentistUser.getLastName();
        // Evitar duplicar "Dr." si ya está en el nombre
        String contactName = fullName.trim().startsWith("Dr. ") || fullName.trim().startsWith("Dr.") 
            ? fullName.trim() 
            : "Dr. " + fullName.trim();
        dto.setContactName(contactName);
        dto.setContactInitials(getInitials(dentistUser.getFirstName(), dentistUser.getLastName()));
        dto.setContactId(conversation.getDentist().getId());
        dto.setContactRole("DENTIST");
        dto.setLastMessageDatetime(conversation.getLastMessageDatetime());
        dto.setLastMessagePreview(conversation.getLastMessagePreview());
        dto.setUnreadCount(conversation.getPatientUnreadCount());
        dto.setLastMessageTime(formatTime(conversation.getLastMessageDatetime()));
        return dto;
    }

    private ChatMessageResponseDto mapToChatMessageResponseDto(ChatMessage message) {
        ChatMessageResponseDto dto = new ChatMessageResponseDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderRole(message.getSenderRole());
        dto.setMessageText(message.getMessageText());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileType(message.getFileType());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedDatetime(message.getCreatedDatetime());
        return dto;
    }

    private String getInitials(String firstName, String lastName) {
        String first = firstName != null && !firstName.isEmpty() ? firstName.charAt(0) + "" : "";
        String last = lastName != null && !lastName.isEmpty() ? lastName.charAt(0) + "" : "";
        return (first + last).toUpperCase();
    }

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private Long getDentistIdByUserId(Long userId) {
        return dentistRepository.findByUserId(userId)
                .map(Dentist::getId)
                .orElseThrow(() -> new IllegalArgumentException("Dentist not found for user ID: " + userId));
    }

    private Long getPatientIdByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .map(Patient::getId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found for user ID: " + userId));
    }

    private ConversationSummaryDto createEmptyConversationSummaryForPatient(Patient patient) {
        try {
            UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
            ConversationSummaryDto dto = new ConversationSummaryDto();
            dto.setId(null); // No hay conversación aún
            dto.setConversationId(null);
            dto.setContactName(patientUser.getFirstName() + " " + patientUser.getLastName());
            dto.setContactInitials(getInitials(patientUser.getFirstName(), patientUser.getLastName()));
            dto.setContactId(patient.getId());
            dto.setContactRole("PATIENT");
            dto.setLastMessageDatetime(null);
            dto.setLastMessagePreview("Sin mensajes");
            dto.setUnreadCount(0);
            dto.setLastMessageTime("");
            return dto;
        } catch (Exception e) {
            log.error("Error creating empty conversation summary for patient: {}", patient.getId(), e);
            return null;
        }
    }

    private ConversationSummaryDto createEmptyConversationSummaryForDentist(Dentist dentist) {
        try {
            UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
            ConversationSummaryDto dto = new ConversationSummaryDto();
            dto.setId(null); // No hay conversación aún
            dto.setConversationId(null);
            String fullName = dentistUser.getFirstName() + " " + dentistUser.getLastName();
            // Evitar duplicar "Dr." si ya está en el nombre
            String contactName = fullName.trim().startsWith("Dr. ") || fullName.trim().startsWith("Dr.") 
                ? fullName.trim() 
                : "Dr. " + fullName.trim();
            dto.setContactName(contactName);
            dto.setContactInitials(getInitials(dentistUser.getFirstName(), dentistUser.getLastName()));
            dto.setContactId(dentist.getId());
            dto.setContactRole("DENTIST");
            dto.setLastMessageDatetime(null);
            dto.setLastMessagePreview("Sin mensajes");
            dto.setUnreadCount(0);
            dto.setLastMessageTime("");
            return dto;
        } catch (Exception e) {
            log.error("Error creating empty conversation summary for dentist: {}", dentist.getId(), e);
            return null;
        }
    }
}

