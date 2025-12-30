package com.coursemgmt.service;

import com.coursemgmt.model.Certificate;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Service để generate PDF certificate
 * Sử dụng OpenHTMLToPDF để convert HTML sang PDF
 */
@Service
public class PdfGeneratorService {

    @Value("${certificate.storage.path:./certificates}")
    private String storagePath;
    
    @Value("${certificate.base-url:http://localhost:8080/certificates}")
    private String baseUrl;

    /**
     * Generate PDF certificate từ HTML content và trả về byte array
     * 
     * @param certificate Certificate entity
     * @return PDF file as byte array
     * @throws IOException Nếu có lỗi khi generate PDF
     */
    public byte[] generateCertificatePdf(Certificate certificate) throws IOException {
        // Generate HTML content
        String htmlContent = generateCertificateHtmlContent(certificate);
        
        // Convert HTML to PDF using OpenHTMLToPDF
        return convertHtmlToPdf(htmlContent);
    }

    /**
     * Convert HTML content to PDF byte array
     * Sử dụng PdfRendererBuilder để render HTML thành PDF
     * 
     * @param htmlContent HTML content as string
     * @return PDF as byte array
     * @throws IOException If PDF generation fails
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            // Set UTF-8 encoding explicitly
            // OpenHTMLToPDF will automatically handle UTF-8 if HTML has proper charset declaration
            builder.withHtmlContent(htmlContent, null);
            
            builder.toStream(outputStream);
            builder.run();
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Generate PDF certificate và lưu vào file, trả về URL
     * Method helper để tương thích với code cũ
     * 
     * @param certificate Certificate entity
     * @return URL của file PDF đã được lưu
     * @throws IOException Nếu có lỗi khi generate hoặc lưu PDF
     */
    public String generateCertificatePdfAndSave(Certificate certificate) throws IOException {
        // Convert to absolute path to match WebMvcConfig
        java.nio.file.Path storageDir = java.nio.file.Paths.get(storagePath).toAbsolutePath().normalize();
        
        // Ensure storage directory exists
        File directory = storageDir.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filename = "certificate_" + certificate.getCertificateCode() + ".pdf";
        java.nio.file.Path filepath = storageDir.resolve(filename);
        
        // Generate PDF bytes
        byte[] pdfBytes = generateCertificatePdf(certificate);
        
        // Save PDF to file
        try (FileOutputStream fos = new FileOutputStream(filepath.toFile())) {
            fos.write(pdfBytes);
            fos.flush();
        }
        
        // Return URL that matches the resource handler pattern
        return baseUrl + "/" + filename;
    }

    /**
     * Generate HTML content for certificate
     * Có thể convert sang PDF bằng Flying Saucer
     */
    private String generateCertificateHtmlContent(Certificate certificate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        String userName = certificate.getEnrollment().getUser().getFullName();
        String courseTitle = certificate.getEnrollment().getCourse().getTitle();
        String instructorName = certificate.getEnrollment()
                .getCourse().getInstructor().getFullName();
        String issuedDate = certificate.getIssuedAt().format(formatter);
        String certificateCode = certificate.getCertificateCode();
        
        // Escape HTML entities to prevent XSS and ensure proper encoding
        String safeUserName = escapeHtml(userName);
        String safeCourseTitle = escapeHtml(courseTitle);
        String safeInstructorName = escapeHtml(instructorName);
        String safeIssuedDate = escapeHtml(issuedDate);
        String safeCertificateCode = escapeHtml(certificateCode);
        
        // Use String.format() instead of .formatted() to avoid conflict with # in CSS
        // Ensure UTF-8 encoding is properly declared
        return String.format("""
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="UTF-8" />
    <style>
        @page {
            size: A4 landscape;
            margin: 0;
        }
        body {
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
            margin: 0;
            padding: 40px;
            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
        }
        .certificate {
            background: white;
            padding: 60px;
            border: 10px solid #667eea;
            text-align: center;
            min-height: 500px;
        }
        h1 {
            font-size: 48px;
            color: #667eea;
            margin-bottom: 20px;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        h2 {
            font-size: 24px;
            color: #333;
            margin-bottom: 40px;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        .recipient {
            font-size: 36px;
            color: #764ba2;
            margin: 30px 0;
            font-weight: bold;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        .course {
            font-size: 28px;
            color: #333;
            margin: 20px 0;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        .instructor {
            font-size: 18px;
            color: #666;
            margin-top: 40px;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        .date {
            font-size: 16px;
            color: #999;
            margin-top: 20px;
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
        .code {
            font-size: 14px;
            color: #999;
            margin-top: 30px;
            font-family: "DejaVu Sans Mono", "Courier New", monospace;
        }
        p {
            font-family: "DejaVu Sans", "Arial Unicode MS", "Noto Sans", Arial, sans-serif;
        }
    </style>
</head>
<body>
    <div class="certificate">
        <h1>CHỨNG CHỈ HOÀN THÀNH</h1>
        <h2>Certificate of Completion</h2>
        
        <p>Chứng nhận rằng / This is to certify that</p>
        
        <div class="recipient">%s</div>
        
        <p>đã hoàn thành xuất sắc khóa học<br />has successfully completed the course</p>
        
        <div class="course">%s</div>
        
        <div class="instructor">
            Giảng viên / Instructor: %s
        </div>
        
        <div class="date">
            Ngày cấp / Issue Date: %s
        </div>
        
        <div class="code">
            Mã chứng chỉ / Certificate Code: %s
        </div>
    </div>
</body>
</html>
                """, safeUserName, safeCourseTitle, safeInstructorName, safeIssuedDate, safeCertificateCode);
    }
    
    /**
     * Escape HTML special characters to prevent XSS and ensure proper rendering
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

