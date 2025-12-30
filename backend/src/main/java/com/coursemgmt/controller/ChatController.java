package com.coursemgmt.controller;

import com.coursemgmt.dto.*;
import com.coursemgmt.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.coursemgmt.security.services.UserDetailsImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> createConversation(@RequestBody CreateConversationRequest request) {
        Long currentUserId = getCurrentUserId();
        ConversationResponse response = chatService.createConversation(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        try {
            Long currentUserId = getCurrentUserId();
            List<ConversationResponse> conversations = chatService.getUserConversations(currentUserId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching conversations: " + e.getMessage(), e);
        }
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ConversationResponse conversation = chatService.getConversation(id, currentUserId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        Long currentUserId = getCurrentUserId();
        ChatMessageResponse response = chatService.sendMessage(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.data.domain.Page<ChatMessageResponse>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            org.springframework.data.domain.Page<ChatMessageResponse> messages = chatService.getMessages(id, currentUserId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching messages: " + e.getMessage(), e);
        }
    }

    @PutMapping("/messages/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponse> updateMessage(
            @PathVariable Long id,
            @RequestBody UpdateMessageRequest request) {
        Long currentUserId = getCurrentUserId();
        ChatMessageResponse response = chatService.updateMessage(id, currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/messages/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        chatService.deleteMessage(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        chatService.markAsRead(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/{id}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        Long count = chatService.getUnreadCount(id, currentUserId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/instructors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InstructorInfoDTO>> getEnrolledInstructors() {
        Long currentUserId = getCurrentUserId();
        List<InstructorInfoDTO> instructors = chatService.getEnrolledInstructors(currentUserId);
        return ResponseEntity.ok(instructors);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
