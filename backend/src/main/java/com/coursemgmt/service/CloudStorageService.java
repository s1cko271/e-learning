package com.coursemgmt.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service để upload và quản lý file trên Cloudinary
 */
@Service
public class CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudStorageService.class);
    
    private final Cloudinary cloudinary;
    private final boolean enabled;

    public CloudStorageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        
        this.enabled = cloudName != null && !cloudName.isEmpty() 
                    && apiKey != null && !apiKey.isEmpty() 
                    && apiSecret != null && !apiSecret.isEmpty();
        
        if (enabled) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", "true"); // Always use HTTPS
            
            this.cloudinary = new Cloudinary(config);
            logger.info("Cloudinary initialized successfully");
        } else {
            this.cloudinary = null;
            logger.warn("Cloudinary not configured - using local storage");
        }
    }

    /**
     * Upload file lên Cloudinary
     * @param file File cần upload
     * @param folder Folder trên Cloudinary (ví dụ: "avatars", "lessons/videos", "lessons/documents")
     * @param publicId Public ID cho file (nếu null sẽ tự động generate)
     * @return URL của file trên Cloudinary
     */
    public String uploadFile(MultipartFile file, String folder, String publicId) throws IOException {
        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "auto"); // Auto-detect: image, video, raw
            
            if (publicId != null && !publicId.isEmpty()) {
                params.put("public_id", publicId);
            }
            
            // Upload từ InputStream để tránh đọc toàn bộ file vào memory
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getInputStream(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            logger.info("File uploaded to Cloudinary: {} (size: {} bytes)", secureUrl, file.getSize());
            return secureUrl;
            
        } catch (IOException e) {
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Upload video file lên Cloudinary
     * Sử dụng InputStream để tránh đọc toàn bộ file vào memory
     */
    public String uploadVideo(MultipartFile file, String folder, String publicId) throws IOException {
        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "video");
            // Thêm timeout và chunk size cho video lớn
            params.put("chunk_size", 6000000); // 6MB chunks
            params.put("timeout", 120000); // 120 seconds timeout (tăng timeout cho video lớn)
            
            if (publicId != null && !publicId.isEmpty()) {
                params.put("public_id", publicId);
            }
            
            logger.info("Starting Cloudinary video upload: folder={}, publicId={}, size={} bytes", folder, publicId, file.getSize());
            
            // Upload từ InputStream thay vì đọc toàn bộ vào memory
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getInputStream(), params);
            
            if (uploadResult == null || !uploadResult.containsKey("secure_url")) {
                logger.error("Cloudinary upload returned null or missing secure_url");
                throw new IOException("Cloudinary upload failed: no secure_url in response");
            }
            
            String secureUrl = (String) uploadResult.get("secure_url");
            
            if (secureUrl == null || secureUrl.isEmpty()) {
                logger.error("Cloudinary upload returned empty secure_url");
                throw new IOException("Cloudinary upload failed: empty secure_url");
            }
            
            logger.info("Video uploaded successfully to Cloudinary: {} (size: {} bytes)", secureUrl, file.getSize());
            return secureUrl;
            
        } catch (Exception e) {
            logger.error("Error uploading video to Cloudinary: {}", e.getMessage(), e);
            // Log full stack trace
            if (e.getCause() != null) {
                logger.error("Caused by: {}", e.getCause().getMessage(), e.getCause());
            }
            throw new IOException("Failed to upload video to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Upload document/raw file lên Cloudinary
     */
    public String uploadDocument(MultipartFile file, String folder, String publicId) throws IOException {
        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "raw"); // For PDF, DOC, etc.
            
            if (publicId != null && !publicId.isEmpty()) {
                params.put("public_id", publicId);
            }
            
            // Upload từ InputStream để tránh đọc toàn bộ file vào memory
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getInputStream(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            logger.info("Document uploaded to Cloudinary: {} (size: {} bytes)", secureUrl, file.getSize());
            return secureUrl;
            
        } catch (IOException e) {
            logger.error("Error uploading document to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload document to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa file từ Cloudinary
     */
    public void deleteFile(String publicId, String resourceType) throws IOException {
        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured");
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("resource_type", resourceType); // "image", "video", "raw"
            
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, params);
            logger.info("File deleted from Cloudinary: {}", publicId);
            
        } catch (IOException e) {
            logger.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra xem Cloudinary có được enable không
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Extract public ID từ Cloudinary URL
     * Ví dụ: https://res.cloudinary.com/xxx/image/upload/v1234567890/folder/file.jpg
     * -> folder/file
     */
    public String extractPublicId(String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Extract public_id from URL
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                // Remove version prefix if exists (v1234567890/)
                if (afterUpload.matches("^v\\d+/.*")) {
                    afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
                }
                // Remove file extension
                int lastDot = afterUpload.lastIndexOf('.');
                if (lastDot > 0) {
                    afterUpload = afterUpload.substring(0, lastDot);
                }
                return afterUpload;
            }
        } catch (Exception e) {
            logger.warn("Error extracting public_id from URL: {}", cloudinaryUrl);
        }
        
        return null;
    }
}

