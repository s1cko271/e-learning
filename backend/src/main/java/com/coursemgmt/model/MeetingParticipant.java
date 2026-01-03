package com.coursemgmt.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_participants", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
@Data
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EMeetingParticipantRole role = EMeetingParticipantRole.PARTICIPANT;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_audio_enabled")
    private Boolean isAudioEnabled = true;

    @Column(name = "is_video_enabled")
    private Boolean isVideoEnabled = true;

    @Column(name = "is_screen_sharing")
    private Boolean isScreenSharing = false;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}

