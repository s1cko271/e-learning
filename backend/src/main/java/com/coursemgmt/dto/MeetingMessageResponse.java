package com.coursemgmt.dto;

import com.coursemgmt.model.MeetingMessage;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingMessageResponse {
    private Long id;
    private Long meetingId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String message;
    private String messageType;
    private LocalDateTime createdAt;

    public static MeetingMessageResponse fromEntity(MeetingMessage message) {
        MeetingMessageResponse response = new MeetingMessageResponse();
        response.setId(message.getId());
        
        if (message.getMeeting() != null) {
            response.setMeetingId(message.getMeeting().getId());
        }
        
        if (message.getUser() != null) {
            response.setUserId(message.getUser().getId());
            response.setUserName(message.getUser().getFullName());
            response.setUserAvatar(message.getUser().getAvatarUrl());
        }
        
        response.setMessage(message.getMessage());
        response.setMessageType(message.getMessageType() != null ? message.getMessageType().name() : null);
        response.setCreatedAt(message.getCreatedAt());
        
        return response;
    }
}

