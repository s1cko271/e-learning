package com.coursemgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private Long courseId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes (8 hours)")
    private Integer durationMinutes = 60;
    
    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 100, message = "Max participants must not exceed 100")
    private Integer maxParticipants = 50;
    
    private MeetingSettings settings;
    
    @Data
    public static class MeetingSettings {
        private Boolean allowScreenShare = true;
        private Boolean allowChat = true;
        private Boolean muteOnJoin = false;
        private Boolean waitingRoom = false;
        private String password;
    }
}

