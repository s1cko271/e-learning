# Cấu hình VNPay - Thông tin thực tế

## Thông tin cấu hình đã được cập nhật

✅ **Terminal ID (vnp_TmnCode)**: `PISGV29M`  
✅ **Secret Key (vnp_HashSecret)**: `DRC0V9AAYA651P2SID7SVYRY46HND1H4`  
✅ **URL thanh toán**: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`

## Các bước cấu hình

### 1. Cập nhật application.properties

File `backend/src/main/resources/application.properties` đã được cập nhật với thông tin trên.

Nếu bạn chưa có file `application.properties`, hãy copy từ `application.properties.example`:

```bash
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
```

### 2. Cấu hình IPN URL trên VNPay Dashboard

**Quan trọng**: IPN URL là bắt buộc để VNPay có thể thông báo kết quả thanh toán.

1. Đăng nhập vào VNPay Merchant Admin:
   - URL: https://sandbox.vnpayment.vn/merchantv2/
   - Email: baophuc2712003@gmail.com
   - Mật khẩu: (mật khẩu bạn đã đăng ký)

2. Vào phần **Cấu hình** > **IPN URL**

3. Nhập IPN URL:
   - **Local development**: Sử dụng ngrok để expose localhost
     ```bash
     ngrok http 8080
     # Sau đó sử dụng URL ngrok: https://xxxx.ngrok.io/api/v1/vnpay/ipn
     ```
   - **Production**: `https://your-domain.com/api/v1/vnpay/ipn`

4. Lưu cấu hình

### 3. Test với thẻ test

VNPay cung cấp thẻ test để kiểm thử:

- **Ngân hàng**: NCB
- **Số thẻ**: 9704198526191432198
- **Tên chủ thẻ**: NGUYEN VAN A
- **Ngày phát hành**: 07/15
- **Mật khẩu OTP**: 123456

### 4. Test case (SIT)

Để test các kịch bản thanh toán:

1. Truy cập: https://sandbox.vnpayment.vn/vnpaygw-sit-testing/user/login
2. Đăng nhập với:
   - Email: baophuc2712003@gmail.com
   - Mật khẩu: (mật khẩu đã đăng ký)

## Các endpoint đã được tạo

### Backend:
- ✅ `POST /api/v1/payment/create` - Tạo payment URL với QR code
- ✅ `GET /api/v1/vnpay/return` - Xử lý ReturnURL từ VNPay
- ✅ `GET /api/v1/vnpay/ipn` - Xử lý IPN URL từ VNPay (server-to-server)

### Frontend:
- ✅ `/payment/vnpay-return` - Trang hiển thị kết quả thanh toán

## Cách test thanh toán

1. **Khởi động backend**:
   ```bash
   cd backend
   mvnw spring-boot:run
   ```

2. **Khởi động frontend**:
   ```bash
   cd frontend
   npm run dev
   ```

3. **Setup ngrok** (để test IPN URL):
   ```bash
   ngrok http 8080
   # Copy URL ngrok và cập nhật vào VNPay Dashboard IPN URL
   ```

4. **Test thanh toán**:
   - Mua một khóa học
   - Chọn thanh toán
   - Hệ thống sẽ redirect đến VNPay với QR code
   - Quét QR code bằng app VNPay hoặc sử dụng thẻ test

## Lưu ý quan trọng

⚠️ **IPN URL phải có thể truy cập từ internet**
- Localhost không hoạt động cho IPN URL
- Phải sử dụng ngrok hoặc deploy lên server để test IPN

⚠️ **Checksum verification**
- Hệ thống đã tự động verify checksum từ VNPay
- Đảm bảo HashSecret đúng trong application.properties

⚠️ **Transaction Code**
- Hệ thống sử dụng transactionCode làm `vnp_TxnRef`
- Đảm bảo transactionCode là unique

## Troubleshooting

### IPN URL không được gọi
- Kiểm tra IPN URL có thể truy cập từ internet
- Kiểm tra firewall/security group
- Xem logs backend để debug

### Lỗi "Invalid signature"
- Kiểm tra HashSecret trong application.properties
- Đảm bảo HashSecret đúng với thông tin từ VNPay

### Payment URL không hoạt động
- Kiểm tra TmnCode và HashSecret
- Kiểm tra URL VNPay có đúng không
- Xem logs backend để xem lỗi chi tiết

## Tài liệu tham khảo

- Tài liệu tích hợp: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
- Code demo: https://sandbox.vnpayment.vn/apis/vnpay-demo/code-demo-tích-hợp

