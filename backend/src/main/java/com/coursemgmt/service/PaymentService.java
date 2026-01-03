package com.coursemgmt.service;

import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private VNPayService vnPayService;

    /**
     * Tạo payment URL cho việc mua khóa học
     * UC-PAY-01: Payment Module - Create Payment
     */
    @Transactional
    public Map<String, String> createPaymentUrl(Long courseId) {
        // Lấy user hiện tại từ authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Course not found with id: " + courseId
                ));
        
        // Check if user already enrolled
        Long userId = user.getId();
        if (userId != null && courseId != null) {
            Optional<Enrollment> existingEnrollment = enrollmentRepository
                    .findByUserIdAndCourseId(userId, courseId);
            
            if (existingEnrollment.isPresent()) {
                throw new RuntimeException("Bạn đã đăng ký khóa học này rồi");
            }
        }
        
        // Create new Transaction with status PENDING
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCourse(course);
        transaction.setAmount(course.getPrice());
        transaction.setStatus(ETransactionStatus.PENDING);
        transaction.setPaymentGateway(EPaymentGateway.VNPAY); // Default to VNPAY for mock
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Generate unique transaction code
        String transactionCode = generateTransactionCode();
        transaction.setTransactionCode(transactionCode);
        
        Transaction saved = transactionRepository.save(transaction);
        
        // Generate VNPay payment URL with QR code support
        try {
            String returnUrl = "http://localhost:3000/payment/vnpay-return";
            // Sử dụng transactionCode làm vnp_TxnRef cho VNPay
            String paymentUrl = vnPayService.createPaymentUrl(
                transactionCode, // VNPay sẽ dùng transactionCode này làm vnp_TxnRef
                saved.getAmount(),
                "Thanh toan khoa hoc: " + course.getTitle(),
                returnUrl,
                null // Let user choose payment method on VNPay
            );
            
            return Map.of(
                "paymentUrl", paymentUrl,
                "transactionCode", transactionCode,
                "amount", saved.getAmount().toString()
            );
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create VNPay URL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo URL thanh toán: " + e.getMessage());
        }
    }

    /**
     * Xử lý callback từ payment gateway (Mock IPN)
     * UC-PAY-01: Payment Module - Process Callback
     * @param txnCode Transaction code
     * @param status Payment status (SUCCESS/FAILED)
     * @param cartId Optional cart ID if payment is from cart checkout
     */
    @Transactional
    public Map<String, String> processCallback(String txnCode, String status, String cartId) {
        System.out.println("========================================");
        System.out.println("Processing Payment Callback");
        System.out.println("Transaction Code: " + txnCode);
        System.out.println("Status Received: " + status);
        System.out.println("========================================");
        
        // 1. Find ALL Transactions by transactionCode (can be multiple for cart checkout)
        java.util.List<Transaction> transactions = transactionRepository.findAllByTransactionCode(txnCode);
        
        if (transactions.isEmpty()) {
            System.err.println("ERROR: No transactions found with code: " + txnCode);
            throw new ResourceNotFoundException("Transaction not found with code: " + txnCode);
        }
        
        System.out.println("Found " + transactions.size() + " transaction(s) with code: " + txnCode);
        for (Transaction t : transactions) {
            System.out.println("  - Transaction ID=" + t.getId() + 
                              ", Course=" + t.getCourse().getId() + 
                              " (" + t.getCourse().getTitle() + ")" +
                              ", Amount=" + t.getAmount());
        }
        
        // 2. Normalize Status (Case Insensitive)
        ETransactionStatus newStatus;
        try {
            newStatus = ETransactionStatus.valueOf(status.toUpperCase());
            System.out.println("Status normalized to: " + newStatus);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Invalid status received: " + status);
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be SUCCESS or FAILED");
        }
        
        // 3. Check if already processed (prevent duplicate processing)
        // Only check for SUCCESS status to avoid re-processing successful payments
        if (newStatus == ETransactionStatus.SUCCESS) {
            boolean alreadySuccess = transactions.stream()
                    .allMatch(t -> t.getStatus() == ETransactionStatus.SUCCESS);
            
            if (alreadySuccess) {
                System.out.println("Transaction already processed with SUCCESS status");
                // Return early - transaction already updated
                return Map.of(
                    "message", "Transaction already processed",
                    "transactionCode", txnCode,
                    "status", newStatus.toString(),
                    "alreadyUpdated", "true"
                );
            }
        }
        
        // 4. Update ALL transaction statuses
        for (Transaction transaction : transactions) {
            transaction.setStatus(newStatus);
            transactionRepository.save(transaction);
            System.out.println("Transaction " + transaction.getId() + " status updated to: " + newStatus);
        }
        
        // 5. Create Enrollments IF Success
        if (newStatus == ETransactionStatus.SUCCESS) {
            System.out.println("Status is SUCCESS - Creating Enrollments for all courses...");
            createEnrollmentsAfterPayment(transactions, cartId);
        } else {
            System.out.println("Status is " + newStatus + " - Skipping Enrollment creation");
        }
        
        System.out.println("========================================");
        System.out.println("Callback processing completed");
        System.out.println("========================================");
        
        return Map.of(
            "message", "Payment callback processed successfully",
            "transactionCode", txnCode,
            "status", newStatus.toString(),
            "coursesCount", String.valueOf(transactions.size())
        );
    }

    /**
     * Tự động tạo enrollment sau khi thanh toán thành công
     * @param transactions List các transactions đã thanh toán thành công (có thể là nhiều courses)
     * @param cartId Cart ID nếu thanh toán từ giỏ hàng (null nếu thanh toán đơn lẻ)
     */
    private void createEnrollmentsAfterPayment(java.util.List<Transaction> transactions, String cartId) {
        if (transactions.isEmpty()) return;
        
        // User is same for all transactions
        Long userId = transactions.get(0).getUser().getId();
        
        // Create enrollment for each transaction's course
        System.out.println(">>> Creating enrollments for " + transactions.size() + " course(s)");
        
        for (Transaction transaction : transactions) {
            Course course = transaction.getCourse();
            createSingleEnrollment(userId, course, transaction);
        }
        
        // Clear cart if cartId is provided
        if (cartId != null && !cartId.isEmpty()) {
            try {
                Long cartIdLong = Long.parseLong(cartId);
                Optional<Cart> cartOpt = cartRepository.findByIdWithItems(cartIdLong);
                
                if (cartOpt.isPresent()) {
                    Cart cart = cartOpt.get();
                    if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                        int itemCount = cart.getItems().size();
                        System.out.println(">>> Clearing " + itemCount + " items from cart ID: " + cart.getId());
                        
                        cart.getItems().clear();
                        cartRepository.save(cart);
                        
                        System.out.println(">>> Cart cleared successfully after payment");
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println(">>> ERROR: Invalid cartId format: " + cartId);
            }
        }
    }
    
    /**
     * Tạo enrollment cho một course
     */
    private void createSingleEnrollment(Long userId, Course course, Transaction transaction) {
        Long courseId = course.getId();
        
        System.out.println(">>> Creating Enrollment for User ID: " + userId + ", Course ID: " + courseId);
        
        // Check if enrollment already exists (prevent duplicates)
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        
        if (alreadyEnrolled) {
            System.out.println(">>> WARNING: User " + userId + " is already enrolled in Course " + courseId + ". Skipping enrollment creation.");
            return;
        }
        
        System.out.println(">>> No existing enrollment found. Creating new enrollment...");
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(userRepository.findById(userId).orElseThrow());
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(0.0);
        enrollment.setStatus(EEnrollmentStatus.IN_PROGRESS);
        
        System.out.println(">>> Enrollment object created:");
        System.out.println("    - User: " + enrollment.getUser().getId() + " (" + enrollment.getUser().getUsername() + ")");
        System.out.println("    - Course: " + enrollment.getCourse().getId() + " (" + enrollment.getCourse().getTitle() + ")");
        System.out.println("    - EnrolledAt: " + enrollment.getEnrolledAt());
        System.out.println("    - Status: " + enrollment.getStatus());
        
        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        System.out.println(">>> SUCCESS: Enrollment created with ID: " + savedEnrollment.getId());
        System.out.println(">>> Enrollment saved for User " + userId + " in Course " + courseId);
        
        // Verify the save
        Optional<Enrollment> verifyEnrollment = enrollmentRepository.findById(savedEnrollment.getId());
        if (verifyEnrollment.isPresent()) {
            System.out.println(">>> VERIFIED: Enrollment exists in database with ID: " + verifyEnrollment.get().getId());
        } else {
            System.err.println(">>> ERROR: Enrollment was not saved properly! ID: " + savedEnrollment.getId());
        }
        
        // Tạo thông báo cho instructor khi có học viên mua khóa học
        try {
            if (course.getInstructor() != null && transaction != null) {
                Long transactionId = transaction.getId();
                notificationService.notifyCoursePurchased(userId, courseId, transactionId);
            }
        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng đến quá trình thanh toán
            System.err.println(">>> ERROR: Failed to create notification: " + e.getMessage());
        }
    }
    
    /**
     * Generate unique transaction code
     */
    private String generateTransactionCode() {
        return "TXN_" + System.currentTimeMillis() + 
               String.format("%04d", new Random().nextInt(10000));
    }
}

