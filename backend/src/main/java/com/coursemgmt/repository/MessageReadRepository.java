package com.coursemgmt.repository;

import com.coursemgmt.model.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
    
    Optional<MessageRead> findByMessageIdAndUserId(Long messageId, Long userId);
    
    List<MessageRead> findByMessageId(Long messageId);
    
    @Query("SELECT mr FROM MessageRead mr " +
           "JOIN FETCH mr.message m " +
           "JOIN FETCH mr.user " +
           "WHERE m.conversation.id = :conversationId " +
           "AND mr.user.id = :userId")
    List<MessageRead> findByConversationIdAndUserId(@Param("conversationId") Long conversationId, 
                                                     @Param("userId") Long userId);
}

