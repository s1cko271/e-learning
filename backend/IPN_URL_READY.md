# ✅ IPN URL sẵn sàng - Cấu hình ngay!

## Ngrok URL của bạn:

```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev
```

## IPN URL cần nhập vào VNPay Dashboard:

```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

## Các bước tiếp theo:

### 1. Vào VNPay Dashboard
- Trang: **"Thay đổi thông tin cấu hình"**
- Field: **"IPN Url *"**

### 2. Nhập IPN URL
Copy và paste vào field **"IPN Url *"**:

```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

### 3. Kiểm tra cấu hình
- **Giao thức IPN**: GET ✅
- **Kiểu mã hóa**: HMACSHA512 ✅

### 4. Test IPN (tùy chọn)
- Click **"Test call IPN"** để kiểm tra
- Xem logs backend có nhận được request không

### 5. Lưu
- Click **"Hoàn thành"** để lưu cấu hình

## Đảm bảo backend đang chạy

Trước khi test, đảm bảo backend đang chạy:

```bash
cd backend
mvnw spring-boot:run
```

Backend phải chạy trên cổng **8080** để ngrok có thể forward request.

## Test IPN URL

### Cách 1: Test từ VNPay Dashboard
1. Click **"Test call IPN"** trên dashboard
2. Xem logs backend để kiểm tra có nhận được request không

### Cách 2: Test bằng curl (tùy chọn)
```bash
curl "https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn?vnp_Amount=1000000&vnp_BankCode=NCB&vnp_ResponseCode=00&vnp_TxnRef=TEST123&vnp_SecureHash=test"
```

## Xem logs backend

Khi IPN được gọi, bạn sẽ thấy:

```
========================================
VNPay IPN Callback Received
Params: {...}
========================================
IPN processed successfully for transaction: ...
========================================
```

## Lưu ý quan trọng

⚠️ **Ngrok URL sẽ thay đổi khi restart ngrok**
- Mỗi lần restart ngrok, URL sẽ khác
- Phải cập nhật lại IPN URL trên VNPay Dashboard

⚠️ **Giữ ngrok chạy khi test**
- Đừng tắt ngrok khi đang test payment
- Nếu tắt, IPN URL sẽ không hoạt động

⚠️ **Backend phải chạy trên cổng 8080**
- Ngrok đang forward đến `http://localhost:8080`
- Backend phải chạy đúng cổng này

## Sẵn sàng test!

Sau khi nhập IPN URL và lưu, bạn có thể:
- ✅ Test payment với VNPay
- ✅ Xem IPN callback hoạt động
- ✅ Kiểm tra transaction được cập nhật tự động

