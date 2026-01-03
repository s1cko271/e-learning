package com.coursemgmt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeetingMessageRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
}

