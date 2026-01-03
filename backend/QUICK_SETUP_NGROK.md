# 🚀 Quick Setup - Ngrok cho VNPay IPN

## TL;DR - Tóm tắt nhanh

✅ **CHỈ CẦN 1 ngrok tunnel cho cổng 8080**  
✅ **ReturnURL dùng localhost:3000 (KHÔNG cần ngrok)**  
✅ **IPN URL cần ngrok URL**

## Bước 1: Chạy ngrok

```bash
ngrok http 8080
```

Bạn sẽ thấy output như:
```
Forwarding: https://abc123.ngrok.io -> http://localhost:8080
```

## Bước 2: Copy ngrok URL

Copy URL: `https://abc123.ngrok.io`

## Bước 3: Tìm chỗ cấu hình IPN URL trên VNPay Dashboard

### Cách tìm:
1. Đăng nhập: https://sandbox.vnpayment.vn/merchantv2/
2. Tìm menu **"Cài đặt thông báo"** hoặc **"Notification Settings"**
   - Thường nằm trong menu **CÔNG CỤ** (TOOLS) bên trái
3. Hoặc tìm **"Cấu hình"** / **"Settings"** > **"Thông tin kết nối"**

### Nhập IPN URL:
```
https://abc123.ngrok.io/api/v1/vnpay/ipn
```
(Thay `abc123.ngrok.io` bằng URL ngrok của bạn)

## Bước 4: Cấu hình ReturnURL (KHÔNG cần ngrok)

Trong file `application.properties`, ReturnURL vẫn dùng localhost:

```properties
vnpay.return-url=http://localhost:3000/payment/vnpay-return
```

## Tại sao?

| URL | Ai gọi? | Cần ngrok? |
|-----|---------|------------|
| **IPN URL** | VNPay Server → Backend | ✅ **CÓ** (server-to-server) |
| **ReturnURL** | Browser redirect | ❌ **KHÔNG** (client-side) |

## Sơ đồ luồng

```
1. User mua khóa học
   ↓
2. Backend tạo payment URL với:
   - ReturnURL: http://localhost:3000/payment/vnpay-return (KHÔNG cần ngrok)
   - IPN URL: https://abc123.ngrok.io/api/v1/vnpay/ipn (CẦN ngrok)
   ↓
3. User thanh toán trên VNPay
   ↓
4. VNPay gọi IPN URL (server-to-server) → Cần ngrok
   ↓
5. VNPay redirect browser về ReturnURL → localhost OK
```

## Lưu ý

⚠️ **Ngrok URL thay đổi mỗi lần restart**
- Mỗi lần restart ngrok, phải cập nhật lại IPN URL trên VNPay Dashboard
- Hoặc dùng ngrok paid plan để có static domain

⚠️ **Chỉ cần 1 ngrok tunnel**
- Chỉ expose cổng 8080 (backend)
- Không cần expose cổng 3000 (frontend)

## Test

1. Chạy ngrok: `ngrok http 8080`
2. Copy ngrok URL
3. Cập nhật IPN URL trên VNPay Dashboard
4. Thực hiện test payment
5. Xem logs backend để kiểm tra IPN callback

