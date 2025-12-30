package com.coursemgmt.service;

import com.coursemgmt.dto.CertificateRequest;
import com.coursemgmt.dto.ChapterRequest;
import com.coursemgmt.dto.ChapterResponse;
import com.coursemgmt.dto.LessonRequest;
import com.coursemgmt.dto.LessonResponse;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import com.coursemgmt.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private UserProgressRepository userProgressRepository;
    @Autowired
    private CertificateService certificateService;

    private static final Logger logger = Logger.getLogger(ContentService.class.getName());

    // --- Quản lý Chapter ---

    @Transactional
    public Chapter createChapter(Long courseId, ChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setPosition(request.getPosition());
        chapter.setCourse(course);
        return chapterRepository.save(chapter);
    }

    @Transactional
    public Chapter updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found!"));

        chapter.setTitle(request.getTitle());
        chapter.setPosition(request.getPosition());
        return chapterRepository.save(chapter);
    }

    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found!"));
        chapterRepository.delete(chapter);
    }

    // --- Quản lý Lesson ---

    @Transactional
    public Lesson createLesson(Long chapterId, LessonRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found!"));

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDocumentUrl(request.getDocumentUrl());
        lesson.setSlideUrl(request.getSlideUrl());
        lesson.setContent(request.getContent());
        lesson.setPosition(request.getPosition());
        lesson.setDurationInMinutes(request.getDurationInMinutes());
        lesson.setIsPreview(request.getIsPreview() != null ? request.getIsPreview() : false);
        lesson.setChapter(chapter);

        return lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Long lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found!"));

        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDocumentUrl(request.getDocumentUrl());
        lesson.setSlideUrl(request.getSlideUrl());
        lesson.setContent(request.getContent());
        lesson.setPosition(request.getPosition());
        lesson.setDurationInMinutes(request.getDurationInMinutes());
        lesson.setIsPreview(request.getIsPreview() != null ? request.getIsPreview() : false);

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found!"));
        lessonRepository.delete(lesson);
    }

    // Helper method to get lessons for a chapter
    public List<Lesson> getChapterLessons(Long chapterId) {
        return lessonRepository.findByChapterIdOrderByPositionAsc(chapterId);
    }

    // Get lesson by ID
    public Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found!"));
    }

    // Reorder chapters
    @Transactional
    public void reorderChapters(Long courseId, Map<Long, Integer> chapterPositions) {
        for (Map.Entry<Long, Integer> entry : chapterPositions.entrySet()) {
            Chapter chapter = chapterRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Chapter not found: " + entry.getKey()));
            if (chapter.getCourse().getId().equals(courseId)) {
                chapter.setPosition(entry.getValue());
                chapterRepository.save(chapter);
            }
        }
    }

    // Reorder lessons in a chapter
    @Transactional
    public void reorderLessons(Long chapterId, Map<Long, Integer> lessonPositions) {
        for (Map.Entry<Long, Integer> entry : lessonPositions.entrySet()) {
            Lesson lesson = lessonRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Lesson not found: " + entry.getKey()));
            if (lesson.getChapter().getId().equals(chapterId)) {
                lesson.setPosition(entry.getValue());
                lessonRepository.save(lesson);
            }
        }
    }

    // --- Lấy nội dung (cho Học viên) ---

    // Lấy toàn bộ nội dung (chapters + lessons) của 1 khóa học
    public List<ChapterResponse> getCourseContent(Long courseId, UserDetailsImpl userDetails) {
        System.out.println("========================================");
        System.out.println("ContentService.getCourseContent called");
        System.out.println("Course ID: " + courseId);
        System.out.println("User ID: " + (userDetails != null ? userDetails.getId() : "NULL"));
        System.out.println("========================================");
        
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        System.out.println("User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("Course found: " + course.getTitle() + " (ID: " + course.getId() + ")");

        // Kiểm tra xem user có phải là chủ khóa học không (chỉ khóa học của chính họ)
        boolean isInstructor = course.getInstructor() != null && 
                               course.getInstructor().getId().equals(user.getId());
        System.out.println("Is Instructor (owner of this course): " + isInstructor);

        // Lấy thông tin Enrollment - Try multiple methods
        System.out.println("Attempting to find enrollment...");
        System.out.println("Method 1: findByUserAndCourse");
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course).orElse(null);
        
        // Fallback: Try by userId and courseId
        if (enrollment == null) {
            System.out.println("Method 1 failed. Trying Method 2: findByUserIdAndCourseId");
            enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId).orElse(null);
        }
        
        // Additional check: List all enrollments for this user to debug
        if (enrollment == null) {
            System.out.println("Method 2 also failed. Checking all enrollments for user...");
            List<Enrollment> allUserEnrollments = enrollmentRepository.findByUserIdWithCourse(user.getId());
            System.out.println("Total enrollments for user " + user.getId() + ": " + allUserEnrollments.size());
            for (Enrollment e : allUserEnrollments) {
                System.out.println("  - Enrollment ID: " + e.getId() + ", Course ID: " + e.getCourse().getId() + ", Course Title: " + e.getCourse().getTitle());
            }
        }
        
        System.out.println("Enrollment found: " + (enrollment != null ? "YES (ID: " + enrollment.getId() + ")" : "NO"));
        
        if (enrollment != null) {
            System.out.println("Enrollment Status: " + enrollment.getStatus());
            System.out.println("Enrollment Progress: " + enrollment.getProgress() + "%");
        }

        // Authorization logic:
        // - Cho phép nếu là instructor của khóa học này (chỉ khóa học của chính họ)
        // - Cho phép nếu đã enrolled (bất kỳ ai, kể cả giảng viên khác xem khóa học của giảng viên khác)
        // - Từ chối nếu không phải instructor và chưa enrolled
        if (!isInstructor && enrollment == null) {
            System.err.println("ERROR: User " + user.getId() + " is not the instructor of course " + courseId + " and is not enrolled");
            // Throw AccessDeniedException instead of RuntimeException to return 403 instead of 400
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này! Vui lòng đăng ký để xem nội dung.");
        }
        
        System.out.println("Access granted - proceeding to fetch content");
        System.out.println("========================================");
        
        // Lấy danh sách ID các bài học đã hoàn thành
        Set<Long> completedLessonIds = Set.of();
        if (enrollment != null) {
            System.out.println("Fetching user progress for enrollment ID: " + enrollment.getId());
            try {
                // Sử dụng JOIN FETCH để load lesson cùng lúc, tránh LazyInitializationException
                Set<User_Progress> progressSet = userProgressRepository.findByEnrollmentWithLesson(enrollment);
                System.out.println("Found " + progressSet.size() + " progress records");
                
                completedLessonIds = progressSet.stream()
                        .filter(progress -> {
                            if (progress == null) return false;
                            Boolean isCompleted = progress.getIsCompleted();
                            return isCompleted != null && isCompleted;
                        })
                        .filter(progress -> {
                            try {
                                return progress.getLesson() != null && progress.getLesson().getId() != null;
                            } catch (Exception e) {
                                System.err.println("ERROR accessing lesson from progress: " + e.getMessage());
                                e.printStackTrace();
                                return false;
                            }
                        })
                        .map(progress -> {
                            try {
                                return progress.getLesson().getId();
                            } catch (Exception e) {
                                System.err.println("ERROR getting lesson ID: " + e.getMessage());
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
                System.out.println("Completed lesson IDs: " + completedLessonIds.size());
            } catch (Exception e) {
                System.err.println("ERROR fetching user progress: " + e.getMessage());
                e.printStackTrace();
                // Continue with empty set if progress fetch fails
                completedLessonIds = Set.of();
            }
        }
        
        System.out.println("Final completed lesson IDs count: " + completedLessonIds.size());

        final Set<Long> finalCompletedLessonIds = completedLessonIds; // Cần final để dùng trong lambda

        // Lấy danh sách Chapters với lessons (sử dụng JOIN FETCH để tránh LazyInitializationException)
        System.out.println("Fetching chapters for course ID: " + courseId);
        List<Chapter> chapters;
        try {
            chapters = chapterRepository.findByCourseIdWithLessons(courseId);
            System.out.println("Found " + chapters.size() + " chapters");
            
            if (chapters.isEmpty()) {
                System.out.println("WARNING: No chapters found for course " + courseId);
                return List.of(); // Return empty list instead of throwing error
            }
        } catch (Exception e) {
            System.err.println("ERROR fetching chapters: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tải nội dung khóa học: " + e.getMessage(), e);
        }

        // Map sang DTO
        System.out.println("Mapping chapters to DTOs...");
        try {
            return chapters.stream().map(chapter -> {
                List<LessonResponse> lessonResponses = chapter.getLessons().stream()
                        .map(lesson -> {
                            boolean isCompleted = isInstructor || finalCompletedLessonIds.contains(lesson.getId());
                            return LessonResponse.fromEntity(lesson, isCompleted);
                        })
                        .collect(Collectors.toList());
                return ChapterResponse.fromEntity(chapter, lessonResponses);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("ERROR mapping chapters to DTOs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể xử lý nội dung khóa học: " + e.getMessage(), e);
        }
    }

    // --- Chức năng: Theo dõi tiến độ ---
    @Transactional
    public void markLessonAsCompleted(Long lessonId, UserDetailsImpl userDetails) {
        System.out.println("DEBUG: markLessonAsCompleted called for lessonId=" + lessonId + ", userId=" + userDetails.getId());
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + lessonId));
        
        // Safely get chapter and course with null checks
        Chapter chapter = lesson.getChapter();
        if (chapter == null) {
            throw new RuntimeException("Bài học chưa được gán vào chương nào!");
        }
        
        Course course = chapter.getCourse();
        if (course == null) {
            throw new RuntimeException("Chương chưa được gán vào khóa học nào!");
        }
        
        System.out.println("DEBUG: Found lesson '" + lesson.getTitle() + "' in course '" + course.getTitle() + "'");

        // Tìm enrollment của user cho khóa học này
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký khóa học này!"));
        
        System.out.println("DEBUG: Found enrollment ID=" + enrollment.getId());

        // Tìm hoặc tạo mới User_Progress
        User_Progress progress = userProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElse(new User_Progress());

        progress.setEnrollment(enrollment);
        progress.setLesson(lesson);
        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        userProgressRepository.save(progress);
        
        System.out.println("DEBUG: Marked lesson as completed, updating enrollment progress...");

        // Cập nhật lại % tiến độ tổng của Enrollment
        updateEnrollmentProgress(enrollment);
        
        System.out.println("DEBUG: markLessonAsCompleted completed successfully");
    }

    // --- Chức năng: Cập nhật tiến độ xem video (Auto-Progress) ---
    @Transactional
    public void updateLessonWatchTime(Long lessonId, Integer watchedTime, Integer totalDuration, UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found!"));
        Course course = lesson.getChapter().getCourse();

        // Tìm enrollment của user cho khóa học này
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký khóa học này!"));

        // Tìm hoặc tạo mới User_Progress
        User_Progress progress = userProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElse(new User_Progress());

        // Set enrollment and lesson if this is a new progress record
        if (progress.getEnrollment() == null) {
            progress.setEnrollment(enrollment);
            progress.setLesson(lesson);
        }

        // Update watched time and total duration
        progress.setLastWatchedTime(watchedTime);
        progress.setTotalDuration(totalDuration);

        // Calculate watch percentage
        double percent = (double) watchedTime / totalDuration;

        // Crucial Check: IF watched >= 90% AND not already completed -> Auto-complete
        if (percent >= 0.9 && !Boolean.TRUE.equals(progress.getIsCompleted())) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            
            // Trigger course-level progress recalculation
            updateEnrollmentProgress(enrollment);
        }

        // Save progress (whether completed or not)
        userProgressRepository.save(progress);
    }

    // Hàm private để tính toán lại tiến độ
    private void updateEnrollmentProgress(Enrollment enrollment) {
        long totalLessonsInCourse = lessonRepository.countByChapter_Course_Id(enrollment.getCourse().getId());
        if (totalLessonsInCourse == 0) {
            enrollment.setProgress(100.0);
            enrollment.setStatus(EEnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);
            
            // Auto-issue certificate for courses with no lessons
            autoIssueCertificate(enrollment);
            return;
        }

        long completedLessons = userProgressRepository.countByEnrollmentAndIsCompleted(enrollment, true);

        double progressPercentage = totalLessonsInCourse > 0 
            ? ((double) completedLessons / totalLessonsInCourse) * 100.0 
            : 100.0;
        
        // Round to 2 decimal places
        progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;
        
        System.out.println("Updating enrollment progress: " + completedLessons + " / " + totalLessonsInCourse + " = " + progressPercentage + "%");
        
        enrollment.setProgress(progressPercentage);

        if (progressPercentage >= 100.0) {
            enrollment.setStatus(EEnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);
            
            // Auto-issue certificate when course is completed
            autoIssueCertificate(enrollment);
        } else {
            enrollment.setStatus(EEnrollmentStatus.IN_PROGRESS);
            enrollmentRepository.save(enrollment);
        }
    }

    /**
     * Auto-issue certificate when enrollment reaches 100% completion
     * Check if certificate already exists BEFORE calling issueCertificate to avoid transaction rollback
     */
    private void autoIssueCertificate(Enrollment enrollment) {
        try {
            // Check if certificate already exists for this enrollment
            // This prevents the transaction from being marked for rollback
            boolean certificateExists = certificateService.existsByEnrollmentId(enrollment.getId());
            
            if (certificateExists) {
                logger.info("Certificate already exists for Enrollment ID: " + enrollment.getId() + " - Skipping auto-issue");
                return;
            }
            
            CertificateRequest certRequest = new CertificateRequest();
            certRequest.setEnrollmentId(enrollment.getId());
            
            certificateService.issueCertificate(certRequest);
            logger.info("Auto-issued certificate for Enrollment ID: " + enrollment.getId());
        } catch (Exception e) {
            // Log but don't fail the enrollment update
            logger.warning("Failed to auto-issue certificate for Enrollment ID: " + enrollment.getId() + " - Error: " + e.getMessage());
        }
    }
}