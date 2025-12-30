package com.coursemgmt.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EContentType contentType; // Enum: VIDEO, TEXT, DOCUMENT

    private String videoUrl;

    private String documentUrl;

    private String slideUrl; // URL cho slide bài giảng (PPT, PPTX, ODP)

    @Lob // Dùng cho nội dung text dài
    private String content;

    private Integer position; // Thứ tự bài học

    private Integer durationInMinutes;

    @Column(name = "is_preview")
    private Boolean isPreview = false; // Cho phép giảng viên preview bài học trước khi publish

    // (n-1) Nhiều Lesson thuộc 1 Chapter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;


    // (1-n) 1 Lesson có nhiều Tiến độ
    @OneToMany(mappedBy = "lesson")
    private List<User_Progress> progresses;
}