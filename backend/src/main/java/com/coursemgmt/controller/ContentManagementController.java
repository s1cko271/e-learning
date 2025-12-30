package com.coursemgmt.controller;

import com.coursemgmt.dto.ChapterRequest;
import com.coursemgmt.dto.LessonRequest;
import com.coursemgmt.dto.LessonResponse;
import com.coursemgmt.dto.MessageResponse;
import com.coursemgmt.model.Chapter;
import com.coursemgmt.model.Lesson;
import com.coursemgmt.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.coursemgmt.service.ExcelService; // Thêm import
import org.springframework.core.io.InputStreamResource; // Thêm import
import org.springframework.core.io.Resource; // Thêm import
import org.springframework.http.HttpHeaders; // Thêm import
import org.springframework.http.MediaType; // Thêm import
import org.springframework.web.multipart.MultipartFile; // Thêm import
import java.io.ByteArrayInputStream;
import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/manage/content") // API quản lý
public class ContentManagementController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ExcelService excelService; // Thêm ExcelService

    // --- API QUẢN LÝ CHAPTER ---

    @PostMapping("/courses/{courseId}/chapters")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<Chapter> createChapter(@PathVariable Long courseId,
                                                 @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(contentService.createChapter(courseId, request));
    }

    @PutMapping("/chapters/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long chapterId,
                                                 @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(contentService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/chapters/{chapterId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<MessageResponse> deleteChapter(@PathVariable Long chapterId) {
        contentService.deleteChapter(chapterId);
        return ResponseEntity.ok(new MessageResponse("Chapter deleted successfully!"));
    }

    // --- API QUẢN LÝ LESSON ---

    @PostMapping("/chapters/{chapterId}/lessons")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfChapter(authentication, #chapterId)")
    public ResponseEntity<Lesson> createLesson(@PathVariable Long chapterId,
                                               @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(contentService.createLesson(chapterId, request));
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long lessonId,
                                               @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(contentService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<MessageResponse> deleteLesson(@PathVariable Long lessonId) {
        contentService.deleteLesson(lessonId);
        return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully!"));
    }

    // API để giảng viên preview bài học trước khi publish
    @GetMapping("/lessons/{lessonId}/preview")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructorOfLesson(authentication, #lessonId)")
    public ResponseEntity<?> previewLesson(@PathVariable Long lessonId) {
        try {
            Lesson lesson = contentService.getLessonById(lessonId);
            return ResponseEntity.ok(LessonResponse.fromEntity(lesson, false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Lesson not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error previewing lesson: " + e.getMessage()));
        }
    }

    // --- API IMPORT/EXPORT MỚI ---

    /**
     * API Export nội dung khóa học ra file Excel
     *
     */
    @GetMapping("/courses/{courseId}/export")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<Resource> exportLessons(@PathVariable Long courseId) throws IOException {
        ByteArrayInputStream in = excelService.exportLessons(courseId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=NoiDungKhoaHoc_" + courseId + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    /**
     * API Import nội dung khóa học từ file Excel
     *
     */
    @PostMapping("/courses/{courseId}/import")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #courseId)")
    public ResponseEntity<MessageResponse> importLessons(@PathVariable Long courseId,
                                                         @RequestParam("file") MultipartFile file) {
        try {
            excelService.importLessons(courseId, file);
            return ResponseEntity.ok(new MessageResponse("Import nội dung thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi khi import: " + e.getMessage()));
        }
    }
}