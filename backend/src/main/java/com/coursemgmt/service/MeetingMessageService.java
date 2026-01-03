package com.coursemgmt.service;

import com.coursemgmt.dto.MeetingMessageRequest;
import com.coursemgmt.dto.MeetingMessageResponse;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import com.coursemgmt.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingMessageService {

    @Autowired
    private MeetingMessageRepository messageRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingParticipantRepository participantRepository;

    /**
     * Send message in meeting
     */
    @Transactional
    public MeetingMessageResponse sendMessage(Long meetingId, MeetingMessageRequest request, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        
        // Check if user is participant
        boolean isParticipant = participantRepository.existsByMeetingIdAndUserId(meetingId, userDetails.getId());
        if (!isParticipant) {
            throw new RuntimeException("You must join the meeting to send messages");
        }
        
        // Check if chat is allowed
        // TODO: Parse settings JSON to check allowChat
        
        MeetingMessage message = new MeetingMessage();
        message.setMeeting(meeting);
        message.setUser(user);
        message.setMessage(request.getMessage());
        message.setMessageType(EMeetingMessageType.TEXT);
        
        MeetingMessage saved = messageRepository.save(message);
        return MeetingMessageResponse.fromEntity(saved);
    }

    /**
     * Get meeting messages
     */
    public Page<MeetingMessageResponse> getMeetingMessages(Long meetingId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MeetingMessage> messages = messageRepository.findByMeetingIdOrderByCreatedAtDesc(meetingId, pageable);
        
        return messages.map(MeetingMessageResponse::fromEntity);
    }

    /**
     * Get all messages (for real-time)
     */
    public List<MeetingMessageResponse> getAllMeetingMessages(Long meetingId) {
        List<MeetingMessage> messages = messageRepository.findMessagesByMeetingIdOrderByCreatedAt(meetingId);
        return messages.stream()
            .map(MeetingMessageResponse::fromEntity)
            .collect(Collectors.toList());
    }
}

