package com.coursemgmt.service;

import com.coursemgmt.dto.MeetingParticipantResponse;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import com.coursemgmt.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingParticipantService {

    @Autowired
    private MeetingParticipantRepository participantRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Join meeting
     */
    @Transactional
    public MeetingParticipantResponse joinMeeting(Long meetingId, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        
        // Check if already joined
        MeetingParticipant existing = participantRepository
            .findByMeetingIdAndUserId(meetingId, userDetails.getId())
            .orElse(null);
        
        if (existing != null) {
            // Rejoin if left
            if (existing.getLeftAt() != null) {
                existing.setLeftAt(null);
                existing.setJoinedAt(LocalDateTime.now());
                existing = participantRepository.save(existing);
            }
            return MeetingParticipantResponse.fromEntity(existing);
        }
        
        // Check enrollment if meeting is for a course
        if (meeting.getCourse() != null) {
            boolean isInstructor = meeting.getInstructor().getId().equals(userDetails.getId());
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(
                userDetails.getId(), 
                meeting.getCourse().getId()
            );
            
            if (!isInstructor && !isEnrolled) {
                throw new RuntimeException("You must enroll in the course to join this meeting");
            }
        }
        
        // Check max participants
        Long currentCount = participantRepository.countActiveParticipantsByMeetingId(meetingId);
        if (meeting.getMaxParticipants() != null && currentCount >= meeting.getMaxParticipants()) {
            throw new RuntimeException("Meeting is full");
        }
        
        // Check if meeting is ongoing or scheduled
        if (meeting.getStatus() == EMeetingStatus.ENDED || meeting.getStatus() == EMeetingStatus.CANCELLED) {
            throw new RuntimeException("Meeting has ended or been cancelled");
        }
        
        // Create participant
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        
        // Set role
        if (meeting.getInstructor().getId().equals(userDetails.getId())) {
            participant.setRole(EMeetingParticipantRole.HOST);
        } else {
            participant.setRole(EMeetingParticipantRole.PARTICIPANT);
        }
        
        participant.setJoinedAt(LocalDateTime.now());
        
        MeetingParticipant saved = participantRepository.save(participant);
        return MeetingParticipantResponse.fromEntity(saved);
    }

    /**
     * Leave meeting
     */
    @Transactional
    public void leaveMeeting(Long meetingId, UserDetailsImpl userDetails) {
        MeetingParticipant participant = participantRepository
            .findByMeetingIdAndUserId(meetingId, userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Participant", "meetingId", meetingId));
        
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    /**
     * Get meeting participants
     */
    public List<MeetingParticipantResponse> getMeetingParticipants(Long meetingId) {
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(meetingId);
        return participants.stream()
            .map(MeetingParticipantResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get active participants (not left)
     */
    public List<MeetingParticipantResponse> getActiveParticipants(Long meetingId) {
        List<MeetingParticipant> participants = participantRepository.findActiveParticipantsByMeetingId(meetingId);
        return participants.stream()
            .map(MeetingParticipantResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Update participant media state
     */
    @Transactional
    public MeetingParticipantResponse updateParticipantState(
        Long meetingId, 
        Long userId,
        Boolean isAudioEnabled,
        Boolean isVideoEnabled,
        Boolean isScreenSharing,
        UserDetailsImpl userDetails
    ) {
        // Only allow updating own state or if user is host
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        boolean isHost = meeting.getInstructor().getId().equals(userDetails.getId());
        boolean isOwnState = userId.equals(userDetails.getId());
        
        if (!isHost && !isOwnState) {
            throw new RuntimeException("You can only update your own state");
        }
        
        MeetingParticipant participant = participantRepository
            .findByMeetingIdAndUserId(meetingId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", "userId", userId));
        
        if (isAudioEnabled != null) {
            participant.setIsAudioEnabled(isAudioEnabled);
        }
        if (isVideoEnabled != null) {
            participant.setIsVideoEnabled(isVideoEnabled);
        }
        if (isScreenSharing != null) {
            participant.setIsScreenSharing(isScreenSharing);
        }
        
        MeetingParticipant updated = participantRepository.save(participant);
        return MeetingParticipantResponse.fromEntity(updated);
    }
}

