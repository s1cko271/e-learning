package com.coursemgmt.controller;

import com.coursemgmt.service.PaymentService;
import com.coursemgmt.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý callback từ VNPay
 * - ReturnURL: URL mà VNPay redirect user về sau khi thanh toán
 * - IPN URL: URL mà VNPay gọi để thông báo kết quả thanh toán (server-to-server)
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/vnpay")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private PaymentService paymentService;

    /**
     * Xử lý ReturnURL - VNPay redirect user về đây sau khi thanh toán
     * GET /api/v1/vnpay/return
     * 
     * LƯU Ý: ReturnURL chỉ kiểm tra checksum và hiển thị kết quả cho user
     * KHÔNG cập nhật database tại đây. Database được cập nhật tại IPN URL.
     * 
     * VNPay sẽ redirect với các tham số:
     * - vnp_TxnRef: Mã giao dịch
     * - vnp_ResponseCode: Mã phản hồi (00 = thành công)
     * - vnp_SecureHash: Chữ ký để verify
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> handleReturnUrl(
            @RequestParam Map<String, String> params
    ) {
        System.out.println("========================================");
        System.out.println("VNPay ReturnURL Callback Received");
        System.out.println("Params: " + params);
        System.out.println("========================================");
        
        try {
            // Verify signature
            boolean isValid = vnPayService.verifyPaymentSignature(params);
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String transactionNo = params.get("vnp_TransactionNo");
            String amount = params.get("vnp_Amount");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");
            
            if (!isValid) {
                System.err.println("ERROR: Invalid signature from VNPay");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                            "success", false,
                            "message", "Chữ ký không hợp lệ",
                            "code", "INVALID_SIGNATURE"
                        ));
            }
            
            // ReturnURL chỉ hiển thị kết quả, KHÔNG cập nhật database
            // Database được cập nhật tại IPN URL (server-to-server)
            System.out.println("ReturnURL: Checksum valid, returning result to user");
            System.out.println("========================================");
            
            // Return result for frontend to display
            Map<String, Object> response = new HashMap<>();
            response.put("success", "00".equals(responseCode));
            response.put("message", "00".equals(responseCode) ? "Thanh toán thành công" : "Thanh toán thất bại");
            response.put("responseCode", responseCode);
            response.put("transactionCode", txnRef);
            response.put("transactionNo", transactionNo);
            response.put("amount", amount);
            response.put("bankCode", bankCode);
            response.put("payDate", payDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Lỗi xử lý thanh toán: " + e.getMessage()
                    ));
        }
    }

    /**
     * Xử lý IPN URL - VNPay gọi đến đây để thông báo kết quả thanh toán (server-to-server)
     * GET /api/v1/vnpay/ipn
     * 
     * IPN URL được gọi bất kể user có quay lại ReturnURL hay không
     * Đây là nơi QUAN TRỌNG để cập nhật trạng thái thanh toán vào database
     * 
     * Response format theo VNPay:
     * - RspCode: "00" = Success, "02" = Already updated, "04" = Invalid amount, "97" = Invalid signature, "99" = Unknown error
     * - Message: Mô tả lỗi
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIpnUrl(
            @RequestParam Map<String, String> params,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        System.out.println("========================================");
        System.out.println("VNPay IPN Callback Received");
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Query String: " + request.getQueryString());
        System.out.println("Params Map: " + params);
        System.out.println("Params Size: " + params.size());
        
        // Log all parameter names
        if (params.isEmpty()) {
            System.out.println("WARNING: Params map is empty!");
            System.out.println("This might be a test call from VNPay Dashboard");
        } else {
            System.out.println("Parameter names: " + params.keySet());
        }
        System.out.println("========================================");
        
        try {
            // Handle empty params (test call from VNPay Dashboard)
            if (params.isEmpty() || params.size() == 0) {
                System.out.println("Empty params detected - likely a test call");
                // Return success for test calls (VNPay just checks if endpoint is reachable)
                return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Test call received successfully"
                ));
            }
            
            // 1. Verify signature FIRST (quan trọng!)
            boolean isValid = vnPayService.verifyPaymentSignature(params);
            
            if (!isValid) {
                System.err.println("ERROR: Invalid signature from VNPay IPN");
                System.err.println("Received params: " + params);
                return ResponseEntity.ok(Map.of(
                    "RspCode", "97",
                    "Message", "Checksum failed"
                ));
            }
            
            // 2. Get transaction info
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            
            System.out.println("Transaction: " + txnRef);
            System.out.println("Response Code: " + responseCode);
            System.out.println("Amount: " + amount);
            
            // 3. Process payment callback (update database)
            String status = "00".equals(responseCode) ? "SUCCESS" : "FAILED";
            Map<String, String> result = paymentService.processCallback(txnRef, status, null);
            
            System.out.println("IPN processed successfully for transaction: " + txnRef);
            System.out.println("Result: " + result);
            System.out.println("========================================");
            
            // 4. Check if already updated (from service response)
            if ("true".equals(result.get("alreadyUpdated"))) {
                // Transaction already processed, return RspCode 02
                return ResponseEntity.ok(Map.of(
                    "RspCode", "02",
                    "Message", "Order Already Update"
                ));
            }
            
            // 5. Return success response to VNPay
            // RspCode "00" = Success, VNPay will stop retrying
            return ResponseEntity.ok(Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
            ));
            
        } catch (Exception e) {
            System.err.println("ERROR processing VNPay IPN: " + e.getMessage());
            e.printStackTrace();
            // RspCode "99" = Unknown error, VNPay will retry
            return ResponseEntity.ok(Map.of(
                "RspCode", "99",
                "Message", "Unknown error: " + e.getMessage()
            ));
        }
    }
}

