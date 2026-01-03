# 🔍 Kiểm tra IPN Callback - Xem logs backend

## Backend đang chạy ✅

Backend đã chạy trên cổng 8080 (Process ID: 6508)

## Các bước kiểm tra:

### 1. Xem logs backend

Trong terminal đang chạy backend, bạn sẽ thấy logs khi VNPay gọi IPN:

```
========================================
VNPay IPN Callback Received
Params: {vnp_Amount=..., vnp_BankCode=..., ...}
========================================
IPN processed successfully for transaction: ...
========================================
```

### 2. Nếu không thấy logs:

Có thể VNPay chưa gọi được đến backend. Thử:

**Option A: Refresh trang VNPay Dashboard**
- Click F5 hoặc refresh
- Thử click "Hoàn thành" lại

**Option B: Test IPN URL thủ công**
Mở browser và truy cập:
```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn?vnp_Amount=1000000&vnp_ResponseCode=00&vnp_TxnRef=TEST123&vnp_SecureHash=test
```

Nếu thấy response hoặc error (không phải timeout) → Endpoint hoạt động ✅

### 3. Kiểm tra ngrok

Trong terminal ngrok, xem có request nào không:
- Truy cập: http://127.0.0.1:4040 (ngrok web interface)
- Xem tab "Requests" để thấy các request đã được forward

### 4. Nếu vẫn loading mãi:

**Có thể do:**
- VNPay đang chờ response từ backend
- Backend chưa trả về response đúng format
- Network timeout

**Giải pháp:**
1. Đợi thêm vài giây (có thể VNPay đang retry)
2. Refresh trang VNPay Dashboard
3. Xem logs backend có lỗi gì không
4. Kiểm tra response format có đúng không

## Response format cần thiết:

Backend phải trả về JSON:
```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

## Nếu thấy lỗi trong logs:

- **Invalid signature**: Kiểm tra HashSecret
- **Transaction not found**: Kiểm tra vnp_TxnRef
- **500 Internal Server Error**: Xem logs chi tiết để debug

## Tóm tắt:

1. ✅ Backend đang chạy
2. ⏳ Đang chờ VNPay gọi IPN
3. 👀 Xem logs backend để kiểm tra
4. 🔄 Nếu loading quá lâu, refresh và thử lại

