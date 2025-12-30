package com.coursemgmt.repository;

import com.coursemgmt.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
    
    List<ConversationParticipant> findByConversationId(Long conversationId);
    
    List<ConversationParticipant> findByUserId(Long userId);
    
    @Query("SELECT cp FROM ConversationParticipant cp " +
           "JOIN FETCH cp.user " +
           "WHERE cp.conversation.id = :conversationId AND cp.user.id != :userId")
    List<ConversationParticipant> findOtherParticipants(@Param("conversationId") Long conversationId, 
                                                        @Param("userId") Long userId);
}

