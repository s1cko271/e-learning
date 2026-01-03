package com.coursemgmt.controller;

import com.coursemgmt.dto.*;
import com.coursemgmt.model.EMeetingStatus;
import com.coursemgmt.security.services.UserDetailsImpl;
import com.coursemgmt.service.MeetingService;
import com.coursemgmt.service.MeetingParticipantService;
import com.coursemgmt.service.MeetingMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingParticipantService participantService;

    @Autowired
    private MeetingMessageService messageService;

    /**
     * Get all meetings (with optional filters)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeetingResponse>> getMeetings(
        @RequestParam(required = false) Long courseId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long instructorId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        EMeetingStatus meetingStatus = null;
        if (status != null) {
            try {
                meetingStatus = EMeetingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        List<MeetingResponse> meetings = meetingService.getMeetings(courseId, meetingStatus, instructorId);
        return ResponseEntity.ok(meetings);
    }

    /**
     * Get meeting by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long id) {
        MeetingResponse meeting = meetingService.getMeeting(id);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Get meeting by code
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> getMeetingByCode(@PathVariable String code) {
        MeetingResponse meeting = meetingService.getMeetingByCode(code);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Create meeting
     */
    @PostMapping
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> createMeeting(
        @Valid @RequestBody MeetingRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingResponse meeting = meetingService.createMeeting(request, userDetails);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Update meeting
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> updateMeeting(
        @PathVariable Long id,
        @Valid @RequestBody MeetingRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingResponse meeting = meetingService.updateMeeting(id, request, userDetails);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Delete meeting
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMeeting(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        meetingService.deleteMeeting(id, userDetails);
        return ResponseEntity.ok(new MessageResponse("Meeting deleted successfully!"));
    }

    /**
     * Start meeting
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> startMeeting(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingResponse meeting = meetingService.startMeeting(id, userDetails);
        return ResponseEntity.ok(meeting);
    }

    /**
     * End meeting
     */
    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<MeetingResponse> endMeeting(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingResponse meeting = meetingService.endMeeting(id, userDetails);
        return ResponseEntity.ok(meeting);
    }

    /**
     * Join meeting
     */
    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingParticipantResponse> joinMeeting(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingParticipantResponse participant = participantService.joinMeeting(id, userDetails);
        return ResponseEntity.ok(participant);
    }

    /**
     * Join meeting by code
     */
    @PostMapping("/code/{code}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> joinMeetingByCode(
        @PathVariable String code,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingResponse meeting = meetingService.getMeetingByCode(code);
        MeetingParticipantResponse participant = participantService.joinMeeting(meeting.getId(), userDetails);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meeting", meeting);
        response.put("participant", participant);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Leave meeting
     */
    @PostMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> leaveMeeting(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        participantService.leaveMeeting(id, userDetails);
        return ResponseEntity.ok(new MessageResponse("Left meeting successfully!"));
    }

    /**
     * Get meeting participants
     */
    @GetMapping("/{id}/participants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeetingParticipantResponse>> getParticipants(@PathVariable Long id) {
        List<MeetingParticipantResponse> participants = participantService.getMeetingParticipants(id);
        return ResponseEntity.ok(participants);
    }

    /**
     * Get active participants
     */
    @GetMapping("/{id}/participants/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeetingParticipantResponse>> getActiveParticipants(@PathVariable Long id) {
        List<MeetingParticipantResponse> participants = participantService.getActiveParticipants(id);
        return ResponseEntity.ok(participants);
    }

    /**
     * Update participant state
     */
    @PatchMapping("/{id}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingParticipantResponse> updateParticipantState(
        @PathVariable Long id,
        @PathVariable Long userId,
        @RequestBody Map<String, Boolean> state,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingParticipantResponse participant = participantService.updateParticipantState(
            id,
            userId,
            state.get("isAudioEnabled"),
            state.get("isVideoEnabled"),
            state.get("isScreenSharing"),
            userDetails
        );
        return ResponseEntity.ok(participant);
    }

    /**
     * Get meeting messages
     */
    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MeetingMessageResponse>> getMessages(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        Page<MeetingMessageResponse> messages = messageService.getMeetingMessages(id, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send message in meeting
     */
    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingMessageResponse> sendMessage(
        @PathVariable Long id,
        @Valid @RequestBody MeetingMessageRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        MeetingMessageResponse message = messageService.sendMessage(id, request, userDetails);
        return ResponseEntity.ok(message);
    }
}

