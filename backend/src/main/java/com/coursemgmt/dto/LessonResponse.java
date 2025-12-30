package com.coursemgmt.dto;

import com.coursemgmt.model.EContentType;
import com.coursemgmt.model.Lesson;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private EContentType contentType;
    private String videoUrl;
    private String documentUrl;
    private String slideUrl;
    private String content;
    private Integer durationInMinutes;
    private Integer position;
    private Boolean isPreview; // Cho phép giảng viên preview bài học trước khi publish

    // Dùng cho chức năng "Theo dõi tiến độ"
    @JsonProperty("isCompleted")
    private boolean isCompleted;

    public static LessonResponse fromEntity(Lesson lesson, boolean isCompleted) {
        LessonResponse dto = new LessonResponse();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setDurationInMinutes(lesson.getDurationInMinutes());
        dto.setPosition(lesson.getPosition());
        dto.setIsPreview(lesson.getIsPreview() != null ? lesson.getIsPreview() : false);
        dto.setCompleted(isCompleted);

        // Trả về nội dung chi tiết cho học viên đã đăng ký (không cần đợi hoàn thành)
        // Học viên cần xem video để học, không phải chỉ khi đã hoàn thành
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setDocumentUrl(lesson.getDocumentUrl());
        dto.setSlideUrl(lesson.getSlideUrl());
        dto.setContent(lesson.getContent());

        return dto;
    }
}