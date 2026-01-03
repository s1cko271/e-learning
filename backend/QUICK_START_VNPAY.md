# 🚀 Quick Start - VNPay QR Payment

## Bước 1: Tạo file application.properties

Nếu chưa có file `application.properties`, copy từ file example:

```bash
# Windows
copy backend\src\main\resources\application.properties.example backend\src\main\resources\application.properties

# Linux/Mac
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
```

## Bước 2: Kiểm tra cấu hình VNPay

File `application.properties.example` đã được cập nhật với thông tin VNPay:

```properties
# VNPay Configuration
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:3000/payment/vnpay-return
vnpay.ipn-url=http://localhost:8080/api/v1/vnpay/ipn
vnpay.tmn-code=PISGV29M
vnpay.hash-secret=DRC0V9AAYA651P2SID7SVYRY46HND1H4
```

✅ **Đã cấu hình sẵn!** Không cần thay đổi gì nếu bạn dùng localhost.

## Bước 3: Cấu hình IPN URL (QUAN TRỌNG!)

IPN URL cho phép VNPay thông báo kết quả thanh toán (server-to-server).

### Option 1: Sử dụng ngrok (cho local development)

1. **Cài đặt ngrok**: https://ngrok.com/download
2. **Chạy ngrok**:
   ```bash
   ngrok http 8080
   ```
3. **Copy URL ngrok** (ví dụ: `https://abc123.ngrok.io`)
4. **Cập nhật IPN URL trên VNPay Dashboard**:
   - Đăng nhập: https://sandbox.vnpayment.vn/merchantv2/
   - Email: baophuc2712003@gmail.com
   - Vào **Cấu hình** > **IPN URL**
   - Nhập: `https://abc123.ngrok.io/api/v1/vnpay/ipn`
   - Lưu

### Option 2: Deploy lên server (cho production)

1. Deploy backend lên server có domain
2. Cập nhật IPN URL: `https://your-domain.com/api/v1/vnpay/ipn`

## Bước 4: Test thanh toán

### 1. Khởi động backend:
```bash
cd backend
mvnw spring-boot:run
```

### 2. Khởi động frontend:
```bash
cd frontend
npm run dev
```

### 3. Test với thẻ test VNPay:

- **Ngân hàng**: NCB
- **Số thẻ**: `9704198526191432198`
- **Tên chủ thẻ**: NGUYEN VAN A
- **Ngày phát hành**: `07/15`
- **Mật khẩu OTP**: `123456`

### 4. Quy trình test:

1. Mở http://localhost:3000
2. Đăng nhập/Đăng ký tài khoản
3. Chọn một khóa học và click "Mua khóa học"
4. Hệ thống sẽ redirect đến VNPay với QR code
5. **Option A**: Quét QR code bằng app VNPay
6. **Option B**: Click "Thanh toán bằng thẻ" và nhập thông tin thẻ test
7. Nhập OTP: `123456`
8. Thanh toán thành công → Redirect về `/payment/vnpay-return`

## Kiểm tra logs

### Backend logs:
- Xem console để thấy logs khi tạo payment URL
- Xem logs khi nhận callback từ VNPay (ReturnURL và IPN)

### Frontend:
- Mở DevTools (F12) để xem network requests
- Kiểm tra response từ `/api/v1/payment/create`

## Troubleshooting

### ❌ Lỗi "Invalid signature"
- ✅ Đã kiểm tra: HashSecret đúng trong application.properties
- ✅ Kiểm tra lại: `vnpay.hash-secret=DRC0V9AAYA651P2SID7SVYRY46HND1H4`

### ❌ IPN URL không được gọi
- ✅ Đảm bảo ngrok đang chạy
- ✅ Kiểm tra IPN URL trên VNPay Dashboard đúng với ngrok URL
- ✅ Kiểm tra firewall không chặn port 8080

### ❌ Payment URL không hoạt động
- ✅ Kiểm tra backend đang chạy trên port 8080
- ✅ Kiểm tra TmnCode: `PISGV29M`
- ✅ Xem logs backend để debug

## Test case scenarios

Truy cập: https://sandbox.vnpayment.vn/vnpaygw-sit-testing/user/login
- Email: baophuc2712003@gmail.com
- Test các kịch bản thanh toán khác nhau

## ✅ Checklist

- [ ] File `application.properties` đã được tạo
- [ ] VNPay config đã đúng (TmnCode, HashSecret)
- [ ] Ngrok đang chạy (nếu test local)
- [ ] IPN URL đã được cấu hình trên VNPay Dashboard
- [ ] Backend đang chạy trên port 8080
- [ ] Frontend đang chạy trên port 3000
- [ ] Đã test thanh toán với thẻ test

## 🎉 Sẵn sàng demo!

Sau khi hoàn thành các bước trên, bạn có thể:
- ✅ Tạo payment URL với QR code
- ✅ Xử lý ReturnURL từ VNPay
- ✅ Xử lý IPN URL từ VNPay
- ✅ Tự động tạo enrollment sau khi thanh toán thành công

