package com.coursemgmt.controller;

import com.coursemgmt.dto.CourseRequest;
import com.coursemgmt.dto.CourseResponse;
import com.coursemgmt.dto.CourseStatisticsResponse;
import com.coursemgmt.dto.CourseAnalyticsResponse;
import com.coursemgmt.dto.MessageResponse;
import com.coursemgmt.model.Course;
import com.coursemgmt.repository.CourseRepository;
import com.coursemgmt.security.services.UserDetailsImpl;
import com.coursemgmt.service.CourseService;
import com.coursemgmt.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CourseRepository courseRepository;

    // Valid sort fields for Course entity
    private static final Set<String> VALID_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "id", "title", "description", "price", "imageUrl", "totalDurationInHours",
            "status", "createdAt", "updatedAt"
    ));

    /**
     * Sanitize sort parameter to prevent 400 errors from invalid field names
     * @param sort Original sort string (e.g., "enrollmentCount,desc")
     * @return Sanitized sort string with valid field name (e.g., "createdAt,desc")
     */
    private String sanitizeSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return "createdAt,desc";
        }

        String[] parts = sort.split(",");
        String fieldName = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "desc";

        // Check if field name is valid
        if (!VALID_SORT_FIELDS.contains(fieldName)) {
            // Replace invalid field with default (createdAt)
            return "createdAt," + direction;
        }

        return fieldName + "," + direction;
    }

    // 1. Tạo khóa học (Admin, Giảng viên)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Course course = courseService.createCourse(request, userDetails);
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    // 2. Cập nhật khóa học (Admin hoặc Giảng viên sở hữu)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #id)")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                       @Valid @RequestBody CourseRequest request) {
        Course updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(CourseResponse.fromEntity(updatedCourse));
    }

    // 2.1. Upload ảnh bìa khóa học
    @PostMapping(value = "/{id}/image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #id)")
    public ResponseEntity<?> uploadCourseImage(@PathVariable Long id,
                                                @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("========================================");
            System.out.println("Upload Course Image Request");
            System.out.println("Course ID: " + id);
            
            // Validate file
            if (file == null || file.isEmpty()) {
                System.err.println("File is null or empty!");
                return ResponseEntity.badRequest().body(new MessageResponse("File is required and cannot be empty"));
            }
            
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            System.out.println("File is empty: " + file.isEmpty());
            System.out.println("========================================");

            // Validate course exists (getCourseById throws exception if not found)
            CourseResponse courseResponse = courseService.getCourseById(id);
            System.out.println("Course found: " + courseResponse.getTitle());

            // Store the image file
            String imageUrl = fileStorageService.storeCourseImage(file, id);
            System.out.println("Image stored at: " + imageUrl);

            // Update course with new image URL
            // Get current course entity to preserve other fields
            Course currentCourse = courseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found!"));
            
            CourseRequest updateRequest = new CourseRequest();
            updateRequest.setTitle(currentCourse.getTitle());
            updateRequest.setDescription(currentCourse.getDescription());
            updateRequest.setPrice(currentCourse.getPrice());
            if (currentCourse.getCategory() != null && currentCourse.getCategory().getId() != null) {
                updateRequest.setCategoryId(currentCourse.getCategory().getId());
            } else {
                throw new RuntimeException("Course category is missing!");
            }
            updateRequest.setImageUrl(imageUrl);
            if (currentCourse.getTotalDurationInHours() != null) {
                updateRequest.setTotalDurationInHours(currentCourse.getTotalDurationInHours());
            }
            
            Course updatedCourse = courseService.updateCourse(id, updateRequest);
            System.out.println("Course updated successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course image uploaded successfully");
            response.put("imageUrl", imageUrl);
            response.put("course", CourseResponse.fromEntity(updatedCourse));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error uploading course image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading course image: " + e.getMessage()));
        }
    }

    // 3. Xóa khóa học (Admin hoặc Giảng viên)
    @DeleteMapping("/{id}")
    // Cho phép Admin hoặc bất kỳ Giảng viên nào xóa khóa học.
    // Lý do: logic kiểm tra sở hữu dễ gây lỗi khi chuyển quyền hoặc dữ liệu không đồng bộ.
    // UI đã giới hạn chỉ hiển thị khóa học của chính giảng viên trong trang "Khóa học của tôi".
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(new MessageResponse("Course deleted successfully!"));
    }

    // 4. Duyệt khóa học (Admin)
    @PatchMapping("/{id}/approve") // Dùng PATCH vì chỉ cập nhật 1 phần (status)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable Long id) {
        Course approvedCourse = courseService.approveCourse(id);
        return ResponseEntity.ok(CourseResponse.fromEntity(approvedCourse));
    }

    // 5. Lấy chi tiết 1 khóa học (Public)
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }


    // 5.2. Lấy danh sách khóa học nổi bật (Featured Courses - Public)
    @GetMapping("/featured")
    public ResponseEntity<List<CourseResponse>> getFeaturedCourses() {
        List<CourseResponse> featuredCourses = courseService.getFeaturedCourses();
        return ResponseEntity.ok(featuredCourses);
    }

    // 6. Tìm kiếm, lọc, sắp xếp khóa học (Public)
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @RequestParam(required = false) String keyword, // Từ khóa tìm kiếm
            @RequestParam(required = false) Long categoryId, // Lọc theo danh mục
            @RequestParam(required = false) Double minPrice, // Giá tối thiểu
            @RequestParam(required = false) Double maxPrice, // Giá tối đa
            @RequestParam(required = false) Boolean isFree, // Lọc khóa học miễn phí
            @RequestParam(required = false) Boolean isPaid, // Lọc khóa học có phí
            @RequestParam(required = false) String level, // Lọc theo cấp độ (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
            @RequestParam(required = false) Double minRating, // Đánh giá tối thiểu
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort // Sắp xếp
    ) {
        // Sanitize sort parameter to prevent 400 errors from invalid field names
        String sanitizedSort = sanitizeSort(sort);
        Page<CourseResponse> courses = courseService.getAllPublishedCourses(keyword, categoryId, minPrice, maxPrice, isFree, isPaid, level, minRating, page, size, sanitizedSort);
        return ResponseEntity.ok(courses);
    }

    // 7. Thống kê (Admin hoặc Giảng viên sở hữu)
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #id)")
    public ResponseEntity<CourseStatisticsResponse> getCourseStatistics(@PathVariable Long id) {
        CourseStatisticsResponse stats = courseService.getCourseStatistics(id);
        return ResponseEntity.ok(stats);
    }
    
    // 8. Analytics chi tiết (Admin hoặc Giảng viên sở hữu)
    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or @courseSecurityService.isInstructor(authentication, #id)")
    public ResponseEntity<CourseAnalyticsResponse> getCourseAnalytics(@PathVariable Long id) {
        CourseAnalyticsResponse analytics = courseService.getCourseAnalytics(id);
        return ResponseEntity.ok(analytics);
    }

    // 8. Giảng viên gửi yêu cầu phê duyệt khóa học
    @PostMapping("/{id}/request-approval")
    @PreAuthorize("hasRole('LECTURER') and @courseSecurityService.isInstructor(authentication, #id)")
    public ResponseEntity<CourseResponse> requestCourseApproval(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Course approvedCourse = courseService.requestApproval(id, userDetails);
        return ResponseEntity.ok(CourseResponse.fromEntity(approvedCourse));
    }

    // 9. Giảng viên tự publish khóa học (Marketplace Model - Self-Publish)
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> publishCourse(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Course publishedCourse = courseService.publishCourse(id, userDetails);
        return ResponseEntity.ok(CourseResponse.fromEntity(publishedCourse));
    }

    // 10. Giảng viên gỡ khóa học (Unpublish) - PUBLISHED -> DRAFT
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> unpublishCourse(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Course unpublishedCourse = courseService.unpublishCourse(id, userDetails);
        return ResponseEntity.ok(CourseResponse.fromEntity(unpublishedCourse));
    }

    // 10. Đánh dấu khóa học là nổi bật (Featured) - Chỉ Admin
    @PatchMapping("/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> toggleFeatured(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "true") Boolean isFeatured) {
        Course course = courseService.toggleFeatured(id, isFeatured);
        return ResponseEntity.ok(CourseResponse.fromEntity(course));
    }

    // 11. Lấy danh sách khóa học của học viên (My Courses)
    @GetMapping("/my-courses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseResponse>> getMyCourses(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        System.out.println("========================================");
        System.out.println("GetMyCourses: Request received for user ID: " + userDetails.getId());
        
        List<CourseResponse> courses = courseService.getMyCourses(userDetails.getId());
        
        System.out.println("GetMyCourses: Found " + courses.size() + " courses for user " + userDetails.getId());
        if (courses.size() > 0) {
            System.out.println("GetMyCourses: Course IDs: " + 
                courses.stream().map(CourseResponse::getId).toList());
        }
        System.out.println("========================================");
        
        return ResponseEntity.ok(courses);
    }
}