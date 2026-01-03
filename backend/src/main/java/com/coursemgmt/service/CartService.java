package com.coursemgmt.service;

import com.coursemgmt.dto.AddToCartRequest;
import com.coursemgmt.dto.CartItemResponse;
import com.coursemgmt.dto.CartResponse;
import com.coursemgmt.dto.CourseResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private VNPayService vnPayService;

    /**
     * Lấy hoặc tạo Cart cho user hiện tại
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Thêm khóa học vào giỏ hàng
     */
    @Transactional
    public CartResponse addToCart(Long userId, Long courseId) {
        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if user already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Bạn đã sở hữu khóa học này");
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Check if course already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndCourse(cart, course);
        if (existingItem.isPresent()) {
            throw new RuntimeException("Khóa học đã có trong giỏ hàng");
        }

        // Create new cart item
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setCourse(course);
        cartItemRepository.save(cartItem);

        // Return updated cart
        return getCart(userId);
    }

    /**
     * Lấy giỏ hàng của user
     * Note: Not read-only because it may need to create a new cart if one doesn't exist
     */
    @Transactional
    public CartResponse getCart(Long userId) {
        // Use getOrCreateCart to ensure cart exists (handles creation if needed)
        Cart cart = getOrCreateCart(userId);
        
        // Fetch cart with items loaded (JOIN FETCH)
        cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(cart); // Fallback to the cart we just got/created
        
        return mapToCartResponse(cart);
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    @Transactional
    public CartResponse removeFromCart(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        // Verify ownership
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to remove this item");
        }

        cartItemRepository.delete(item);

        return getCart(userId);
    }

    /**
     * Xóa tất cả items khỏi giỏ hàng
     * Uses orphanRemoval on Cart.items for automatic deletion
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(getOrCreateCart(userId));
        
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    /**
     * Checkout từ giỏ hàng - Tạo transactions cho tất cả courses trong cart
     * Trả về payment URL để redirect đến payment gateway
     */
    @Transactional
    public Map<String, String> checkout(Long userId) {
        // Get cart with items
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        User user = cart.getUser();

        // Validate: Check if user already enrolled in any course
        for (CartItem item : cart.getItems()) {
            Course course = item.getCourse();
            if (enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId())) {
                throw new RuntimeException("Bạn đã sở hữu khóa học: " + course.getTitle());
            }
        }

        // Calculate total amount
        Double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getCourse().getPrice())
                .sum();

        // Generate unique transaction code (shared by all transactions in this cart checkout)
        String transactionCode = generateTransactionCode();
        
        // Create a transaction for EACH course in the cart
        // All transactions share the same transactionCode for tracking
        for (CartItem item : cart.getItems()) {
            Course course = item.getCourse();
            
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setCourse(course);
            transaction.setAmount(course.getPrice()); // Individual course price
            transaction.setStatus(ETransactionStatus.PENDING);
            transaction.setPaymentGateway(EPaymentGateway.VNPAY);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setTransactionCode(transactionCode); // Same code for all
            
            transactionRepository.save(transaction);
        }

        // Generate VNPay payment URL with total amount
        try {
            // Build order info from course titles
            String orderInfo = "Thanh toan gio hang: " + 
                cart.getItems().stream()
                    .map(item -> item.getCourse().getTitle())
                    .limit(3) // Limit to first 3 courses to avoid URL too long
                    .collect(Collectors.joining(", "));
            
            if (cart.getItems().size() > 3) {
                orderInfo += " va " + (cart.getItems().size() - 3) + " khoa hoc khac";
            }
            
            String returnUrl = "http://localhost:3000/payment/vnpay-return";
            String paymentUrl = vnPayService.createPaymentUrl(
                transactionCode, // Use transactionCode as vnp_TxnRef
                totalAmount, // Total amount for all courses
                orderInfo,
                returnUrl,
                null // Let user choose payment method on VNPay (QR, ATM, etc.)
            );

        return Map.of(
            "paymentUrl", paymentUrl,
            "transactionCode", transactionCode,
            "amount", totalAmount.toString(),
            "cartId", cart.getId().toString()
        );
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create VNPay URL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo URL thanh toán: " + e.getMessage());
        }
    }

    /**
     * Generate unique transaction code
     */
    private String generateTransactionCode() {
        return "TXN_" + System.currentTimeMillis() + 
               String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * Map Cart entity to CartResponse DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<CartItemResponse> itemResponses = cart.getItems().stream()
                    .map(item -> {
                        CartItemResponse itemResponse = new CartItemResponse();
                        itemResponse.setId(item.getId());
                        itemResponse.setAddedAt(item.getAddedAt());
                        
                        // Map course to CourseResponse
                        Course course = item.getCourse();
                        CourseResponse courseResponse = CourseResponse.fromEntity(course);
                        itemResponse.setCourse(courseResponse);
                        
                        return itemResponse;
                    })
                    .collect(Collectors.toList());

            response.setItems(itemResponses);
            response.setItemCount(itemResponses.size());

            // Calculate total amount
            Double totalAmount = itemResponses.stream()
                    .mapToDouble(item -> item.getCourse().getPrice() != null ? item.getCourse().getPrice() : 0.0)
                    .sum();
            response.setTotalAmount(totalAmount);
        } else {
            response.setItems(List.of());
            response.setItemCount(0);
            response.setTotalAmount(0.0);
        }

        return response;
    }
}

