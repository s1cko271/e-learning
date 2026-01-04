package com.coursemgmt.config;

import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();

    // Danh sách các khóa học featured từ F8 (dựa vào SQL dump)
    private static final List<String> FEATURED_COURSE_TITLES = Arrays.asList(
            "Kiến Thức Nhập Môn IT",
            "Lập trình C++ cơ bản, nâng cao",
            "HTML CSS từ Zero đến Hero",
            "Responsive Với Grid System"
    );

    @Override
    public void run(String... args) throws Exception {
        // Initialize Roles
        initializeRoles();
        
        // Initialize Categories
        initializeCategories();
        
        // Initialize F8 Featured Courses from API
        initializeF8FeaturedCourses();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles...");

            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);

            Role lecturerRole = new Role();
            lecturerRole.setName(ERole.ROLE_LECTURER);
            roleRepository.save(lecturerRole);

            Role studentRole = new Role();
            studentRole.setName(ERole.ROLE_STUDENT);
            roleRepository.save(studentRole);

            System.out.println("Roles initialized successfully!");
        } else {
            System.out.println("Roles already exist. Skipping initialization.");
        }
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            System.out.println("Initializing categories...");

            Category cat1 = new Category();
            cat1.setName("Lập trình");
            cat1.setDescription("Các khóa học về lập trình và phát triển phần mềm");
            categoryRepository.save(cat1);

            Category cat2 = new Category();
            cat2.setName("Web Development");
            cat2.setDescription("Phát triển ứng dụng web");
            categoryRepository.save(cat2);

            Category cat3 = new Category();
            cat3.setName("Mobile Development");
            cat3.setDescription("Phát triển ứng dụng di động");
            categoryRepository.save(cat3);

            Category cat4 = new Category();
            cat4.setName("Data Science");
            cat4.setDescription("Khoa học dữ liệu và phân tích");
            categoryRepository.save(cat4);

            Category cat5 = new Category();
            cat5.setName("Front-end");
            cat5.setDescription("Phát triển giao diện người dùng");
            categoryRepository.save(cat5);

            Category cat6 = new Category();
            cat6.setName("Back-end");
            cat6.setDescription("Phát triển server và API");
            categoryRepository.save(cat6);

            Category cat7 = new Category();
            cat7.setName("Mobile App");
            cat7.setDescription("Phát triển ứng dụng di động");
            categoryRepository.save(cat7);

            Category cat8 = new Category();
            cat8.setName("DevOps");
            cat8.setDescription("Vận hành và triển khai phần mềm");
            categoryRepository.save(cat8);

            Category cat9 = new Category();
            cat9.setName("UI/UX Design");
            cat9.setDescription("Thiết kế giao diện và trải nghiệm người dùng");
            categoryRepository.save(cat9);

            Category cat10 = new Category();
            cat10.setName("Database");
            cat10.setDescription("Quản lý và phát triển cơ sở dữ liệu");
            categoryRepository.save(cat10);

            System.out.println("Categories initialized successfully!");
        } else {
            System.out.println("Categories already exist. Skipping initialization.");
        }
    }

    private void initializeF8FeaturedCourses() {
        System.out.println("Fetching F8 courses data...");
        
        try {
            // Fetch data from F8 API
            List<Map<String, Object>> f8Courses = fetchF8Courses();
            
            if (f8Courses.isEmpty()) {
                System.out.println("No F8 courses found. Skipping initialization.");
                return;
            }

            // Lấy roles và categories
            Role lecturerRole = roleRepository.findByName(ERole.ROLE_LECTURER)
                    .orElseThrow(() -> new RuntimeException("ROLE_LECTURER not found"));

            List<Category> categories = categoryRepository.findAll();
            if (categories.isEmpty()) {
                System.out.println("No categories found. Skipping F8 data initialization.");
                return;
            }

            // Tạo default instructor nếu chưa có
            User defaultInstructor = userRepository.findByUsername("f8_instructor")
                    .orElseGet(() -> {
                        User instructor = createInstructor(
                                "f8_instructor",
                                "f8@example.com",
                                "F8 Instructor",
                                "Giảng viên từ F8 Education",
                                "Web Development, Programming",
                                passwordEncoder.encode("123456")
                        );
                        instructor.getRoles().add(lecturerRole);
                        return userRepository.save(instructor);
                    });

            // Import tất cả các khóa học từ F8
            int importedCount = 0;
            int featuredCount = 0;
            for (Map<String, Object> courseData : f8Courses) {
                String title = (String) courseData.get("title");
                
                // Kiểm tra xem có phải khóa học featured không
                boolean isFeatured = FEATURED_COURSE_TITLES.contains(title);
                if (isFeatured) {
                    featuredCount++;
                }
                
                // Import tất cả các khóa học
                importF8Course(courseData, defaultInstructor, categories, isFeatured);
                importedCount++;
            }

            System.out.println("F8 courses initialized successfully! Imported: " + importedCount + " courses (Featured: " + featuredCount + ").");
        } catch (Exception e) {
            System.err.println("Error initializing F8 courses: " + e.getMessage());
            e.printStackTrace();
            // Không throw exception để app vẫn có thể start được
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchF8Courses() {
        try {
            String apiUrl = "https://api-gateway.f8.edu.vn/api/combined-courses";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Referer", "https://f8.edu.vn/");
            headers.set("Origin", "https://f8.edu.vn");
            headers.set("Accept", "application/json, text/plain, */*");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> allCourses = new ArrayList<>();
                
                // Process free_courses and pro_courses
                for (Map.Entry<String, Object> entry : responseBody.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> categoryData = (Map<String, Object>) entry.getValue();
                        if (categoryData.containsKey("data") && categoryData.get("data") instanceof List) {
                            List<Map<String, Object>> courses = (List<Map<String, Object>>) categoryData.get("data");
                            allCourses.addAll(courses);
                        }
                    }
                }
                
                return allCourses;
            }
        } catch (Exception e) {
            System.err.println("Error fetching F8 API: " + e.getMessage());
        }
        
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private void importF8Course(Map<String, Object> courseData, User instructor, List<Category> categories, boolean isFeatured) {
        try {
            String title = (String) courseData.get("title");
            
            // Kiểm tra xem khóa học đã tồn tại chưa (tránh duplicate)
            if (courseRepository.findByTitle(title).isPresent()) {
                System.out.println("Course already exists, skipping: " + title);
                return;
            }
            
            String description = (String) courseData.getOrDefault("description", "");
            Object priceObj = courseData.getOrDefault("price", 0);
            Double price = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0.0;
            String imageUrl = (String) courseData.getOrDefault("image_url", "");
            
            // Parse duration from duration_text (e.g., "10 hours" -> 10)
            Integer totalDurationInHours = parseDuration(courseData.getOrDefault("duration_text", "").toString());
            
            // Map category based on course title/keywords
            Category category = mapCategory(title, categories);
            
            Course course = new Course();
            course.setTitle(title);
            course.setDescription(description.isEmpty() ? "Khóa học từ F8 Education" : description);
            course.setPrice(price);
            course.setImageUrl(imageUrl.isEmpty() ? "https://files.f8.edu.vn/f8-prod/courses/7.png" : imageUrl);
            course.setTotalDurationInHours(totalDurationInHours != null ? totalDurationInHours : 10);
            course.setStatus(ECourseStatus.PUBLISHED);
            course.setIsFeatured(isFeatured); // Chỉ đánh dấu featured cho các khóa học trong danh sách
            course.setIsPublished(true);
            course.setInstructor(instructor);
            course.setCategory(category);
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            
            courseRepository.save(course);
            String featuredLabel = isFeatured ? " [FEATURED]" : "";
            System.out.println("Imported course: " + title + featuredLabel);
        } catch (Exception e) {
            System.err.println("Error importing course: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer parseDuration(String durationText) {
        if (durationText == null || durationText.isEmpty()) {
            return 10; // Default
        }
        
        try {
            // Extract number from text like "10 hours", "3 giờ", etc.
            String[] parts = durationText.split("\\s+");
            for (String part : parts) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return 10; // Default
    }

    private Category mapCategory(String title, List<Category> categories) {
        String titleLower = title.toLowerCase();
        
        // Map based on keywords
        if (titleLower.contains("html") || titleLower.contains("css") || titleLower.contains("responsive")) {
            return categories.stream()
                    .filter(c -> c.getName().equals("Front-end") || c.getName().equals("Web Development"))
                    .findFirst()
                    .orElse(categories.get(0));
        } else if (titleLower.contains("javascript") || titleLower.contains("react") || titleLower.contains("node")) {
            return categories.stream()
                    .filter(c -> c.getName().equals("Web Development") || c.getName().equals("Front-end"))
                    .findFirst()
                    .orElse(categories.get(0));
        } else if (titleLower.contains("c++") || titleLower.contains("lập trình")) {
            return categories.stream()
                    .filter(c -> c.getName().equals("Lập trình"))
                    .findFirst()
                    .orElse(categories.get(0));
        }
        
        return categories.get(0); // Default
    }

    private User createInstructor(String username, String email, String fullName, 
                                  String bio, String expertise, String encodedPassword) {
        User instructor = new User();
        instructor.setUsername(username);
        instructor.setEmail(email);
        instructor.setFullName(fullName);
        instructor.setPassword(encodedPassword);
        instructor.setBio(bio);
        instructor.setExpertise(expertise);
        instructor.setIsEnabled(true);
        instructor.setCreatedAt(LocalDateTime.now());
        instructor.setRoles(new HashSet<>());
        return instructor;
    }

}

