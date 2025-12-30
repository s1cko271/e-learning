package com.coursemgmt.repository;

import com.coursemgmt.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a course
    Page<Review> findByCourseId(Long courseId, Pageable pageable);
    
    List<Review> findByCourseId(Long courseId);

    // Find review by user and course (to check if user already reviewed)
    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);

    // Check if user already reviewed a course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Count reviews for a course
    Long countByCourseId(Long courseId);

    // Calculate average rating for a course
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);

    // Calculate average rating for all courses of an instructor
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.instructor.id = :instructorId")
    Double getAverageRatingByInstructorId(@Param("instructorId") Long instructorId);

    // Count total reviews for all courses of an instructor
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.instructor.id = :instructorId")
    Long countByInstructorId(@Param("instructorId") Long instructorId);

    // Find all reviews by user
    Page<Review> findByUserId(Long userId, Pageable pageable);

    // Get rating distribution for a course (count per rating)
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.course.id = :courseId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("courseId") Long courseId);

    // Find all reviews for courses of an instructor
    @Query("SELECT r FROM Review r WHERE r.course.instructor.id = :instructorId ORDER BY r.createdAt DESC")
    Page<Review> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    // Count unreplied reviews for instructor
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.instructor.id = :instructorId AND r.instructorReply IS NULL")
    Long countUnrepliedByInstructorId(@Param("instructorId") Long instructorId);

    // Find recent reviews for instructor (for notifications)
    @Query("SELECT r FROM Review r WHERE r.course.instructor.id = :instructorId ORDER BY r.createdAt DESC")
    List<Review> findRecentByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    // Delete all reviews for a course
    @Modifying
    @Query("DELETE FROM Review r WHERE r.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}

