# 📍 Hướng dẫn tìm và cấu hình IPN URL trên VNPay Dashboard

## ⚠️ QUAN TRỌNG: IPN URL KHÔNG nằm ở trang "Cài đặt thông báo"

IPN URL được cấu hình ở **"Thông tin tài khoản"** > **Chỉnh sửa Terminal**

## Các bước chi tiết:

### Bước 1: Đăng nhập VNPay Dashboard
- URL: https://sandbox.vnpayment.vn/merchantv2/
- Email: baophuc2712003@gmail.com

### Bước 2: Tìm "Thông tin tài khoản"
- Ở **góc trên bên phải** của dashboard
- Click vào **"Thông tin tài khoản"** hoặc **"Account Information"**

### Bước 3: Chọn Terminal/Website
- Danh sách các website/terminal sẽ hiển thị
- Tìm terminal có **TmnCode: PISGV29M**
- Click vào **biểu tượng chỉnh sửa** (hình cây bút ✏️) ở bên phải

### Bước 4: Cấu hình IPN URL
- Trong trang chỉnh sửa Terminal, tìm phần **"URL IPN"** hoặc **"IPN URL"**
- Nhập IPN URL của bạn (sau khi đã setup ngrok):
  ```
  https://abc123.ngrok.io/api/v1/vnpay/ipn
  ```
- **Lưu** cấu hình

## Nếu vẫn không thấy:

### Option 1: Liên hệ VNPay Support
- Hotline: *3388 / 024 38 291 291
- Email: support@vnpay.vn
- Yêu cầu: "Tôi cần cấu hình IPN URL cho terminal PISGV29M"

### Option 2: Gửi email với thông tin:
```
Chủ đề: Yêu cầu cấu hình IPN URL cho Terminal PISGV29M

Nội dung:
- Terminal ID: PISGV29M
- IPN URL: https://abc123.ngrok.io/api/v1/vnpay/ipn
- Mục đích: Test tích hợp thanh toán VNPay
```

## Lưu ý:

1. **IPN URL phải dùng HTTPS** (ngrok tự động cung cấp HTTPS)
2. **Endpoint phải trả về HTTP 200** khi nhận được callback
3. **Response format**: JSON với `RspCode` và `Message`

## Test sau khi cấu hình:

1. Chạy ngrok: `ngrok http 8080`
2. Copy ngrok URL
3. Cấu hình IPN URL trên VNPay Dashboard (theo các bước trên)
4. Thực hiện test payment
5. Xem logs backend để kiểm tra IPN callback

## Sơ đồ vị trí:

```
VNPay Dashboard
  └── Góc trên bên phải
      └── "Thông tin tài khoản" / "Account Information"
          └── Danh sách Terminal
              └── [TmnCode: PISGV29M] → Click ✏️ (chỉnh sửa)
                  └── Phần "URL IPN" / "IPN URL"
                      └── Nhập: https://abc123.ngrok.io/api/v1/vnpay/ipn
```

