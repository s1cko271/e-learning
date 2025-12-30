package com.coursemgmt.service;

import com.coursemgmt.dto.*;
import com.coursemgmt.dto.ChatMessageResponse;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    
    @Transactional
    public ConversationResponse createConversation(Long currentUserId, CreateConversationRequest request) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User otherUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate enrollment: If student wants to chat with instructor, check enrollment
        boolean isCurrentUserStudent = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_STUDENT);
        boolean isOtherUserInstructor = otherUser.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_LECTURER);
        
        if (isCurrentUserStudent && isOtherUserInstructor) {
            // Student chatting with instructor - must be enrolled in at least one course
            if (request.getCourseId() != null) {
                // Validate specific course enrollment
                boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, request.getCourseId());
                if (!isEnrolled) {
                    throw new RuntimeException("Bạn phải đăng ký khóa học này để nhắn tin với giảng viên");
                }
                
                // Verify the course belongs to the instructor
                Course course = courseRepository.findById(request.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Course not found"));
                if (!course.getInstructor().getId().equals(request.getUserId())) {
                    throw new RuntimeException("Khóa học này không thuộc về giảng viên này");
                }
            } else {
                // Check if student is enrolled in any course by this instructor
                List<Course> instructorCourses = courseRepository.findByInstructorId(request.getUserId());
                boolean hasEnrollment = instructorCourses.stream()
                        .anyMatch(course -> enrollmentRepository.existsByUserIdAndCourseId(currentUserId, course.getId()));
                
                if (!hasEnrollment) {
                    throw new RuntimeException("Bạn phải đăng ký ít nhất một khóa học của giảng viên này để nhắn tin");
                }
            }
        }
        
        // Check if direct conversation already exists
        Conversation existingConversation = conversationRepository
                .findDirectConversationBetweenUsers(currentUserId, request.getUserId())
                .orElse(null);
        
        if (existingConversation != null) {
            return getConversationResponse(existingConversation, currentUserId);
        }
        
        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setType(Conversation.ConversationType.DIRECT);
        conversation = conversationRepository.save(conversation);
        
        // Add participants
        ConversationParticipant participant1 = new ConversationParticipant();
        participant1.setConversation(conversation);
        participant1.setUser(currentUser);
        participant1.setRole(getUserRole(currentUser));
        
        ConversationParticipant participant2 = new ConversationParticipant();
        participant2.setConversation(conversation);
        participant2.setUser(otherUser);
        participant2.setRole(getUserRole(otherUser));
        
        participantRepository.save(participant1);
        participantRepository.save(participant2);
        
        return getConversationResponse(conversation, currentUserId);
    }
    
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId) {
        try {
            List<Conversation> conversations = conversationRepository
                    .findByUserIdOrderByLastMessageAtDesc(userId);
            
            return conversations.stream()
                    .map(conv -> {
                        try {
                            return getConversationResponse(conv, userId);
                        } catch (Exception e) {
                            log.error("Error processing conversation {}: {}", conv.getId(), e.getMessage(), e);
                            // Return a minimal response to avoid breaking the entire list
                            return ConversationResponse.fromEntity(conv, null, null, 0L);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting user conversations for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error fetching conversations: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        return getConversationResponse(conversation, userId);
    }
    
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify sender is participant
        participantRepository.findByConversationIdAndUserId(conversation.getId(), senderId)
                .orElseThrow(() -> new RuntimeException("User is not a participant"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        
        message = messageRepository.save(message);
        
        // Update conversation last message time
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return ChatMessageResponse.fromEntity(message, false, null);
    }
    
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long conversationId, Long userId, int page, int size) {
        // Verify user is participant
        participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a participant"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        
        // Get read status for each message
        List<MessageRead> reads = messageReadRepository.findByConversationIdAndUserId(conversationId, userId);
        
        return messages.map(msg -> {
            MessageRead read = reads.stream()
                    .filter(r -> {
                        try {
                            return r.getMessage() != null && r.getMessage().getId() != null && 
                                   r.getMessage().getId().equals(msg.getId());
                        } catch (Exception e) {
                            log.warn("Error checking message read status: {}", e.getMessage());
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);
            
            try {
                return ChatMessageResponse.fromEntity(
                        msg,
                        read != null,
                        read != null ? read.getReadAt() : null
                );
            } catch (Exception e) {
                log.error("Error creating ChatMessageResponse for message {}: {}", msg.getId(), e.getMessage(), e);
                throw new RuntimeException("Error processing message: " + e.getMessage(), e);
            }
        });
    }
    
    @Transactional
    public ChatMessageResponse updateMessage(Long messageId, Long userId, UpdateMessageRequest request) {
        Message message = messageRepository.findByIdAndSenderId(messageId, userId)
                .orElseThrow(() -> new RuntimeException("Message not found or unauthorized"));
        
        message.setContent(request.getContent());
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());
        
        message = messageRepository.save(message);
        
        MessageRead read = messageReadRepository.findByMessageIdAndUserId(messageId, userId).orElse(null);
        return ChatMessageResponse.fromEntity(message, read != null, read != null ? read.getReadAt() : null);
    }
    
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findByIdAndSenderId(messageId, userId)
                .orElseThrow(() -> new RuntimeException("Message not found or unauthorized"));
        
        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
    }
    
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        participant.setLastReadAt(LocalDateTime.now());
        participantRepository.save(participant);
        
        // Mark all unread messages as read
        List<Message> allMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<Message> unreadMessages = allMessages.stream()
                .filter(msg -> {
                    // Check if message is from other users
                    try {
                        Long senderId = msg.getSender().getId();
                        if (senderId.equals(userId)) {
                            return false; // Skip own messages
                        }
                    } catch (Exception e) {
                        log.warn("Error getting sender ID: {}", e.getMessage());
                        return false;
                    }
                    
                    // Check if already read
                    MessageRead existingRead = messageReadRepository
                            .findByMessageIdAndUserId(msg.getId(), userId)
                            .orElse(null);
                    return existingRead == null;
                })
                .collect(Collectors.toList());
        
        for (Message msg : unreadMessages) {
            MessageRead read = new MessageRead();
            read.setMessage(msg);
            read.setUser(participant.getUser());
            messageReadRepository.save(read);
        }
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long conversationId, Long userId) {
        return messageRepository.countUnreadMessages(conversationId, userId, LocalDateTime.MIN);
    }
    
    /**
     * Lấy danh sách giảng viên từ các khóa học đã đăng ký của student
     */
    @Transactional(readOnly = true)
    public List<InstructorInfoDTO> getEnrolledInstructors(Long studentId) {
        // Get all enrollments for this student
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(studentId);
        
        // Extract unique instructors with course info
        return enrollments.stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getInstructor() != null)
                .map(e -> {
                    User instructor = e.getCourse().getInstructor();
                    return InstructorInfoDTO.builder()
                            .id(instructor.getId())
                            .fullName(instructor.getFullName())
                            .username(instructor.getUsername())
                            .email(instructor.getEmail())
                            .avatarUrl(instructor.getAvatarUrl())
                            .courseId(e.getCourse().getId())
                            .courseTitle(e.getCourse().getTitle())
                            .build();
                })
                .distinct() // Remove duplicates if student enrolled in multiple courses by same instructor
                .collect(Collectors.toList());
    }
    
    private ConversationResponse getConversationResponse(Conversation conversation, Long currentUserId) {
        try {
            // Get other participant
            List<ConversationParticipant> otherParticipants = participantRepository
                    .findOtherParticipants(conversation.getId(), currentUserId);
            
            ConversationResponse.UserInfo otherParticipant = null;
            if (!otherParticipants.isEmpty()) {
                ConversationParticipant participant = otherParticipants.get(0);
                User otherUser = participant.getUser();
                if (otherUser != null) {
                    otherParticipant = ConversationResponse.UserInfo.builder()
                            .id(otherUser.getId())
                            .fullName(otherUser.getFullName())
                            .avatar(otherUser.getAvatarUrl())
                            .role(participant.getRole() != null ? participant.getRole().name() : "STUDENT")
                            .build();
                }
            }
            
            // Get last message - query separately to avoid LAZY loading issues
            ChatMessageResponse lastMessage = null;
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
            if (!messages.isEmpty()) {
                Message lastMsg = messages.get(messages.size() - 1); // Last message (most recent)
                if (lastMsg != null && lastMsg.getSender() != null) {
                    MessageRead read = messageReadRepository
                            .findByMessageIdAndUserId(lastMsg.getId(), currentUserId)
                            .orElse(null);
                    lastMessage = ChatMessageResponse.fromEntity(
                            lastMsg,
                            read != null,
                            read != null ? read.getReadAt() : null
                    );
                }
            }
            
            // Get unread count
            Long unreadCount = getUnreadCount(conversation.getId(), currentUserId);
            
            return ConversationResponse.fromEntity(conversation, otherParticipant, lastMessage, unreadCount);
        } catch (Exception e) {
            log.error("Error creating ConversationResponse for conversation {}: {}", conversation.getId(), e.getMessage(), e);
            throw new RuntimeException("Error processing conversation: " + e.getMessage(), e);
        }
    }
    
    private ConversationParticipant.ParticipantRole getUserRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER) 
                ? ConversationParticipant.ParticipantRole.INSTRUCTOR
                : ConversationParticipant.ParticipantRole.STUDENT;
    }
    
    /**
     * Get all participant IDs for a conversation
     */
    @Transactional(readOnly = true)
    public List<Long> getConversationParticipantIds(Long conversationId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversationId);
        return participants.stream()
                .map(p -> p.getUser().getId())
                .collect(Collectors.toList());
    }
    
    /**
     * Get all conversation IDs for a user
     */
    @Transactional(readOnly = true)
    public List<Long> getUserConversationIds(Long userId) {
        List<ConversationParticipant> participants = participantRepository.findByUserId(userId);
        return participants.stream()
                .map(p -> p.getConversation().getId())
                .distinct()
                .collect(Collectors.toList());
    }
}
