package com.coursemgmt.repository;

import com.coursemgmt.model.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByMeetingId(Long meetingId);

    List<MeetingParticipant> findByUserId(Long userId);

    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

    @Query("SELECT mp FROM MeetingParticipant mp WHERE mp.meeting.id = :meetingId AND mp.leftAt IS NULL")
    List<MeetingParticipant> findActiveParticipantsByMeetingId(Long meetingId);

    @Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.meeting.id = :meetingId AND mp.leftAt IS NULL")
    Long countActiveParticipantsByMeetingId(Long meetingId);
}

