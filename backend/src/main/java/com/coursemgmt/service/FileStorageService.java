package com.coursemgmt.service;

import com.coursemgmt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path avatarStorageLocation;
    private final String avatarBaseUrl;
    private final Path courseImageStorageLocation;
    private final String courseImageBaseUrl;
    private final Path lessonVideoStorageLocation;
    private final String lessonVideoBaseUrl;
    private final Path lessonDocumentStorageLocation;
    private final String lessonDocumentBaseUrl;
    private final Path lessonSlideStorageLocation;
    private final String lessonSlideBaseUrl;
    private final SlideConversionService slideConversionService;

    public FileStorageService(@Value("${avatar.storage.path:./uploads/avatars}") String avatarStoragePath,
                              @Value("${AVATAR_BASE_URL:${avatar.base-url:http://localhost:8080/api/files/avatars}}") String avatarBaseUrl,
                              @Value("${course.image.storage.path:./uploads/courses}") String courseImageStoragePath,
                              @Value("${COURSE_IMAGE_BASE_URL:${course.image.base-url:http://localhost:8080/api/files/courses}}") String courseImageBaseUrl,
                              @Value("${lesson.video.storage.path:./uploads/lessons/videos}") String lessonVideoStoragePath,
                              @Value("${LESSON_VIDEO_BASE_URL:${lesson.video.base-url:http://localhost:8080/api/files/lessons/videos}}") String lessonVideoBaseUrl,
                              @Value("${lesson.document.storage.path:./uploads/lessons/documents}") String lessonDocumentStoragePath,
                              @Value("${lesson.document.base-url:http://localhost:8080/api/files/lessons/documents}") String lessonDocumentBaseUrl,
                              @Value("${lesson.slide.storage.path:./uploads/lessons/slides}") String lessonSlideStoragePath,
                              @Value("${lesson.slide.base-url:http://localhost:8080/api/files/lessons/slides}") String lessonSlideBaseUrl,
                              @Lazy SlideConversionService slideConversionService) {
        this.avatarStorageLocation = Paths.get(avatarStoragePath).toAbsolutePath().normalize();
        this.avatarBaseUrl = avatarBaseUrl;
        this.courseImageStorageLocation = Paths.get(courseImageStoragePath).toAbsolutePath().normalize();
        this.courseImageBaseUrl = courseImageBaseUrl;
        this.lessonVideoStorageLocation = Paths.get(lessonVideoStoragePath).toAbsolutePath().normalize();
        this.lessonVideoBaseUrl = lessonVideoBaseUrl;
        this.lessonDocumentStorageLocation = Paths.get(lessonDocumentStoragePath).toAbsolutePath().normalize();
        this.lessonDocumentBaseUrl = lessonDocumentBaseUrl;
        this.lessonSlideStorageLocation = Paths.get(lessonSlideStoragePath).toAbsolutePath().normalize();
        this.lessonSlideBaseUrl = lessonSlideBaseUrl;
        this.slideConversionService = slideConversionService;
        try {
            Files.createDirectories(this.avatarStorageLocation);
            Files.createDirectories(this.courseImageStorageLocation);
            Files.createDirectories(this.lessonVideoStorageLocation);
            Files.createDirectories(this.lessonDocumentStorageLocation);
            Files.createDirectories(this.lessonSlideStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeAvatar(MultipartFile file, Long userId) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        // Validate file type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Invalid file type. Only image files are allowed.");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) { // 5 MB
            throw new RuntimeException("File size exceeds the limit of 5MB.");
        }

        // Create unique file name: userId_timestamp_uuid.extension
        String fileName = userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        try {
            Path targetLocation = this.avatarStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return avatarBaseUrl + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.avatarStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.avatarStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    public String storeCourseImage(MultipartFile file, Long courseId) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        // Validate file type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Invalid file type. Only image files are allowed.");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) { // 10 MB
            throw new RuntimeException("File size exceeds the limit of 10MB.");
        }

        // Create unique file name: courseId_timestamp_uuid.extension
        String fileName = courseId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        try {
            Path targetLocation = this.courseImageStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return courseImageBaseUrl + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadCourseImageAsResource(String fileName) {
        try {
            Path filePath = this.courseImageStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName);
        }
    }

    public void deleteCourseImage(String fileName) {
        try {
            Path filePath = this.courseImageStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    // Store lesson video file
    public String storeLessonVideo(MultipartFile file, Long lessonId) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        // Validate file type (video files)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new RuntimeException("Invalid file type. Only video files are allowed.");
        }

        // Validate file size (max 500MB for videos)
        if (file.getSize() > 500 * 1024 * 1024) { // 500 MB
            throw new RuntimeException("File size exceeds the limit of 500MB.");
        }

        // Create unique file name: lessonId_timestamp_uuid.extension
        String fileName = lessonId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        try {
            Path targetLocation = this.lessonVideoStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return lessonVideoBaseUrl + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    // Store lesson document file (PDF, DOC, etc.)
    public String storeLessonDocument(MultipartFile file, Long lessonId) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        // Validate file type (documents: PDF, DOC, DOCX, etc.)
        String contentType = file.getContentType();
        String lowerFileName = originalFileName.toLowerCase();
        boolean isValidDocument = (contentType != null && 
            contentType.equals("application/pdf")
        ) || lowerFileName.endsWith(".pdf");

        if (!isValidDocument) {
            throw new RuntimeException("Chỉ cho phép upload file PDF.");
        }

        // Validate file size (max 50MB for documents)
        if (file.getSize() > 50 * 1024 * 1024) { // 50 MB
            throw new RuntimeException("File size exceeds the limit of 50MB.");
        }

        // Create unique file name: lessonId_timestamp_uuid.extension
        String fileName = lessonId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        try {
            Path targetLocation = this.lessonDocumentStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return lessonDocumentBaseUrl + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadLessonVideoAsResource(String fileName) {
        try {
            Path filePath = this.lessonVideoStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName);
        }
    }

    public Resource loadLessonDocumentAsResource(String fileName) {
        try {
            Path filePath = this.lessonDocumentStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName);
        }
    }

    // Store lesson slide file (PPT, PPTX, ODP, PDF)
    // Automatically converts PPT/PPTX to PDF for direct viewing in browser
    public String storeLessonSlide(MultipartFile file, Long lessonId) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        // Validate file type (slides: PPT, PPTX, ODP, PDF)
        String contentType = file.getContentType();
        String lowerFileName = originalFileName.toLowerCase();
        boolean isValidSlide = (contentType != null && (
            contentType.equals("application/vnd.ms-powerpoint") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
            contentType.equals("application/vnd.oasis.opendocument.presentation") ||
            contentType.equals("application/pdf")
        )) || lowerFileName.endsWith(".ppt") || lowerFileName.endsWith(".pptx") 
           || lowerFileName.endsWith(".odp") || lowerFileName.endsWith(".pdf");

        if (!isValidSlide) {
            throw new RuntimeException("Chỉ cho phép upload file slide (PPT, PPTX, ODP, PDF).");
        }

        // Validate file size (max 100MB for slides)
        if (file.getSize() > 100 * 1024 * 1024) { // 100 MB
            throw new RuntimeException("File size exceeds the limit of 100MB.");
        }

        // Check if file needs conversion to PDF
        boolean needsConversion = slideConversionService.needsConversion(originalFileName);
        
        // Create unique file name: lessonId_timestamp_uuid.extension
        String uniqueId = lessonId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = uniqueId + fileExtension;

        try {
            Path targetLocation = this.lessonSlideStorageLocation.resolve(fileName);
            logger.info("Storing slide file: {} (size: {} bytes)", originalFileName, file.getSize());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File saved to: {}", targetLocation);
            
            // Convert PPT/PPTX to PDF for direct viewing in browser
            if (needsConversion) {
                logger.info("Starting automatic conversion to PDF for: {}", originalFileName);
                try {
                    Path pdfPath = slideConversionService.convertAndGetPdfPath(targetLocation);
                    String pdfFileName = pdfPath.getFileName().toString();
                    logger.info("Slide converted successfully to PDF: {}", pdfFileName);
                    return lessonSlideBaseUrl + "/" + pdfFileName;
                } catch (Exception e) {
                    logger.error("Failed to convert slide to PDF: {}. Returning original file URL.", e.getMessage());
                    // If conversion fails, keep the original file and return its URL
                    // User will need to download it instead of viewing inline
                    return lessonSlideBaseUrl + "/" + fileName;
                }
            }
            
            logger.info("Slide stored successfully: {}", fileName);
            return lessonSlideBaseUrl + "/" + fileName;
        } catch (IOException ex) {
            logger.error("Failed to store slide file: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadLessonSlideAsResource(String fileName) {
        try {
            Path filePath = this.lessonSlideStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + fileName);
        }
    }
}

