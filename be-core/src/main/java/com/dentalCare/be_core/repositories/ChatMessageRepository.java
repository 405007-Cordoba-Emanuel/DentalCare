package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdAndIsActiveTrueOrderByCreatedDatetimeAsc(Long conversationId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.isActive = true")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.isRead = false AND m.isActive = true")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}

