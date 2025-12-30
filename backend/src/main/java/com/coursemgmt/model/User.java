package com.coursemgmt.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // Đã mã hóa BCrypt

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    private String avatarUrl;

    @Lob
    private String bio;

    private String expertise;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 200)
    private String address;

    @Column(name = "email_notification_enabled", nullable = false)
    private Boolean emailNotificationEnabled = false; // Mặc định tắt thông báo email

    private LocalDateTime createdAt;

    private Boolean isEnabled = false; // Mặc định là false để kích hoạt

    @Column(length = 500)
    private String lockReason; // Lý do khóa tài khoản

    // (n-n) User có nhiều Role
    @ManyToMany(fetch = FetchType.EAGER) // Tải Role ngay khi tải User
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // (1-n) 1 Giảng viên có nhiều Khóa học
    @OneToMany(mappedBy = "instructor")
    private List<Course> coursesInstructed;

    // (1-n) 1 Học viên có nhiều lượt Ghi danh
    @OneToMany(mappedBy = "user")
    private List<Enrollment> enrollments;


    // (1-n) 1 User có nhiều Giao dịch
    @OneToMany(mappedBy = "user")
    private List<Transaction> transactions;
}