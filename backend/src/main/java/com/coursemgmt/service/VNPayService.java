package com.coursemgmt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service tích hợp VNPay Payment Gateway
 * Documentation: https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/
 */
@Service
public class VNPayService {

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    @Value("${vnpay.return-url:http://localhost:3000/payment/callback}")
    private String vnpReturnUrl;
    
    @Value("${vnpay.tmn-code:YOUR_TMN_CODE}")
    private String vnpTmnCode;
    
    @Value("${vnpay.hash-secret:YOUR_HASH_SECRET}")
    private String vnpHashSecret;

    /**
     * Tạo URL thanh toán VNPay
     * @param transactionCode Transaction code (unique identifier)
     * @param amount Số tiền (VND)
     * @param orderInfo Thông tin đơn hàng
     * @param returnUrl URL trả về sau thanh toán
     * @param bankCode Mã ngân hàng (null = để user chọn, "VNPAYQR" = QR code)
     */
    public String createPaymentUrl(
        String transactionCode,
        Double amount,
        String orderInfo,
        String returnUrl,
        String bankCode
    ) throws UnsupportedEncodingException {
        
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf((long)(amount * 100))); // VNPay yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        // Transaction reference: sử dụng transaction code
        vnpParams.put("vnp_TxnRef", transactionCode);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl != null ? returnUrl : vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        
        // Bank code (optional)
        // VNPAYQR = Thanh toán bằng QR code
        // null = để user chọn phương thức thanh toán
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParams.put("vnp_BankCode", bankCode);
        }
        
        // Create date
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        // Expire date (15 minutes)
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Build query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        // Generate secure hash
        String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);
        
        return vnpUrl + "?" + query.toString();
    }

    /**
     * Verify payment callback signature
     */
    public boolean verifyPaymentSignature(Map<String, String> params) {
        // Handle empty params (test call)
        if (params == null || params.isEmpty()) {
            System.out.println("WARNING: Empty params - cannot verify signature");
            return false;
        }
        
        String vnpSecureHash = params.get("vnp_SecureHash");
        
        // If no SecureHash, cannot verify (might be test call)
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            System.out.println("WARNING: No vnp_SecureHash found in params");
            return false;
        }
        
        // Create a copy to avoid modifying original map
        Map<String, String> paramsCopy = new HashMap<>(params);
        paramsCopy.remove("vnp_SecureHash");
        paramsCopy.remove("vnp_SecureHashType");
        
        // If no params left after removing SecureHash, cannot verify
        if (paramsCopy.isEmpty()) {
            System.out.println("WARNING: No params to verify (only SecureHash present)");
            return false;
        }
        
        // Sort params
        List<String> fieldNames = new ArrayList<>(paramsCopy.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        try {
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
                String fieldValue = paramsCopy.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                    // URL encode fieldName and fieldValue (same as when creating payment URL)
                    hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR: Failed to URL encode for signature verification: " + e.getMessage());
            return false;
        }
        
        String calculatedHash = hmacSHA512(vnpHashSecret, hashData.toString());
        boolean isValid = calculatedHash.equals(vnpSecureHash);
        
        if (!isValid) {
            System.out.println("Signature verification failed:");
            System.out.println("  Expected: " + vnpSecureHash);
            System.out.println("  Calculated: " + calculatedHash);
            System.out.println("  Hash data: " + hashData.toString());
        }
        
        return isValid;
    }

    /**
     * HMAC SHA512 helper
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA512"
            );
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512: " + e.getMessage());
        }
    }
}

