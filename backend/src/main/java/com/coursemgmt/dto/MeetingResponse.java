package com.coursemgmt.dto;

import com.coursemgmt.model.Meeting;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingResponse {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private Long instructorId;
    private String instructorName;
    private String instructorAvatar;
    private String meetingCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private String status;
    private Boolean isRecordingEnabled;
    private String recordingUrl;
    private MeetingSettings settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long participantCount;

    @Data
    public static class MeetingSettings {
        private Boolean allowScreenShare;
        private Boolean allowChat;
        private Boolean muteOnJoin;
        private Boolean waitingRoom;
        private String password;
    }

    public static MeetingResponse fromEntity(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setTitle(meeting.getTitle());
        response.setDescription(meeting.getDescription());
        
        if (meeting.getCourse() != null) {
            response.setCourseId(meeting.getCourse().getId());
        }
        
        if (meeting.getInstructor() != null) {
            response.setInstructorId(meeting.getInstructor().getId());
            response.setInstructorName(meeting.getInstructor().getFullName());
            response.setInstructorAvatar(meeting.getInstructor().getAvatarUrl());
        }
        
        response.setMeetingCode(meeting.getMeetingCode());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setDurationMinutes(meeting.getDurationMinutes());
        response.setMaxParticipants(meeting.getMaxParticipants());
        response.setStatus(meeting.getStatus() != null ? meeting.getStatus().name() : null);
        response.setIsRecordingEnabled(meeting.getIsRecordingEnabled());
        response.setRecordingUrl(meeting.getRecordingUrl());
        response.setCreatedAt(meeting.getCreatedAt());
        response.setUpdatedAt(meeting.getUpdatedAt());
        
        // Parse settings JSON if exists
        if (meeting.getSettings() != null && !meeting.getSettings().isEmpty()) {
            // TODO: Parse JSON string to MeetingSettings object
            // For now, create default settings
            MeetingSettings settings = new MeetingSettings();
            settings.setAllowScreenShare(true);
            settings.setAllowChat(true);
            settings.setMuteOnJoin(false);
            response.setSettings(settings);
        }
        
        // Count active participants
        if (meeting.getParticipants() != null) {
            long activeCount = meeting.getParticipants().stream()
                .filter(p -> p.getLeftAt() == null)
                .count();
            response.setParticipantCount(activeCount);
        }
        
        return response;
    }
}

