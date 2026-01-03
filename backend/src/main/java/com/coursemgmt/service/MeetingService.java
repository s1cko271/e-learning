package com.coursemgmt.service;

import com.coursemgmt.dto.MeetingRequest;
import com.coursemgmt.dto.MeetingResponse;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import com.coursemgmt.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingParticipantRepository participantRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Random random = new Random();

    /**
     * Generate unique meeting code
     */
    private String generateMeetingCode() {
        String code;
        do {
            // Format: XXX-#### (e.g., ABC-1234)
            String prefix = generateRandomString(3).toUpperCase();
            int number = 1000 + random.nextInt(9000);
            code = prefix + "-" + number;
        } while (meetingRepository.existsByMeetingCode(code));
        return code;
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Create meeting
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request, UserDetailsImpl userDetails) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        
        // Set instructor
        User instructor = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        meeting.setInstructor(instructor);
        
        // Set course if provided
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));
            
            // Verify instructor owns the course
            if (!course.getInstructor().getId().equals(userDetails.getId())) {
                throw new RuntimeException("You are not authorized to create meetings for this course");
            }
            
            meeting.setCourse(course);
        }
        
        // Generate meeting code
        meeting.setMeetingCode(generateMeetingCode());
        
        // Set time
        meeting.setStartTime(request.getStartTime());
        if (request.getDurationMinutes() != null) {
            meeting.setDurationMinutes(request.getDurationMinutes());
            meeting.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
        }
        
        meeting.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 50);
        
        // Determine status
        if (request.getStartTime().isBefore(LocalDateTime.now()) || 
            request.getStartTime().isEqual(LocalDateTime.now())) {
            meeting.setStatus(EMeetingStatus.ONGOING);
        } else {
            meeting.setStatus(EMeetingStatus.SCHEDULED);
        }
        
        // Parse settings
        if (request.getSettings() != null) {
            try {
                meeting.setSettings(objectMapper.writeValueAsString(request.getSettings()));
            } catch (Exception e) {
                // Default settings
                meeting.setSettings("{\"allowScreenShare\":true,\"allowChat\":true,\"muteOnJoin\":false}");
            }
        }
        
        Meeting saved = meetingRepository.save(meeting);
        
        // Auto-join instructor as HOST
        MeetingParticipant host = new MeetingParticipant();
        host.setMeeting(saved);
        host.setUser(instructor);
        host.setRole(EMeetingParticipantRole.HOST);
        host.setJoinedAt(LocalDateTime.now());
        participantRepository.save(host);
        
        return MeetingResponse.fromEntity(saved);
    }

    /**
     * Update meeting
     */
    @Transactional
    public MeetingResponse updateMeeting(Long meetingId, MeetingRequest request, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        // Verify ownership
        if (!meeting.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to update this meeting");
        }
        
        // Only allow update if SCHEDULED
        if (meeting.getStatus() != EMeetingStatus.SCHEDULED) {
            throw new RuntimeException("Can only update scheduled meetings");
        }
        
        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            meeting.setStartTime(request.getStartTime());
            if (request.getDurationMinutes() != null) {
                meeting.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
            }
        }
        if (request.getDurationMinutes() != null) {
            meeting.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getMaxParticipants() != null) {
            meeting.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getSettings() != null) {
            try {
                meeting.setSettings(objectMapper.writeValueAsString(request.getSettings()));
            } catch (Exception e) {
                // Keep existing settings
            }
        }
        
        Meeting updated = meetingRepository.save(meeting);
        return MeetingResponse.fromEntity(updated);
    }

    /**
     * Delete meeting
     */
    @Transactional
    public void deleteMeeting(Long meetingId, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        // Verify ownership
        if (!meeting.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to delete this meeting");
        }
        
        // Only allow delete if SCHEDULED or ENDED
        if (meeting.getStatus() == EMeetingStatus.ONGOING) {
            throw new RuntimeException("Cannot delete ongoing meeting. Please end it first.");
        }
        
        meetingRepository.delete(meeting);
    }

    /**
     * Get meeting by ID
     */
    public MeetingResponse getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        return MeetingResponse.fromEntity(meeting);
    }

    /**
     * Get meeting by code
     */
    public MeetingResponse getMeetingByCode(String code) {
        Meeting meeting = meetingRepository.findByMeetingCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "code", code));
        return MeetingResponse.fromEntity(meeting);
    }

    /**
     * Get all meetings (with filters)
     */
    public List<MeetingResponse> getMeetings(Long courseId, EMeetingStatus status, Long instructorId) {
        List<Meeting> meetings;
        
        if (courseId != null) {
            if (status != null) {
                meetings = meetingRepository.findByCourseIdAndStatus(courseId, status);
            } else {
                meetings = meetingRepository.findByCourseId(courseId);
            }
        } else if (instructorId != null) {
            meetings = meetingRepository.findByInstructorId(instructorId);
        } else if (status != null) {
            meetings = meetingRepository.findByStatus(status);
        } else {
            meetings = meetingRepository.findAll();
        }
        
        return meetings.stream()
            .map(MeetingResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get meetings for a course
     */
    public List<MeetingResponse> getCourseMeetings(Long courseId) {
        List<Meeting> meetings = meetingRepository.findByCourseIdOrderByStartTimeDesc(courseId);
        return meetings.stream()
            .map(MeetingResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Start meeting (change status to ONGOING)
     */
    @Transactional
    public MeetingResponse startMeeting(Long meetingId, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        // Verify ownership
        if (!meeting.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to start this meeting");
        }
        
        if (meeting.getStatus() != EMeetingStatus.SCHEDULED) {
            throw new RuntimeException("Can only start scheduled meetings");
        }
        
        meeting.setStatus(EMeetingStatus.ONGOING);
        meeting.setStartTime(LocalDateTime.now());
        
        Meeting updated = meetingRepository.save(meeting);
        return MeetingResponse.fromEntity(updated);
    }

    /**
     * End meeting (change status to ENDED)
     */
    @Transactional
    public MeetingResponse endMeeting(Long meetingId, UserDetailsImpl userDetails) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        
        // Verify ownership
        if (!meeting.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to end this meeting");
        }
        
        if (meeting.getStatus() != EMeetingStatus.ONGOING) {
            throw new RuntimeException("Can only end ongoing meetings");
        }
        
        meeting.setStatus(EMeetingStatus.ENDED);
        meeting.setEndTime(LocalDateTime.now());
        
        // Mark all participants as left
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(meetingId);
        for (MeetingParticipant participant : participants) {
            if (participant.getLeftAt() == null) {
                participant.setLeftAt(LocalDateTime.now());
                participantRepository.save(participant);
            }
        }
        
        Meeting updated = meetingRepository.save(meeting);
        return MeetingResponse.fromEntity(updated);
    }
}

