# Hướng dẫn cấu hình IPN URL trên VNPay Dashboard

## Tìm chỗ cấu hình IPN URL

Trên VNPay Dashboard, IPN URL thường nằm ở một trong các vị trí sau:

### Cách 1: Qua menu "Cài đặt thông báo"
1. Đăng nhập vào: https://sandbox.vnpayment.vn/merchantv2/
2. Vào menu bên trái: **CÔNG CỤ** > **Cài đặt thông báo** (Notification Settings)
3. Tìm phần **IPN URL** hoặc **URL thông báo kết quả**
4. Nhập IPN URL của bạn

### Cách 2: Qua menu "Cấu hình" hoặc "Settings"
1. Tìm menu **Cấu hình** hoặc **Settings** (có thể ở header hoặc sidebar)
2. Vào phần **Thông tin kết nối** hoặc **Connection Settings**
3. Tìm **IPN URL** hoặc **Callback URL**

### Cách 3: Liên hệ VNPay Support
Nếu không tìm thấy, có thể:
- Gửi email cho VNPay support với thông tin merchant
- Hoặc gọi hotline: *3388 / 024 38 291 291

## Cấu hình ngrok - CHỈ CẦN CỔNG 8080

### Tại sao chỉ cần cổng 8080?

- **IPN URL** (server-to-server): VNPay gọi trực tiếp đến backend → Cần ngrok expose cổng 8080
- **ReturnURL** (client redirect): VNPay redirect browser về frontend → Có thể dùng localhost:3000 (không cần ngrok)

### Các bước setup ngrok:

#### Bước 1: Cài đặt ngrok
- Download: https://ngrok.com/download
- Hoặc dùng chocolatey: `choco install ngrok`

#### Bước 2: Chạy ngrok expose cổng 8080
```bash
ngrok http 8080
```

#### Bước 3: Copy URL ngrok
Sau khi chạy, bạn sẽ thấy:
```
Forwarding: https://abc123.ngrok.io -> http://localhost:8080
```

Copy URL: `https://abc123.ngrok.io`

#### Bước 4: Cấu hình IPN URL trên VNPay Dashboard
Nhập vào IPN URL:
```
https://abc123.ngrok.io/api/v1/vnpay/ipn
```

#### Bước 5: Cấu hình ReturnURL trong code
ReturnURL có thể dùng localhost (không cần ngrok):
```properties
vnpay.return-url=http://localhost:3000/payment/vnpay-return
```

## Tóm tắt

| URL | Loại | Cần ngrok? | Giá trị |
|-----|------|------------|---------|
| **IPN URL** | Server-to-server | ✅ **CÓ** | `https://abc123.ngrok.io/api/v1/vnpay/ipn` |
| **ReturnURL** | Client redirect | ❌ **KHÔNG** | `http://localhost:3000/payment/vnpay-return` |

## Lưu ý quan trọng

1. **Chỉ cần 1 ngrok tunnel** cho cổng 8080
2. **ReturnURL không cần ngrok** vì browser tự redirect về localhost
3. **IPN URL BẮT BUỘC phải có ngrok** vì VNPay server cần gọi đến backend của bạn
4. **Ngrok URL thay đổi mỗi lần restart** (trừ khi dùng ngrok paid plan)
   - Mỗi lần restart ngrok, phải cập nhật lại IPN URL trên VNPay Dashboard

## Test IPN URL

Sau khi cấu hình, test bằng cách:
1. Thực hiện một giao dịch thanh toán test
2. Xem logs backend để kiểm tra IPN callback có được gọi không
3. Kiểm tra VNPay Dashboard > Giao dịch để xem trạng thái

## Troubleshooting

### IPN URL không được gọi
- ✅ Kiểm tra ngrok đang chạy: `ngrok http 8080`
- ✅ Kiểm tra backend đang chạy trên cổng 8080
- ✅ Kiểm tra IPN URL trên VNPay Dashboard đúng với ngrok URL
- ✅ Kiểm tra firewall không chặn

### Ngrok URL thay đổi
- Mỗi lần restart ngrok, URL sẽ thay đổi
- Phải cập nhật lại IPN URL trên VNPay Dashboard
- Hoặc dùng ngrok paid plan để có static domain

