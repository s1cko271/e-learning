package com.coursemgmt.controller;

import com.coursemgmt.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MeetingWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle WebRTC offer
     */
    @MessageMapping("/meeting.offer")
    public void handleOffer(@Payload WebRTCMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                log.error("User not authenticated");
                return;
            }
            
            Long senderId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            message.setFromUserId(senderId);
            
            // Forward offer to target user
            messagingTemplate.convertAndSendToUser(
                message.getToUserId().toString(),
                "/queue/meeting.offer",
                message
            );
            
            log.debug("Offer forwarded from {} to {}", senderId, message.getToUserId());
        } catch (Exception e) {
            log.error("Error handling offer: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle WebRTC answer
     */
    @MessageMapping("/meeting.answer")
    public void handleAnswer(@Payload WebRTCMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                log.error("User not authenticated");
                return;
            }
            
            Long senderId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            message.setFromUserId(senderId);
            
            // Forward answer to target user
            messagingTemplate.convertAndSendToUser(
                message.getToUserId().toString(),
                "/queue/meeting.answer",
                message
            );
            
            log.debug("Answer forwarded from {} to {}", senderId, message.getToUserId());
        } catch (Exception e) {
            log.error("Error handling answer: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle ICE candidate
     */
    @MessageMapping("/meeting.ice")
    public void handleIceCandidate(@Payload WebRTCMessage message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                log.error("User not authenticated");
                return;
            }
            
            Long senderId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            message.setFromUserId(senderId);
            
            // Forward ICE candidate to target user
            messagingTemplate.convertAndSendToUser(
                message.getToUserId().toString(),
                "/queue/meeting.ice",
                message
            );
            
            log.debug("ICE candidate forwarded from {} to {}", senderId, message.getToUserId());
        } catch (Exception e) {
            log.error("Error handling ICE candidate: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle participant joined
     */
    @MessageMapping("/meeting.join")
    public void handleJoin(@Payload MeetingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                log.error("User not authenticated");
                return;
            }
            
            Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            event.setUserId(userId);
            
            // Broadcast to all participants in the meeting
            messagingTemplate.convertAndSend("/topic/meeting/" + event.getMeetingId(), event);
            
            log.debug("User {} joined meeting {}", userId, event.getMeetingId());
        } catch (Exception e) {
            log.error("Error handling join: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle participant left
     */
    @MessageMapping("/meeting.leave")
    public void handleLeave(@Payload MeetingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                log.error("User not authenticated");
                return;
            }
            
            Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            event.setUserId(userId);
            
            // Broadcast to all participants in the meeting
            messagingTemplate.convertAndSend("/topic/meeting/" + event.getMeetingId(), event);
            
            log.debug("User {} left meeting {}", userId, event.getMeetingId());
        } catch (Exception e) {
            log.error("Error handling leave: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle toggle audio
     */
    @MessageMapping("/meeting.toggle-audio")
    public void handleToggleAudio(@Payload MeetingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                return;
            }
            
            Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            event.setUserId(userId);
            
            // Broadcast to all participants
            messagingTemplate.convertAndSend("/topic/meeting/" + event.getMeetingId(), event);
        } catch (Exception e) {
            log.error("Error handling toggle audio: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle toggle video
     */
    @MessageMapping("/meeting.toggle-video")
    public void handleToggleVideo(@Payload MeetingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                return;
            }
            
            Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            event.setUserId(userId);
            
            // Broadcast to all participants
            messagingTemplate.convertAndSend("/topic/meeting/" + event.getMeetingId(), event);
        } catch (Exception e) {
            log.error("Error handling toggle video: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle screen share
     */
    @MessageMapping("/meeting.screen-share")
    public void handleScreenShare(@Payload MeetingEvent event, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                return;
            }
            
            Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            event.setUserId(userId);
            
            // Broadcast to all participants
            messagingTemplate.convertAndSend("/topic/meeting/" + event.getMeetingId(), event);
        } catch (Exception e) {
            log.error("Error handling screen share: {}", e.getMessage(), e);
        }
    }
    
    // Inner classes for WebSocket messages
    public static class WebRTCMessage {
        private Long meetingId;
        private Long fromUserId;
        private Long toUserId;
        private Map<String, Object> data; // SDP, ICE candidate, etc.
        
        public Long getMeetingId() { return meetingId; }
        public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
        public Long getFromUserId() { return fromUserId; }
        public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
        public Long getToUserId() { return toUserId; }
        public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
    
    public static class MeetingEvent {
        private Long meetingId;
        private Long userId;
        private String eventType; // join, leave, toggle-audio, toggle-video, screen-share
        private Map<String, Object> data;
        
        public MeetingEvent() {
            this.data = new HashMap<>();
        }
        
        public Long getMeetingId() { return meetingId; }
        public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
}

