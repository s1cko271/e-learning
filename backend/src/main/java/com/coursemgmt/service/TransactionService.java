package com.coursemgmt.service;

import com.coursemgmt.dto.*;
import com.coursemgmt.exception.ResourceNotFoundException;
import com.coursemgmt.model.*;
import com.coursemgmt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private VNPayService vnPayService; // Service tích hợp VNPay

    /**
     * Tạo giao dịch mới và generate payment URL
     */
    @Transactional
    public PaymentResponse createTransaction(TransactionCreateRequest request) {
        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + request.getUserId()
                ));
        
        // Validate course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Course not found with id: " + request.getCourseId()
                ));
        
        // Tắm validation để demo dễ dàng
        // Check if user already enrolled in this course
        // Optional<Enrollment> existingEnrollment = enrollmentRepository
        //         .findByUserIdAndCourseId(user.getId(), course.getId());
        
        // if (existingEnrollment.isPresent()) {
        //     throw new RuntimeException("User already enrolled in this course");
        // }
        
        // Check if there's pending transaction
        // Optional<Transaction> pendingTx = transactionRepository
        //         .findByUserIdAndCourseIdAndStatus(
        //             user.getId(), 
        //             course.getId(), 
        //             ETransactionStatus.PENDING
        //         );
        
        // if (pendingTx.isPresent()) {
        //     throw new RuntimeException("There's already a pending transaction for this course");
        // }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCourse(course);
        transaction.setAmount(request.getAmount());
        transaction.setPaymentGateway(EPaymentGateway.valueOf(request.getPaymentGateway()));
        transaction.setStatus(ETransactionStatus.PENDING);
        
        // Generate unique transaction code
        String txCode = generateTransactionCode();
        transaction.setTransactionCode(txCode);
        
        transaction.setCreatedAt(LocalDateTime.now());
        
        Transaction saved = transactionRepository.save(transaction);
        
        // Generate payment URL based on gateway
        String paymentUrl = "";
        try {
            if ("VNPAY".equals(request.getPaymentGateway())) {
                paymentUrl = vnPayService.createPaymentUrl(
                    txCode, // Sử dụng transaction code
                    saved.getAmount(),
                    "Thanh toan khoa hoc: " + course.getTitle(),
                    request.getReturnUrl(),
                    request.getBankCode() // Use provided bankCode or null (let user choose)
                );
            } else if ("MOMO".equals(request.getPaymentGateway())) {
                // TODO: Implement MoMo integration
                paymentUrl = "https://momo.vn/payment/" + txCode;
            } else {
                // Bank transfer - no payment URL needed
                paymentUrl = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment URL: " + e.getMessage());
        }
        
        return new PaymentResponse(
            paymentUrl,
            txCode,
            "Payment created successfully",
            "SUCCESS"
        );
    }

    /**
     * Xử lý callback từ cổng thanh toán
     */
    @Transactional
    public TransactionDTO processPaymentCallback(Map<String, String> params) {
        String txCode = params.get("vnp_TxnRef"); // VNPay transaction ref
        
        Transaction transaction = transactionRepository.findByTransactionCode(txCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with code: " + txCode
                ));
        
        // Verify payment từ VNPay
        boolean isValid = vnPayService.verifyPaymentSignature(params);
        String responseCode = params.get("vnp_ResponseCode");
        
        if (isValid && "00".equals(responseCode)) {
            // Payment success
            transaction.setStatus(ETransactionStatus.SUCCESS);
            // Note: completedAt, bankCode, cardType are not in Transaction model
            
            // Tự động tạo enrollment khi thanh toán thành công
            createEnrollmentAfterPayment(transaction);
        } else {
            // Payment failed
            transaction.setStatus(ETransactionStatus.FAILED);
        }
        
        Transaction updated = transactionRepository.save(transaction);
        return convertToDTO(updated);
    }

    /**
     * Tự động tạo enrollment sau khi thanh toán thành công
     */
    private void createEnrollmentAfterPayment(Transaction transaction) {
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(transaction.getUser());
        enrollment.setCourse(transaction.getCourse());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(0.0);
        enrollment.setStatus(EEnrollmentStatus.IN_PROGRESS);
        
        enrollmentRepository.save(enrollment);
    }

    /**
     * Lấy tất cả giao dịch có phân trang
     */
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lấy giao dịch theo ID
     */
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with id: " + id
                ));
        return convertToDTO(transaction);
    }

    /**
     * Lấy giao dịch của user
     */
    public Page<TransactionDTO> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lấy giao dịch theo course
     */
    public Page<TransactionDTO> getCourseTransactions(Long courseId, Pageable pageable) {
        return transactionRepository.findByCourseId(courseId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Thống kê doanh thu theo khoảng thời gian
     */
    public Double calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        Double revenue = transactionRepository.calculateRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    /**
     * Generate unique transaction code
     */
    private String generateTransactionCode() {
        return "TXN" + System.currentTimeMillis() + 
               String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * Convert Entity to DTO
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser().getId());
        dto.setUserFullName(transaction.getUser().getFullName());
        dto.setCourseId(transaction.getCourse().getId());
        dto.setCourseTitle(transaction.getCourse().getTitle());
        dto.setAmount(transaction.getAmount());
        dto.setPaymentGateway(transaction.getPaymentGateway().toString());
        dto.setTransactionStatus(transaction.getStatus().toString());
        dto.setTransactionCode(transaction.getTransactionCode());
        dto.setCreatedAt(transaction.getCreatedAt());
        // Note: bankCode, cardType, completedAt are not in Transaction model
        return dto;
    }
}

