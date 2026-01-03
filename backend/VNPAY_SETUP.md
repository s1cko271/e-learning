# Hướng dẫn cấu hình VNPay QR Payment

## Bước 1: Đăng ký tài khoản VNPay Sandbox

1. Truy cập: https://sandbox.vnpayment.vn/devreg/
2. Đăng ký tài khoản và nhận thông tin:
   - **TmnCode**: Mã định danh merchant (ví dụ: `2QXUI4J4`)
   - **HashSecret**: Chuỗi bí mật để tạo checksum

## Bước 2: Cấu hình trong application.properties

Thêm các dòng sau vào `backend/src/main/resources/application.properties`:

```properties
# VNPay Configuration
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:3000/payment/vnpay-return
vnpay.ipn-url=http://localhost:8080/api/v1/vnpay/ipn
vnpay.tmn-code=YOUR_TMN_CODE_HERE
vnpay.hash-secret=YOUR_HASH_SECRET_HERE
```

**Lưu ý:**
- Thay `YOUR_TMN_CODE_HERE` bằng TmnCode bạn nhận được từ VNPay
- Thay `YOUR_HASH_SECRET_HERE` bằng HashSecret bạn nhận được từ VNPay
- `vnpay.return-url`: URL frontend để VNPay redirect user về sau khi thanh toán
- `vnpay.ipn-url`: URL backend để VNPay gọi server-to-server (quan trọng!)

## Bước 3: Cấu hình IPN URL trên VNPay Dashboard

1. Đăng nhập vào VNPay Sandbox Dashboard
2. Vào phần **Cấu hình** > **IPN URL**
3. Nhập IPN URL: `http://your-domain.com/api/v1/vnpay/ipn`
   - **Local development**: Sử dụng ngrok hoặc công cụ tương tự để expose localhost
   - **Production**: Sử dụng domain thực của bạn

## Bước 4: Test thanh toán

1. Khởi động backend: `mvnw spring-boot:run`
2. Khởi động frontend: `npm run dev`
3. Mua một khóa học và chọn thanh toán
4. Hệ thống sẽ redirect đến VNPay với QR code
5. Quét QR code bằng app VNPay để thanh toán

## Các endpoint đã được tạo

### Backend:
- `POST /api/v1/payment/create` - Tạo payment URL
- `GET /api/v1/vnpay/return` - Xử lý ReturnURL từ VNPay
- `GET /api/v1/vnpay/ipn` - Xử lý IPN URL từ VNPay (server-to-server)

### Frontend:
- `/payment/vnpay-return` - Trang hiển thị kết quả thanh toán

## Mã lỗi VNPay

| Mã | Mô tả |
|---|---|
| 00 | Giao dịch thành công |
| 07 | Giao dịch bị nghi ngờ |
| 09 | Thẻ/Tài khoản chưa đăng ký InternetBanking |
| 10 | Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần |
| 11 | Đã hết hạn chờ thanh toán |
| 12 | Thẻ/Tài khoản bị khóa |
| 13 | Nhập sai mật khẩu xác thực giao dịch (OTP) |
| 24 | Khách hàng hủy giao dịch |
| 51 | Tài khoản không đủ số dư |
| 65 | Tài khoản đã vượt quá hạn mức giao dịch trong ngày |
| 75 | Ngân hàng thanh toán đang bảo trì |
| 79 | Nhập sai mật khẩu thanh toán quá số lần quy định |

## Lưu ý quan trọng

1. **IPN URL là bắt buộc**: VNPay sẽ gọi IPN URL để thông báo kết quả thanh toán. Đây là cách duy nhất để đảm bảo thanh toán được xử lý ngay cả khi user không quay lại ReturnURL.

2. **Checksum verification**: Luôn verify checksum từ VNPay để đảm bảo dữ liệu không bị giả mạo.

3. **Transaction Code**: Hệ thống sử dụng transactionCode làm `vnp_TxnRef` để match với database khi callback.

4. **QR Code Payment**: Đã cấu hình mặc định sử dụng `VNPAYQR` để thanh toán bằng QR code.

## Troubleshooting

### Lỗi "Invalid signature"
- Kiểm tra lại HashSecret trong `application.properties`
- Đảm bảo HashSecret đúng với thông tin từ VNPay Dashboard

### IPN URL không được gọi
- Kiểm tra IPN URL có thể truy cập được từ internet (không phải localhost)
- Sử dụng ngrok để expose localhost: `ngrok http 8080`
- Cập nhật IPN URL trên VNPay Dashboard

### Payment URL không hoạt động
- Kiểm tra TmnCode và HashSecret
- Kiểm tra URL VNPay có đúng không
- Kiểm tra logs backend để xem lỗi chi tiết

