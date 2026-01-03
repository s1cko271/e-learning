# 🔧 Hướng dẫn cấu hình IPN URL cho website của bạn

## Bước 1: Tìm Terminal của bạn trong danh sách

Trong trang **"Thông tin tài khoản"** > **"Danh sách website"**:

1. Tìm terminal có **Mã website (TmnCode): `PISGV29M`**
   - Nếu không thấy, có thể terminal này chưa được tạo hoặc có tên khác
   - Tìm trong danh sách, cuộn xuống nếu cần

2. Hoặc tìm terminal có **Tên website** liên quan đến project của bạn
   - Ví dụ: "E-Learning Platform" hoặc tên bạn đã đăng ký

## Bước 2: Click vào nút Sửa (✏️)

- Ở cột **"Thao tác"** (Actions), click vào **biểu tượng cây bút** (✏️)
- Trang chỉnh sửa terminal sẽ mở ra

## Bước 3: Tìm và cấu hình IPN URL

Trong trang chỉnh sửa terminal, tìm phần:

- **"URL IPN"** hoặc **"IPN URL"** hoặc **"URL thông báo kết quả"**
- Có thể nằm trong tab **"Cấu hình"** hoặc **"Thông tin kết nối"**

## Bước 4: Nhập IPN URL (sau khi đã chạy ngrok)

### Trước tiên, chạy ngrok:

```bash
ngrok http 8080
```

Bạn sẽ thấy output:
```
Forwarding: https://abc123.ngrok.io -> http://localhost:8080
```

### Copy ngrok URL và nhập vào IPN URL:

```
https://abc123.ngrok.io/api/v1/vnpay/ipn
```

**Lưu ý:**
- Thay `abc123.ngrok.io` bằng URL ngrok thực tế của bạn
- Đảm bảo backend đang chạy trên cổng 8080
- IPN URL phải dùng HTTPS (ngrok tự động cung cấp)

## Bước 5: Lưu cấu hình

- Click nút **"Lưu"** hoặc **"Cập nhật"**
- Xác nhận cấu hình đã được lưu

## Nếu không thấy terminal PISGV29M

### Option 1: Tạo terminal mới
- Tìm nút **"Thêm mới"** hoặc **"Tạo terminal"**
- Điền thông tin:
  - Tên website: "E-Learning Platform" (hoặc tên bạn muốn)
  - Domain: localhost (cho test)
  - Sau đó cấu hình IPN URL

### Option 2: Sử dụng terminal có sẵn
- Nếu có terminal khác trong danh sách, có thể dùng terminal đó
- Lưu ý: Cần dùng đúng TmnCode và HashSecret tương ứng

## Checklist

- [ ] Đã tìm thấy terminal PISGV29M (hoặc terminal khác)
- [ ] Đã click nút sửa (✏️)
- [ ] Đã tìm thấy phần IPN URL
- [ ] Đã chạy ngrok: `ngrok http 8080`
- [ ] Đã copy ngrok URL
- [ ] Đã nhập IPN URL: `https://abc123.ngrok.io/api/v1/vnpay/ipn`
- [ ] Đã lưu cấu hình

## Test sau khi cấu hình

1. Đảm bảo ngrok đang chạy
2. Đảm bảo backend đang chạy trên cổng 8080
3. Thực hiện test payment
4. Xem logs backend để kiểm tra IPN callback có được gọi không

## Lưu ý quan trọng

⚠️ **Ngrok URL thay đổi mỗi lần restart**
- Mỗi lần restart ngrok, URL sẽ thay đổi
- Phải cập nhật lại IPN URL trên VNPay Dashboard

⚠️ **IPN URL phải trả về HTTP 200**
- Backend endpoint `/api/v1/vnpay/ipn` phải trả về JSON:
  ```json
  {
    "RspCode": "00",
    "Message": "Confirm Success"
  }
  ```

## Troubleshooting

### Không thấy phần IPN URL trong trang chỉnh sửa
- Thử tìm trong các tab khác (Cấu hình, Thông tin kết nối, v.v.)
- Hoặc liên hệ VNPay support: *3388 / 024 38 291 291

### IPN URL không được gọi
- Kiểm tra ngrok đang chạy
- Kiểm tra backend đang chạy
- Kiểm tra IPN URL trên dashboard đúng với ngrok URL
- Xem logs backend để debug

