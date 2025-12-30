package com.coursemgmt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 1, max = 100, message = "Tên phải có từ 1 đến 100 ký tự")
    private String fullName;

    @Size(max = 50)
    @Email(message = "Email should be valid")
    private String email;

    private String avatarUrl;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    // TODO: Implement XSS sanitization (since adding a full HTML sanitizer library might be too heavy right now)
    private String bio;

    @Size(max = 200, message = "Chuyên môn không được quá 200 ký tự")
    private String expertise;

    // Note: phoneNumber and address fields are defined here for future use
    // Currently, User entity does not have these fields, so they will be ignored
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;

    @Size(max = 200, message = "Địa chỉ không được quá 200 ký tự")
    private String address;

    private Boolean emailNotificationEnabled;
}