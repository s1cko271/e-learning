package com.coursemgmt.repository;

import com.coursemgmt.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy tất cả thông báo của user, sắp xếp theo thời gian mới nhất
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Đếm số thông báo chưa đọc
    Long countByUserIdAndIsReadFalse(Long userId);
    
    // Lấy thông báo chưa đọc
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Đánh dấu tất cả thông báo là đã đọc
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    // Xóa tất cả notifications liên quan đến một course
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}

