package com.coursemgmt.dto;

import com.coursemgmt.model.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    private UserInfo otherParticipant; // Người còn lại trong conversation
    private ChatMessageResponse lastMessage;
    private Long unreadCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String avatar;
        private String role;
    }
    
    public static ConversationResponse fromEntity(Conversation conversation, UserInfo otherParticipant, 
                                                   ChatMessageResponse lastMessage, Long unreadCount) {
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation cannot be null");
        }
        
        String typeName = conversation.getType() != null ? conversation.getType().name() : "DIRECT";
        
        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(typeName)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessageAt(conversation.getLastMessageAt())
                .otherParticipant(otherParticipant)
                .lastMessage(lastMessage)
                .unreadCount(unreadCount != null ? unreadCount : 0L)
                .build();
    }
}

