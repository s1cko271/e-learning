package com.coursemgmt.service;

import com.coursemgmt.dto.*;
import com.coursemgmt.model.ERole;
import com.coursemgmt.model.PasswordResetToken;
import com.coursemgmt.model.Role;
import com.coursemgmt.model.User;
import com.coursemgmt.repository.PasswordResetTokenRepository;
import com.coursemgmt.repository.RoleRepository;
import com.coursemgmt.repository.UserRepository;
import com.coursemgmt.security.jwt.JwtUtils;
import com.coursemgmt.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder; // Encoder BCrypt

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordResetTokenRepository tokenRepository;

    @Autowired
    EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // Chức năng Đăng nhập
    public JwtResponse loginUser(LoginRequest loginRequest) {
        // Kiểm tra tài khoản có bị khóa không trước khi authenticate
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(), 
                loginRequest.getUsernameOrEmail()
        );
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getIsEnabled() != null && !user.getIsEnabled()) {
                // Tài khoản bị khóa, ném DisabledException với message rõ ràng
                String lockMessage = user.getLockReason() != null && !user.getLockReason().trim().isEmpty()
                    ? user.getLockReason()
                    : "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.";
                throw new org.springframework.security.authentication.DisabledException(lockMessage);
            }
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication); // Trả token JWT

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    // Chức năng Đăng ký
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword())); // Mã hóa BCrypt
        user.setFullName(registerRequest.getFullName());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsEnabled(true); // Tạm thời để true, sau này có thể set false để xác thực email

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // Security: Block admin and mod roles from public registration
        if (strRoles != null && !strRoles.isEmpty()) {
            if (strRoles.contains("admin") || strRoles.contains("mod")) {
                throw new RuntimeException("Error: Role is not allowed for public registration.");
            }
        }

        // Default role: If roles are null/empty, default to 'user' (Student)
        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role 'STUDENT' is not found."));
            roles.add(userRole);
        } else {
            // Allowed Roles: Only allow 'user' (Student) or 'lecturer' (Instructor)
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "lecturer":
                        Role lecturerRole = roleRepository.findByName(ERole.ROLE_LECTURER)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'LECTURER' is not found."));
                        roles.add(lecturerRole);
                        break;
                    case "user":
                    case "student":
                    default:
                        // Default to Student role for any unrecognized role
                        Role userRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'STUDENT' is not found."));
                        roles.add(userRole);
                        break;
                }
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    // Chức năng Quên mật khẩu
    @Transactional
    public void handleForgotPassword(ForgotPasswordRequest request, HttpServletRequest servletRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại trong hệ thống!"));

        // Tìm token cũ nếu có
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken;
        
        if (existingToken.isPresent()) {
            // Cập nhật token cũ thay vì xóa và tạo mới (tránh duplicate key)
            resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        } else {
            // Tạo token mới nếu chưa có
            resetToken = new PasswordResetToken(token, user);
        }
        
        tokenRepository.save(resetToken);

        // Tạo link reset (trỏ về phía Frontend)
        // Sử dụng frontend URL từ config thay vì backend URL
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        // Gửi email
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    // Chức năng Đặt lại mật khẩu
    @Transactional
    public void handleResetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Error: Invalid reset token!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Error: Token expired!");
        }

        User user = resetToken.getUser();
        
        // Kiểm tra mật khẩu mới không được giống mật khẩu cũ
        if (encoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ. Vui lòng chọn mật khẩu khác.");
        }
        
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa token sau khi dùng xong
        tokenRepository.delete(resetToken);
    }

    // Chức năng Cập nhật Profile
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        // Step 1: Fetch user by ID
        System.out.println("UpdateProfile: Fetching user with ID: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.out.println("UpdateProfile: User not found with ID: " + userId);
                    return new UsernameNotFoundException("User not found!");
                });

        // Validate request
        if (request == null) {
            System.out.println("UpdateProfile: Request is null");
            throw new IllegalArgumentException("Request không hợp lệ");
        }

        // Step 2: Update fields ONLY if they are not null (Partial Update)
        // Note: Email update is restricted for security - only allow if not changing or changing to new valid email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            System.out.println("UpdateProfile: Email change requested - checking if email exists");
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email này đã được sử dụng");
            }
            user.setEmail(request.getEmail());
            System.out.println("UpdateProfile: Email updated");
        }

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            System.out.println("UpdateProfile: Updating fullName to: " + request.getFullName());
            user.setFullName(request.getFullName());
        }

        if (request.getAvatarUrl() != null) {
            System.out.println("UpdateProfile: Updating avatarUrl");
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getBio() != null) {
            System.out.println("UpdateProfile: Updating bio");
            user.setBio(request.getBio());
        }

        // Update expertise - allow empty string to clear the field
        if (request.getExpertise() != null) {
            System.out.println("UpdateProfile: Updating expertise to: " + request.getExpertise());
            user.setExpertise(request.getExpertise().trim().isEmpty() ? null : request.getExpertise().trim());
        }

        // Update phoneNumber - allow empty string to clear the field
        if (request.getPhoneNumber() != null) {
            System.out.println("UpdateProfile: Updating phoneNumber to: " + request.getPhoneNumber());
            user.setPhoneNumber(request.getPhoneNumber().trim().isEmpty() ? null : request.getPhoneNumber().trim());
        }

        // Update address - allow empty string to clear the field
        if (request.getAddress() != null) {
            System.out.println("UpdateProfile: Updating address to: " + request.getAddress());
            user.setAddress(request.getAddress().trim().isEmpty() ? null : request.getAddress().trim());
        }

        // Update emailNotificationEnabled
        if (request.getEmailNotificationEnabled() != null) {
            System.out.println("UpdateProfile: Updating emailNotificationEnabled to: " + request.getEmailNotificationEnabled());
            user.setEmailNotificationEnabled(request.getEmailNotificationEnabled());
        }

        // Step 3: CRITICAL - Save to database to persist changes
        System.out.println("UpdateProfile: Saving user to database...");
        User savedUser = userRepository.save(user);
        System.out.println("UpdateProfile: Profile updated successfully for user ID: " + userId);
        
        return savedUser;
    }

    // New method to update only avatar URL
    @Transactional
    public User updateAvatarUrl(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    // New method to get user by ID
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    // Get user profile as DTO to avoid Jackson infinite recursion
    public ProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        // Map User entity to ProfileResponse DTO
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getBio(),
                user.getExpertise(),
                user.getAvatarUrl(),
                user.getEmailNotificationEnabled() != null ? user.getEmailNotificationEnabled() : false,
                user.getCreatedAt()
        );
    }

    // Chức năng Đổi mật khẩu
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Step 1: Check User - Find user by ID
        System.out.println("ChangePassword: Fetching user with ID: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.out.println("ChangePassword: User not found with ID: " + userId);
                    return new UsernameNotFoundException("User not found!");
                });

        // Validate request data
        if (request == null) {
            System.out.println("ChangePassword: Request is null");
            throw new IllegalArgumentException("Request không hợp lệ");
        }
        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            System.out.println("ChangePassword: Old password is null or empty");
            throw new IllegalArgumentException("Mật khẩu hiện tại không được để trống");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            System.out.println("ChangePassword: New password is null or empty");
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }

        // Step 2: Verify Old Password - Use passwordEncoder.matches()
        System.out.println("ChangePassword: Checking old password...");
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            System.out.println("ChangePassword: Stored password is null or empty");
            throw new RuntimeException("Lỗi: Mật khẩu trong database không hợp lệ");
        }

        // Crucial: Verify old password matches
        if (!encoder.matches(request.getOldPassword(), storedPassword)) {
            System.out.println("ChangePassword: Old password does not match");
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        System.out.println("ChangePassword: Old password verified successfully");

        // Step 3: Check if new password is different from old password
        if (encoder.matches(request.getNewPassword(), storedPassword)) {
            System.out.println("ChangePassword: New password is the same as old password");
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ. Vui lòng chọn mật khẩu khác.");
        }
        System.out.println("ChangePassword: New password is different from old password");

        // Step 4: Hash and set new password
        System.out.println("ChangePassword: Encoding new password...");
        String encodedNewPassword = encoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        System.out.println("ChangePassword: New password encoded successfully");

        // Step 5: Save to database
        System.out.println("ChangePassword: Saving user to database...");
        userRepository.save(user);
        System.out.println("ChangePassword: Password changed successfully for user ID: " + userId);
    }
}