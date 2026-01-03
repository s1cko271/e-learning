package com.coursemgmt.dto;

import com.coursemgmt.model.MeetingParticipant;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingParticipantResponse {
    private Long id;
    private Long meetingId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isAudioEnabled;
    private Boolean isVideoEnabled;
    private Boolean isScreenSharing;

    public static MeetingParticipantResponse fromEntity(MeetingParticipant participant) {
        MeetingParticipantResponse response = new MeetingParticipantResponse();
        response.setId(participant.getId());
        
        if (participant.getMeeting() != null) {
            response.setMeetingId(participant.getMeeting().getId());
        }
        
        if (participant.getUser() != null) {
            response.setUserId(participant.getUser().getId());
            response.setUserName(participant.getUser().getFullName());
            response.setUserAvatar(participant.getUser().getAvatarUrl());
        }
        
        response.setRole(participant.getRole() != null ? participant.getRole().name() : null);
        response.setJoinedAt(participant.getJoinedAt());
        response.setLeftAt(participant.getLeftAt());
        response.setIsAudioEnabled(participant.getIsAudioEnabled());
        response.setIsVideoEnabled(participant.getIsVideoEnabled());
        response.setIsScreenSharing(participant.getIsScreenSharing());
        
        return response;
    }
}

