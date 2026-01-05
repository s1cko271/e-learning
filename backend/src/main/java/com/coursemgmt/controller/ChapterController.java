package com.coursemgmt.controller;

import com.coursemgmt.dto.ChapterRequest;
import com.coursemgmt.dto.ChapterResponse;
import com.coursemgmt.dto.LessonRequest;
import com.coursemgmt.dto.LessonResponse;
import com.coursemgmt.dto.MessageResponse;
import com.coursemgmt.model.Chapter;
import com.coursemgmt.model.Lesson;
import com.coursemgmt.security.services.CourseSecurityService;
import com.coursemgmt.security.services.UserDetailsImpl;
import com.coursemgmt.service.ContentService;
import com.coursemgmt.service.FileStorageService;
import com.coursemgmt.service.VideoDurationService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
public class ChapterController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private CourseSecurityService courseSecurityService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VideoDurationService videoDurationService;

    // 1. Lấy danh sách chapters của một course (cho instructor - không cần enrollment)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<List<ChapterResponse>> getChapters(@PathVariable Long courseId,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ChapterResponse> chapters = contentService.getCourseContent(courseId, userDetails);
        return ResponseEntity.ok(chapters);
    }

    // 2. Tạo chapter mới
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<?> createChapter(@PathVariable Long courseId,
                                          @Valid @RequestBody ChapterRequest request,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Chapter chapter = contentService.createChapter(courseId, request);
            // Create empty lessons list for new chapter
            ChapterResponse chapterResponse = ChapterResponse.fromEntity(chapter, List.of());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chapter created successfully");
            response.put("chapter", chapterResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating chapter: " + e.getMessage()));
        }
    }

    // 3. Cập nhật chapter
    @PutMapping("/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<?> updateChapter(@PathVariable Long courseId,
                                          @PathVariable Long chapterId,
                                          @Valid @RequestBody ChapterRequest request) {
        try {
            Chapter chapter = contentService.updateChapter(chapterId, request);
            // Load lessons for this chapter
            List<Lesson> lessons = contentService.getChapterLessons(chapterId);
            List<LessonResponse> lessonResponses = lessons.stream()
                    .map(lesson -> LessonResponse.fromEntity(lesson, false))
                    .toList();
            ChapterResponse chapterResponse = ChapterResponse.fromEntity(chapter, lessonResponses);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chapter updated successfully");
            response.put("chapter", chapterResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating chapter: " + e.getMessage()));
        }
    }

    // 4. Xóa chapter
    @DeleteMapping("/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<?> deleteChapter(@PathVariable Long courseId,
                                           @PathVariable Long chapterId) {
        try {
            contentService.deleteChapter(chapterId);
            return ResponseEntity.ok(new MessageResponse("Chapter deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error deleting chapter: " + e.getMessage()));
        }
    }

    // 5. Tạo lesson mới trong chapter
    @PostMapping("/{chapterId}/lessons")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<?> createLesson(@PathVariable Long courseId,
                                          @PathVariable Long chapterId,
                                          @Valid @RequestBody LessonRequest request) {
        try {
            Lesson lesson = contentService.createLesson(chapterId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lesson created successfully");
            response.put("lesson", LessonResponse.fromEntity(lesson, false));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating lesson: " + e.getMessage()));
        }
    }

    // 6. Cập nhật lesson
    @PutMapping("/{chapterId}/lessons/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> updateLesson(@PathVariable Long courseId,
                                         @PathVariable Long chapterId,
                                         @PathVariable Long lessonId,
                                         @Valid @RequestBody LessonRequest request) {
        try {
            Lesson lesson = contentService.updateLesson(lessonId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lesson updated successfully");
            response.put("lesson", LessonResponse.fromEntity(lesson, false));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating lesson: " + e.getMessage()));
        }
    }

    // 7. Xóa lesson
    @DeleteMapping("/{chapterId}/lessons/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> deleteLesson(@PathVariable Long courseId,
                                          @PathVariable Long chapterId,
                                          @PathVariable Long lessonId) {
        try {
            contentService.deleteLesson(lessonId);
            return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error deleting lesson: " + e.getMessage()));
        }
    }

    // 8. Upload video file cho lesson
    @PostMapping("/{chapterId}/lessons/{lessonId}/upload-video")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> uploadLessonVideo(@PathVariable Long courseId,
                                               @PathVariable Long chapterId,
                                               @PathVariable Long lessonId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "durationInSeconds", required = false) Integer durationInSeconds) {
        try {
            System.out.println("========================================");
            System.out.println("Upload Video Request");
            System.out.println("Course ID: " + courseId);
            System.out.println("Chapter ID: " + chapterId);
            System.out.println("Lesson ID: " + lessonId);
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());
            System.out.println("Duration in seconds: " + durationInSeconds);
            System.out.println("========================================");
            
            String videoUrl = fileStorageService.storeLessonVideo(file, lessonId);
            System.out.println("Video stored at: " + videoUrl);
            
            // Update lesson with video URL
            Lesson lesson = contentService.getLessonById(lessonId);
            LessonRequest updateRequest = new LessonRequest();
            updateRequest.setTitle(lesson.getTitle());
            updateRequest.setContentType(lesson.getContentType());
            updateRequest.setVideoUrl(videoUrl);
            updateRequest.setDocumentUrl(lesson.getDocumentUrl());
            updateRequest.setContent(lesson.getContent());
            updateRequest.setPosition(lesson.getPosition());
            
            // Tự động tính duration nếu frontend gửi durationInSeconds
            if (durationInSeconds != null && durationInSeconds > 0) {
                Integer roundedDuration = videoDurationService.roundDurationToMinutes(durationInSeconds);
                updateRequest.setDurationInMinutes(roundedDuration);
            } else {
                updateRequest.setDurationInMinutes(lesson.getDurationInMinutes());
            }
            
            Lesson updatedLesson = contentService.updateLesson(lessonId, updateRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Video uploaded successfully");
            response.put("videoUrl", videoUrl);
            response.put("lesson", LessonResponse.fromEntity(updatedLesson, false));
            System.out.println("Video upload completed successfully");
            System.out.println("========================================");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("ERROR uploading video:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "None"));
            e.printStackTrace();
            System.err.println("========================================");
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading video: " + e.getMessage()));
        }
    }

    // 9. Extract duration từ YouTube URL
    @GetMapping("/extract-youtube-duration")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> extractYouTubeDuration(@RequestParam("url") String youtubeUrl) {
        try {
            if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("URL không được để trống"));
            }
            
            Integer durationInMinutes = videoDurationService.getYouTubeDurationInMinutes(youtubeUrl.trim());
            if (durationInMinutes == null || durationInMinutes <= 0) {
                // Trả về 200 với duration = null thay vì 400 để frontend có thể xử lý gracefully
                Map<String, Object> response = new HashMap<>();
                response.put("durationInMinutes", null);
                response.put("message", "Không thể lấy thời lượng từ YouTube URL. Vui lòng kiểm tra URL hoặc API key.");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("durationInMinutes", durationInMinutes);
            response.put("message", "Đã lấy thời lượng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error extracting YouTube duration: " + e.getMessage());
            e.printStackTrace();
            // Trả về 200 với duration = null thay vì 500 để frontend có thể xử lý gracefully
            Map<String, Object> response = new HashMap<>();
            response.put("durationInMinutes", null);
            response.put("message", "Lỗi khi lấy thời lượng: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // 10. Upload document file cho lesson
    @PostMapping("/{chapterId}/lessons/{lessonId}/upload-document")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> uploadLessonDocument(@PathVariable Long courseId,
                                                  @PathVariable Long chapterId,
                                                  @PathVariable Long lessonId,
                                                  @RequestParam("file") MultipartFile file) {
        try {
            String documentUrl = fileStorageService.storeLessonDocument(file, lessonId);
            
            // Update lesson with document URL
            Lesson lesson = contentService.getLessonById(lessonId);
            LessonRequest updateRequest = new LessonRequest();
            updateRequest.setTitle(lesson.getTitle());
            updateRequest.setContentType(lesson.getContentType());
            updateRequest.setVideoUrl(lesson.getVideoUrl());
            updateRequest.setDocumentUrl(documentUrl);
            updateRequest.setContent(lesson.getContent());
            updateRequest.setPosition(lesson.getPosition());
            updateRequest.setDurationInMinutes(lesson.getDurationInMinutes());
            
            Lesson updatedLesson = contentService.updateLesson(lessonId, updateRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document uploaded successfully");
            response.put("documentUrl", documentUrl);
            response.put("lesson", LessonResponse.fromEntity(updatedLesson, false));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading document: " + e.getMessage()));
        }
    }

    // 10. Upload slide file cho lesson
    @PostMapping("/{chapterId}/lessons/{lessonId}/upload-slide")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> uploadLessonSlide(@PathVariable Long courseId,
                                               @PathVariable Long chapterId,
                                               @PathVariable Long lessonId,
                                               @RequestParam("file") MultipartFile file) {
        try {
            String slideUrl = fileStorageService.storeLessonSlide(file, lessonId);
            
            // Update lesson with slide URL
            Lesson lesson = contentService.getLessonById(lessonId);
            LessonRequest updateRequest = new LessonRequest();
            updateRequest.setTitle(lesson.getTitle());
            updateRequest.setContentType(lesson.getContentType());
            updateRequest.setVideoUrl(lesson.getVideoUrl());
            updateRequest.setDocumentUrl(lesson.getDocumentUrl());
            updateRequest.setSlideUrl(slideUrl);
            updateRequest.setContent(lesson.getContent());
            updateRequest.setPosition(lesson.getPosition());
            updateRequest.setDurationInMinutes(lesson.getDurationInMinutes());
            
            Lesson updatedLesson = contentService.updateLesson(lessonId, updateRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Slide uploaded successfully");
            response.put("slideUrl", slideUrl);
            response.put("lesson", LessonResponse.fromEntity(updatedLesson, false));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading slide: " + e.getMessage()));
        }
    }

    // 11. Cập nhật thứ tự chapters
    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<?> reorderChapters(@PathVariable Long courseId,
                                             @RequestBody Map<Long, Integer> chapterPositions) {
        try {
            contentService.reorderChapters(courseId, chapterPositions);
            return ResponseEntity.ok(new MessageResponse("Chapters reordered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error reordering chapters: " + e.getMessage()));
        }
    }

    // 11. Cập nhật thứ tự lessons trong chapter
    @PatchMapping("/{chapterId}/lessons/reorder")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<?> reorderLessons(@PathVariable Long courseId,
                                           @PathVariable Long chapterId,
                                           @RequestBody Map<Long, Integer> lessonPositions) {
        try {
            contentService.reorderLessons(chapterId, lessonPositions);
            return ResponseEntity.ok(new MessageResponse("Lessons reordered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error reordering lessons: " + e.getMessage()));
        }
    }
}

