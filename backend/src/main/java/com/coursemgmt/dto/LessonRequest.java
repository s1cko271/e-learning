package com.coursemgmt.dto;

import com.coursemgmt.model.EContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonRequest {
    @NotBlank
    private String title;

    @NotNull
    private EContentType contentType; // VIDEO, TEXT, DOCUMENT, SLIDE

    private String videoUrl;
    private String documentUrl;
    private String slideUrl; // URL cho slide bài giảng
    private String content; // Dùng cho bài đọc

    @NotNull
    private Integer position; // Thứ tự bài học

    @NotNull
    private Integer durationInMinutes; // Thời lượng

    private Boolean isPreview = false; // Cho phép giảng viên preview bài học trước khi publish
}