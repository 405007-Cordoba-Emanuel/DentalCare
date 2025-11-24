package com.dentalCare.be_core.repositories;

import com.dentalCare.be_core.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByDentistIdAndPatientIdAndIsActiveTrue(Long dentistId, Long patientId);

    @Query("SELECT c FROM Conversation c WHERE c.dentist.id = :dentistId AND c.isActive = true ORDER BY c.lastMessageDatetime DESC NULLS LAST")
    List<Conversation> findByDentistIdOrderByLastMessageDatetimeDesc(@Param("dentistId") Long dentistId);

    @Query("SELECT c FROM Conversation c WHERE c.patient.id = :patientId AND c.isActive = true ORDER BY c.lastMessageDatetime DESC NULLS LAST")
    List<Conversation> findByPatientIdOrderByLastMessageDatetimeDesc(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.dentist.id = :dentistId AND c.dentistUnreadCount > 0 AND c.isActive = true")
    Long countUnreadConversationsByDentistId(@Param("dentistId") Long dentistId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.patient.id = :patientId AND c.patientUnreadCount > 0 AND c.isActive = true")
    Long countUnreadConversationsByPatientId(@Param("patientId") Long patientId);
}

