package com.coursemgmt.repository;

import com.coursemgmt.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m " +
           "JOIN FETCH m.sender " +
           "JOIN FETCH m.conversation " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") Long conversationId, Pageable pageable);
    
    @Query("SELECT m FROM Message m " +
           "JOIN FETCH m.sender " +
           "JOIN FETCH m.conversation " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
    
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "JOIN c.participants p " +
           "WHERE c.id = :conversationId " +
           "AND p.user.id = :userId " +
           "AND m.createdAt > COALESCE(p.lastReadAt, :defaultTime) " +
           "AND m.sender.id != :userId")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, 
                            @Param("userId") Long userId,
                            @Param("defaultTime") LocalDateTime defaultTime);
    
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.createdAt > :after " +
           "ORDER BY m.createdAt ASC")
    List<Message> findNewMessagesAfter(@Param("conversationId") Long conversationId, 
                                       @Param("after") LocalDateTime after);
    
    Optional<Message> findByIdAndSenderId(Long messageId, Long senderId);
}

