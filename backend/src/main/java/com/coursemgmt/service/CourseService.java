package com.coursemgmt.service;

import com.coursemgmt.dto.CourseRequest;
import com.coursemgmt.dto.CourseResponse;
import com.coursemgmt.dto.CourseStatisticsResponse;
import com.coursemgmt.dto.CourseAnalyticsResponse;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.model.EEnrollmentStatus;
import com.coursemgmt.repository.CategoryRepository;
import com.coursemgmt.repository.CourseRepository;
import com.coursemgmt.repository.EnrollmentRepository;
import com.coursemgmt.repository.ReviewRepository;
import com.coursemgmt.repository.UserRepository;
import com.coursemgmt.repository.ChapterRepository;
import com.coursemgmt.repository.LessonRepository;
import com.coursemgmt.repository.TransactionRepository;
import com.coursemgmt.repository.CartItemRepository;
import com.coursemgmt.repository.NotificationRepository;
import com.coursemgmt.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NewsletterService newsletterService;

    // Hàm chung để lấy User từ security context
    private User getCurrentUser(UserDetailsImpl userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    /**
     * Helper method: Get current authenticated user ID from SecurityContext
     * Returns null if user is not authenticated
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return null;
            }
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username).orElse(null);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            System.out.println("CourseService.getCurrentUserId: User not authenticated or error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method: Get enrolled course IDs for a user
     * Returns empty set if user is not authenticated or not found
     */
    private Set<Long> getEnrolledCourseIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        try {
            Set<Long> enrolledIds = enrollmentRepository.findEnrolledCourseIdsByUserId(userId);
            System.out.println("CourseService.getEnrolledCourseIds: User " + userId + " has " + 
                             (enrolledIds != null ? enrolledIds.size() : 0) + " enrolled courses");
            return enrolledIds != null ? enrolledIds : Collections.emptySet();
        } catch (Exception e) {
            System.err.println("CourseService.getEnrolledCourseIds: Error fetching enrolled courses: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Helper method: Get enrollment map (courseId -> Enrollment) for a user
     * Returns empty map if user is not authenticated or not found
     * Used to get enrollment progress and status for enrolled courses
     */
    private Map<Long, Enrollment> getEnrollmentMap(Long userId) {
        if (userId == null) {
            return Collections.emptyMap();
        }
        try {
            // Use findByUserIdWithCourse to eagerly fetch course to avoid LazyInitializationException
            List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(userId);
            if (enrollments == null || enrollments.isEmpty()) {
                return Collections.emptyMap();
            }
            return enrollments.stream()
                    .filter(e -> e.getCourse() != null) // Filter out enrollments with null course
                    .collect(Collectors.toMap(
                            e -> e.getCourse().getId(),
                            e -> e,
                            (e1, e2) -> e1 // If duplicate, keep first one
                    ));
        } catch (Exception e) {
            System.err.println("CourseService.getEnrollmentMap: Error fetching enrollments: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    // Chức năng 1: Tạo khóa học
    @Transactional
    public Course createCourse(CourseRequest request, UserDetailsImpl userDetails) {
        User instructor = getCurrentUser(userDetails);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found!"));

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setImageUrl(request.getImageUrl());
        course.setTotalDurationInHours(request.getTotalDurationInHours());
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        // Phân quyền: Admin tạo thì PUBLISHED luôn, Giảng viên tạo thì DRAFT (Marketplace Model)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        if (isAdmin) {
            course.setStatus(ECourseStatus.PUBLISHED);
        } else {
            // Giảng viên tạo sẽ ở trạng thái DRAFT để có thể chỉnh sửa trước khi publish
            course.setStatus(ECourseStatus.DRAFT);
        }

        Course savedCourse = courseRepository.save(course);

        // Gửi email thông báo khóa học mới nếu khóa học được publish ngay (Admin tạo)
        if (savedCourse.getStatus() == ECourseStatus.PUBLISHED) {
            try {
                String courseUrl = "http://localhost:3000/courses/" + savedCourse.getId();
                newsletterService.sendNewCourseNotification(savedCourse.getTitle(), courseUrl);
                System.out.println(">>> Email notification sent for new course: " + savedCourse.getTitle());
            } catch (Exception e) {
                System.err.println(">>> ERROR: Failed to send email notification for new course: " + e.getMessage());
                e.printStackTrace();
                // Không throw exception để không ảnh hưởng đến việc tạo khóa học
            }
        }

        return savedCourse;
    }

    // Chức năng 2: Cập nhật khóa học
    @Transactional
    public Course updateCourse(Long courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found!"));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setImageUrl(request.getImageUrl());
        course.setTotalDurationInHours(request.getTotalDurationInHours());
        course.setCategory(category);
        course.setUpdatedAt(LocalDateTime.now());
        // Khi cập nhật, có thể reset status về PENDING để admin duyệt lại
        // course.setStatus(ECourseStatus.PENDING_APPROVAL);

        return courseRepository.save(course);
    }

    // Chức năng: Gửi yêu cầu phê duyệt (Giảng viên)
    @Transactional
    public Course requestApproval(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        // Chỉ cho phép gửi yêu cầu nếu khóa học đang ở trạng thái DRAFT hoặc đã bị từ chối
        // Nếu đã PUBLISHED thì không cần gửi lại
        if (course.getStatus() == ECourseStatus.PUBLISHED) {
            throw new RuntimeException("Khóa học đã được phê duyệt rồi!");
        }

        // Đặt trạng thái về PENDING_APPROVAL để chờ Admin duyệt
        course.setStatus(ECourseStatus.PENDING_APPROVAL);
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    // Chức năng 3: Xóa khóa học
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));
        
        // Xóa các bản ghi liên quan trước khi xóa khóa học (để tránh foreign key constraint violation)
        
        // 1. Xóa reviews
        reviewRepository.deleteByCourseId(courseId);
        
        // 2. Xóa cart items
        cartItemRepository.deleteByCourseId(courseId);
        
        // 3. Xóa notifications liên quan đến khóa học
        notificationRepository.deleteByCourseId(courseId);
        
        // 4. Xóa enrollments (sẽ tự động xóa User_Progress và Certificates do cascade)
        enrollmentRepository.deleteByCourseId(courseId);
        
        // 5. Xóa transactions
        transactionRepository.deleteByCourseId(courseId);
        
        // 6. Xóa khóa học (chapters và lessons sẽ tự động xóa do cascade = CascadeType.ALL)
        courseRepository.delete(course);
    }

    // Chức năng 3.1: Chuyển quyền sở hữu khóa học (Admin only)
    @Transactional
    public Course transferCourseOwnership(Long courseId, Long newInstructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));
        
        User newInstructor = userRepository.findById(newInstructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found!"));
        
        // Kiểm tra user có phải là giảng viên không
        boolean isLecturer = newInstructor.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_LECTURER);
        
        if (!isLecturer) {
            throw new RuntimeException("User is not a lecturer!");
        }
        
        course.setInstructor(newInstructor);
        course.setUpdatedAt(LocalDateTime.now());
        
        return courseRepository.save(course);
    }

    // Chức năng 4: Admin duyệt khóa học
    @Transactional
    public Course approveCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        course.setStatus(ECourseStatus.PUBLISHED);
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // Chức năng 5: Lấy 1 khóa học
    public CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        CourseResponse dto = CourseResponse.fromEntity(course);
        
        // Tính enrollmentCount từ repository (tránh LAZY loading issue)
        Long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        dto.setEnrollmentCount(enrollmentCount != null ? enrollmentCount : 0L);
        
        // Set rating info
        Double avgRating = reviewRepository.getAverageRatingByCourseId(courseId);
        Long reviewCount = reviewRepository.countByCourseId(courseId);
        dto.setRating(avgRating != null ? avgRating : 0.0);
        dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
        
        // Check if current user is enrolled
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
            dto.setIsEnrolled(isEnrolled);
            System.out.println("CourseService.getCourseById: User " + currentUserId + 
                             " enrolled in course " + courseId + ": " + isEnrolled);
        } else {
            dto.setIsEnrolled(false);
        }
        
        return dto;
    }

    // Chức năng 5.1: Lấy danh sách khóa học nổi bật (Featured Courses)
    public List<CourseResponse> getFeaturedCourses() {
        // Lấy các khóa học được đánh dấu là featured và đã published (sử dụng @Query explicit)
        List<Course> featuredCourses = courseRepository.findFeaturedCourses();
        
        // Nếu không có khóa học nào được đánh dấu featured, fallback về 4 khóa học mới nhất đã published
        if (featuredCourses == null || featuredCourses.isEmpty()) {
            // Fallback: Get top 4 latest published courses if no featured ones found
            Pageable pageable = PageRequest.of(0, 4);
            featuredCourses = courseRepository.findLatestPublishedCourses(pageable);
        }
        
        // Get enrolled course IDs and enrollment map for current user (if authenticated)
        Long currentUserId = getCurrentUserId();
        Set<Long> enrolledIds = getEnrolledCourseIds(currentUserId);
        Map<Long, Enrollment> enrollmentMap = getEnrollmentMap(currentUserId);
        
        // Convert sang DTO và tính enrollmentCount + isEnrolled + enrollmentProgress + rating
        return featuredCourses.stream()
                .map(course -> {
                    CourseResponse dto = CourseResponse.fromEntity(course);
                    // Tính enrollmentCount từ repository (tránh LAZY loading issue)
                    Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
                    dto.setEnrollmentCount(enrollmentCount != null ? enrollmentCount : 0L);
                    
                    // Set rating info
                    Double avgRating = reviewRepository.getAverageRatingByCourseId(course.getId());
                    Long reviewCount = reviewRepository.countByCourseId(course.getId());
                    dto.setRating(avgRating != null ? avgRating : 0.0);
                    dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
                    
                    // Set isEnrolled status
                    boolean isEnrolled = enrolledIds.contains(course.getId());
                    dto.setIsEnrolled(isEnrolled);
                    
                    // If enrolled, set enrollment progress and status
                    if (isEnrolled) {
                        Enrollment enrollment = enrollmentMap.get(course.getId());
                        if (enrollment != null) {
                            if (enrollment.getProgress() != null) {
                                dto.setEnrollmentProgress(enrollment.getProgress());
                            } else {
                                dto.setEnrollmentProgress(0.0);
                            }
                            if (enrollment.getStatus() != null) {
                                dto.setEnrollmentStatus(enrollment.getStatus().name());
                            } else {
                                dto.setEnrollmentStatus("IN_PROGRESS");
                            }
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Chức năng 6.1: Lấy tất cả khóa học cho Admin (không filter theo published)
    @Transactional(readOnly = true)
    public Page<Course> getAllCoursesForAdmin(Pageable pageable, String search, String status) {
        // Use custom query with JOIN FETCH to eager load instructor and category
        // This avoids LAZY loading issues when converting to DTO
        String baseQuery = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.instructor LEFT JOIN FETCH c.category WHERE 1=1";
        String countQuery = "SELECT COUNT(DISTINCT c) FROM Course c WHERE 1=1";
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;
        
        // Search by title
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("LOWER(c.title) LIKE LOWER(?" + paramIndex + ")");
            params.add("%" + search.trim() + "%");
            paramIndex++;
        }
        
        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            try {
                ECourseStatus courseStatus = ECourseStatus.valueOf(status.toUpperCase());
                conditions.add("c.status = ?" + paramIndex);
                params.add(courseStatus);
                paramIndex++;
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        // Build final queries
        if (!conditions.isEmpty()) {
            String whereClause = " AND " + String.join(" AND ", conditions);
            baseQuery += whereClause;
            countQuery += whereClause;
        }
        
        baseQuery += " ORDER BY c.createdAt DESC";
        
        // Execute count query
        jakarta.persistence.Query countQ = entityManager.createQuery(countQuery);
        for (int i = 0; i < params.size(); i++) {
            countQ.setParameter(i + 1, params.get(i));
        }
        Long total = (Long) countQ.getSingleResult();
        
        // Execute main query with pagination
        jakarta.persistence.Query query = entityManager.createQuery(baseQuery, Course.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        @SuppressWarnings("unchecked")
        List<Course> courses = query.getResultList();
        
        return new PageImpl<>(courses, pageable, total);
    }

    // Chức năng 6: Tìm kiếm, lọc, sắp xếp (Public)
    public Page<CourseResponse> getAllPublishedCourses(String keyword, Long categoryId, Double minPrice, Double maxPrice, Boolean isFree, Boolean isPaid, String level, Double minRating, int page, int size, String sort) {

        // 1. Phân trang và Sắp xếp
        // 'sort' có dạng: "price,asc" hoặc "createdAt,desc"
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        // 2. Tạo Specification (bộ lọc động)
        Specification<Course> spec = CourseRepository.isPublished();

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(CourseRepository.titleContains(keyword));
        }
        if (categoryId != null) {
            spec = spec.and(CourseRepository.hasCategory(categoryId));
        }
        
        // Price filtering - Priority: isFree/isPaid > minPrice/maxPrice
        if (isFree != null && isFree) {
            // Free courses: price = 0 (use isFree specification)
            spec = spec.and(CourseRepository.isFree());
        } else if (isPaid != null && isPaid) {
            // Paid courses: price > 0 (use isPaid specification)
            spec = spec.and(CourseRepository.isPaid());
        } else if (minPrice != null || maxPrice != null) {
            // Use price range if minPrice or maxPrice is provided (but not isFree/isPaid)
            spec = spec.and(CourseRepository.priceRange(minPrice, maxPrice));
        }
        
        // Level filtering
        // Note: Course entity currently doesn't have a 'level' field
        // Level filtering will be implemented when level field is added to Course entity
        // if (level != null && !level.isEmpty()) {
        //     spec = spec.and(CourseRepository.hasLevel(level));
        // }
        
        // Rating filtering (if Course entity has rating field)
        // Note: This will be implemented when rating field is added to Course entity
        // if (minRating != null) {
        //     spec = spec.and(CourseRepository.minRating(minRating));
        // }

        // 3. Truy vấn
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        // 4. Get enrolled course IDs and enrollment map for current user (if authenticated)
        Long currentUserId = getCurrentUserId();
        Set<Long> enrolledIds = getEnrolledCourseIds(currentUserId);
        Map<Long, Enrollment> enrollmentMap = getEnrollmentMap(currentUserId);

        // 5. Convert sang DTO và tính enrollmentCount + isEnrolled + enrollmentProgress + rating
        List<CourseResponse> dtos = coursePage.getContent().stream()
                .map(course -> {
                    CourseResponse dto = CourseResponse.fromEntity(course);
                    // Tính enrollmentCount từ repository (tránh LAZY loading issue)
                    Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
                    dto.setEnrollmentCount(enrollmentCount != null ? enrollmentCount : 0L);
                    
                    // Set rating info
                    Double avgRating = reviewRepository.getAverageRatingByCourseId(course.getId());
                    Long reviewCount = reviewRepository.countByCourseId(course.getId());
                    dto.setRating(avgRating != null ? avgRating : 0.0);
                    dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
                    
                    // Set isEnrolled status
                    boolean isEnrolled = enrolledIds.contains(course.getId());
                    dto.setIsEnrolled(isEnrolled);
                    
                    // If enrolled, set enrollment progress and status
                    if (isEnrolled) {
                        Enrollment enrollment = enrollmentMap.get(course.getId());
                        if (enrollment != null) {
                            if (enrollment.getProgress() != null) {
                                dto.setEnrollmentProgress(enrollment.getProgress());
                            } else {
                                dto.setEnrollmentProgress(0.0);
                            }
                            if (enrollment.getStatus() != null) {
                                dto.setEnrollmentStatus(enrollment.getStatus().name());
                            } else {
                                dto.setEnrollmentStatus("IN_PROGRESS");
                            }
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, coursePage.getTotalElements());
    }

    // Chức năng 7: Thống kê
    public CourseStatisticsResponse getCourseStatistics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        Long totalEnrollments = enrollmentRepository.countByCourseId(courseId);

        return new CourseStatisticsResponse(courseId, course.getTitle(), totalEnrollments);
    }
    
    // Chức năng 8: Analytics chi tiết
    public CourseAnalyticsResponse getCourseAnalytics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));
        
        // Basic stats
        Long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        
        // Calculate total revenue from successful transactions
        Double totalRevenue = transactionRepository.calculateRevenueByCourseId(courseId);
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }
        
        // Calculate completion rate
        Long completedEnrollments = enrollmentRepository.countByCourseIdAndStatus(
            courseId, 
            EEnrollmentStatus.COMPLETED
        );
        Double completionRate = 0.0;
        if (totalEnrollments > 0 && completedEnrollments != null) {
            completionRate = (completedEnrollments * 100.0) / totalEnrollments;
        }
        
        // Average rating (not implemented yet, set to null)
        Double averageRating = null;
        
        // Get current year for monthly data
        int currentYear = LocalDateTime.now().getYear();
        
        // Monthly enrollments
        List<Object[]> monthlyEnrollmentData = enrollmentRepository.getMonthlyEnrollmentsByCourse(courseId, currentYear);
        List<CourseAnalyticsResponse.MonthlyEnrollmentData> monthlyEnrollments = new ArrayList<>();
        for (Object[] data : monthlyEnrollmentData) {
            Integer month = (Integer) data[0];
            Long count = ((Number) data[2]).longValue();
            monthlyEnrollments.add(new CourseAnalyticsResponse.MonthlyEnrollmentData(
                "Tháng " + month,
                count
            ));
        }
        
        // Monthly revenue
        List<Object[]> monthlyRevenueData = transactionRepository.getMonthlyRevenueByCourse(courseId, currentYear);
        List<CourseAnalyticsResponse.MonthlyRevenueData> monthlyRevenue = new ArrayList<>();
        for (Object[] data : monthlyRevenueData) {
            Integer month = (Integer) data[0];
            Double revenue = ((Number) data[2]).doubleValue();
            monthlyRevenue.add(new CourseAnalyticsResponse.MonthlyRevenueData(
                "Tháng " + month,
                revenue
            ));
        }
        
        return new CourseAnalyticsResponse(
            courseId,
            course.getTitle(),
            totalEnrollments,
            totalRevenue,
            completionRate,
            averageRating,
            monthlyEnrollments,
            monthlyRevenue
        );
    }

    // Chức năng 7.1: Đánh dấu khóa học là nổi bật (Featured) - Chỉ Admin
    @Transactional
    public Course toggleFeatured(Long courseId, Boolean isFeatured) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        course.setIsFeatured(isFeatured != null ? isFeatured : true);
        course.setUpdatedAt(LocalDateTime.now());
        
        return courseRepository.save(course);
    }

    // Chức năng 8: Giảng viên gửi yêu cầu phê duyệt khóa học
    @Transactional
    public Course requestApproval(Long courseId, UserDetailsImpl userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        // Kiểm tra xem người gửi yêu cầu có phải là giảng viên của khóa học không
        if (!course.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to request approval for this course.");
        }

        // Không cho phép gửi yêu cầu nếu khóa học đã PUBLISHED
        if (ECourseStatus.PUBLISHED.equals(course.getStatus())) {
            throw new RuntimeException("Course is already published and cannot be sent for re-approval.");
        }

        course.setStatus(ECourseStatus.PENDING_APPROVAL);
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // Chức năng 9: Giảng viên tự publish khóa học (Marketplace Model - Self-Publish)
    @Transactional
    public Course publishCourse(Long courseId, UserDetailsImpl userDetails) {
        System.out.println("========================================");
        System.out.println("Publish Course Request");
        System.out.println("Course ID: " + courseId);
        System.out.println("User ID: " + userDetails.getId());
        System.out.println("========================================");
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    System.err.println("Course not found: " + courseId);
                    return new RuntimeException("Course not found!");
                });

        System.out.println("Course found: " + course.getTitle());
        System.out.println("Current status: " + course.getStatus());
        System.out.println("Instructor ID: " + (course.getInstructor() != null ? course.getInstructor().getId() : "null"));

        // Authorization: Ensure the current user is the owner of the course (or Admin)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        System.out.println("Is Admin: " + isAdmin);
        System.out.println("User is instructor: " + (course.getInstructor() != null && course.getInstructor().getId().equals(userDetails.getId())));

        if (!isAdmin && (course.getInstructor() == null || !course.getInstructor().getId().equals(userDetails.getId()))) {
            System.err.println("Authorization failed: User " + userDetails.getId() + " is not authorized to publish course " + courseId);
            throw new RuntimeException("You are not authorized to publish this course.");
        }

        // Validation: Ensure the course is currently in DRAFT status
        if (course.getStatus() != ECourseStatus.DRAFT) {
            System.err.println("Invalid status: Course is " + course.getStatus() + ", expected DRAFT");
            throw new RuntimeException("Only DRAFT courses can be published. Current status: " + course.getStatus());
        }

        // Validation: Course must have at least 1 chapter before publishing
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByPositionAsc(courseId);
        System.out.println("Chapters count: " + (chapters != null ? chapters.size() : 0));
        if (chapters == null || chapters.isEmpty()) {
            System.err.println("Validation failed: Course " + courseId + " has no chapters");
            throw new RuntimeException("Khóa học phải có ít nhất 1 chương trước khi xuất bản. Vui lòng thêm nội dung cho khóa học.");
        }

        // Validation: Each chapter must have at least 1 lesson
        for (Chapter chapter : chapters) {
            // Load lessons for this chapter
            List<Lesson> lessons = lessonRepository.findByChapterIdOrderByPositionAsc(chapter.getId());
            if (lessons == null || lessons.isEmpty()) {
                System.err.println("Validation failed: Chapter " + chapter.getId() + " (" + chapter.getTitle() + ") has no lessons");
                throw new RuntimeException("Chương \"" + chapter.getTitle() + "\" phải có ít nhất 1 bài học. Vui lòng thêm bài học cho chương này.");
            }
        }
        
        System.out.println("Validation passed: Course has " + chapters.size() + " chapter(s) with lessons");

        // Action: Update status to PUBLISHED
        course.setStatus(ECourseStatus.PUBLISHED);
        course.setIsPublished(true);
        course.setUpdatedAt(LocalDateTime.now());
        
        Course savedCourse = courseRepository.save(course);
        System.out.println("Course published successfully. New status: " + savedCourse.getStatus());
        System.out.println("========================================");

        // Gửi email thông báo khóa học mới khi được publish
        try {
            String courseUrl = "http://localhost:3000/courses/" + savedCourse.getId();
            newsletterService.sendNewCourseNotification(savedCourse.getTitle(), courseUrl);
            System.out.println(">>> Email notification sent for published course: " + savedCourse.getTitle());
        } catch (Exception e) {
            System.err.println(">>> ERROR: Failed to send email notification for published course: " + e.getMessage());
            e.printStackTrace();
            // Không throw exception để không ảnh hưởng đến việc publish khóa học
        }
        
        return savedCourse;
    }

    // Chức năng 10: Giảng viên gỡ khóa học (Unpublish) - PUBLISHED -> DRAFT
    @Transactional
    public Course unpublishCourse(Long courseId, UserDetailsImpl userDetails) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found!"));

        // Authorization: Ensure the current user is the owner of the course (or Admin)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(ERole.ROLE_ADMIN.name()));

        if (!isAdmin && !course.getInstructor().getId().equals(userDetails.getId())) {
            throw new RuntimeException("You are not authorized to unpublish this course.");
        }

        // Validation: Ensure the course is currently in PUBLISHED status
        if (course.getStatus() != ECourseStatus.PUBLISHED) {
            throw new RuntimeException("Only PUBLISHED courses can be unpublished. Current status: " + course.getStatus());
        }

        // Action: Update status to DRAFT
        course.setStatus(ECourseStatus.DRAFT);
        course.setIsPublished(false);
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // Chức năng 10: Lấy danh sách khóa học của học viên (My Courses)
    @Transactional(readOnly = true) // IMPORTANT: Add @Transactional to avoid LazyInitializationException
    public List<CourseResponse> getMyCourses(Long userId) {
        System.out.println("CourseService.getMyCourses: Fetching courses for user ID: " + userId);
        
        try {
            // Use JOIN FETCH query to avoid LazyInitializationException
            List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(userId);
            
            System.out.println("CourseService.getMyCourses: Found " + enrollments.size() + " enrollments");
            
            // Extract courses from enrollments
            List<CourseResponse> courses = enrollments.stream()
                    .map(enrollment -> {
                        try {
                            Course course = enrollment.getCourse();
                            if (course == null) {
                                System.err.println("WARNING: Enrollment " + enrollment.getId() + " has null course");
                                return null;
                            }
                            
                            CourseResponse dto = CourseResponse.fromEntity(course);
                            
                            // Set enrollment info
                            Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
                            dto.setEnrollmentCount(enrollmentCount != null ? enrollmentCount : 0L);
                            
                            // Set rating info
                            Double avgRating = reviewRepository.getAverageRatingByCourseId(course.getId());
                            Long reviewCount = reviewRepository.countByCourseId(course.getId());
                            dto.setRating(avgRating != null ? avgRating : 0.0);
                            dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
                            
                            // All courses in "My Courses" are enrolled by definition
                            dto.setIsEnrolled(true);
                            
                            // Set enrollment progress and status
                            if (enrollment.getProgress() != null) {
                                dto.setEnrollmentProgress(enrollment.getProgress());
                            } else {
                                dto.setEnrollmentProgress(0.0);
                            }
                            
                            if (enrollment.getStatus() != null) {
                                dto.setEnrollmentStatus(enrollment.getStatus().name());
                            } else {
                                dto.setEnrollmentStatus("IN_PROGRESS");
                            }
                            
                            System.out.println("Course " + course.getId() + " - Progress: " + dto.getEnrollmentProgress() + "%, Status: " + dto.getEnrollmentStatus());
                            
                            return dto;
                        } catch (Exception e) {
                            System.err.println("ERROR mapping enrollment " + enrollment.getId() + " to CourseResponse: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null) // Filter out null DTOs
                    .collect(Collectors.toList());
            
            System.out.println("CourseService.getMyCourses: Returning " + courses.size() + " courses");
            
            return courses;
        } catch (Exception e) {
            System.err.println("ERROR in getMyCourses: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch my courses: " + e.getMessage(), e);
        }
    }
}