package com.coursemgmt.repository;

import com.coursemgmt.model.MeetingMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingMessageRepository extends JpaRepository<MeetingMessage, Long> {

    List<MeetingMessage> findByMeetingIdOrderByCreatedAtAsc(Long meetingId);

    Page<MeetingMessage> findByMeetingIdOrderByCreatedAtDesc(Long meetingId, Pageable pageable);

    @Query("SELECT mm FROM MeetingMessage mm WHERE mm.meeting.id = :meetingId ORDER BY mm.createdAt ASC")
    List<MeetingMessage> findMessagesByMeetingIdOrderByCreatedAt(Long meetingId);
}

