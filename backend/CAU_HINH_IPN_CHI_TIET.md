# ✅ Hướng dẫn cấu hình IPN URL - Chi tiết từng bước

## Bước 1: Chạy ngrok

Mở terminal/command prompt và chạy:

```bash
ngrok http 8080
```

Bạn sẽ thấy output như sau:
```
ngrok                                                                        

Session Status                online
Account                       [Your Account]
Version                       3.x.x
Region                        [Region]
Latency                       [Latency]
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://abc123.ngrok.io -> http://localhost:8080

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

**Copy URL:** `https://abc123.ngrok.io` (thay `abc123.ngrok.io` bằng URL thực tế của bạn)

## Bước 2: Đảm bảo backend đang chạy

Trước khi cấu hình, đảm bảo backend đang chạy trên cổng 8080:

```bash
cd backend
mvnw spring-boot:run
```

## Bước 3: Cấu hình trên VNPay Dashboard

Trong trang **"Thay đổi thông tin cấu hình"**:

### 3.1. Nhập IPN URL

Trong field **"IPN Url *"**, nhập:

```
https://abc123.ngrok.io/api/v1/vnpay/ipn
```

**Lưu ý:**
- Thay `abc123.ngrok.io` bằng URL ngrok thực tế của bạn
- URL phải bắt đầu bằng `https://`
- Đảm bảo có `/api/v1/vnpay/ipn` ở cuối

### 3.2. Chọn Giao thức IPN

Trong dropdown **"Giao thức IPN *"**:
- Chọn: **GET** ✅ (đúng rồi, code của chúng ta dùng GET)

### 3.3. Chọn Kiểu mã hóa

Trong dropdown **"Kiểu mã hóa *"**:
- Chọn: **HMACSHA512** ✅ (đúng rồi, code của chúng ta dùng HMACSHA512)

## Bước 4: Test IPN URL

1. Click vào link **"Test call IPN"** (bên dưới field IPN Url)
2. VNPay sẽ gọi đến IPN URL của bạn để test
3. Kiểm tra:
   - Xem logs backend có nhận được request không
   - Xem response có đúng format không

**Response mong đợi:**
```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

## Bước 5: Lưu cấu hình

1. Click nút **"Hoàn thành"** (Complete) ở góc dưới bên phải
2. Xác nhận cấu hình đã được lưu thành công

## Checklist cấu hình

- [ ] Đã chạy ngrok: `ngrok http 8080`
- [ ] Đã copy ngrok URL
- [ ] Backend đang chạy trên cổng 8080
- [ ] Đã nhập IPN URL: `https://abc123.ngrok.io/api/v1/vnpay/ipn`
- [ ] Giao thức IPN: **GET** ✅
- [ ] Kiểu mã hóa: **HMACSHA512** ✅
- [ ] Đã test call IPN (tùy chọn)
- [ ] Đã click "Hoàn thành" để lưu

## Test sau khi cấu hình

### Test 1: Test call IPN
- Click "Test call IPN" trên dashboard
- Xem logs backend để kiểm tra có nhận được request không

### Test 2: Test payment thực tế
1. Mua một khóa học trên website
2. Thanh toán qua VNPay
3. Xem logs backend để kiểm tra IPN callback
4. Kiểm tra transaction đã được cập nhật trong database

## Xem logs backend

Khi IPN được gọi, bạn sẽ thấy logs như:

```
========================================
VNPay IPN Callback Received
Params: {vnp_Amount=1000000, vnp_BankCode=NCB, ...}
========================================
IPN processed successfully for transaction: TXN_xxx
========================================
```

## Troubleshooting

### IPN URL không được gọi
- ✅ Kiểm tra ngrok đang chạy: `ngrok http 8080`
- ✅ Kiểm tra backend đang chạy trên cổng 8080
- ✅ Kiểm tra IPN URL trên dashboard đúng với ngrok URL
- ✅ Kiểm tra firewall không chặn

### Test call IPN thất bại
- ✅ Kiểm tra backend endpoint `/api/v1/vnpay/ipn` có trả về HTTP 200
- ✅ Kiểm tra response format đúng: `{"RspCode":"00","Message":"Confirm Success"}`
- ✅ Xem logs backend để debug

### Ngrok URL thay đổi
- ⚠️ Mỗi lần restart ngrok, URL sẽ thay đổi
- ⚠️ Phải cập nhật lại IPN URL trên VNPay Dashboard
- 💡 Tip: Dùng ngrok paid plan để có static domain

## Lưu ý quan trọng

1. **IPN URL phải dùng HTTPS** - ngrok tự động cung cấp
2. **Backend phải trả về HTTP 200** với JSON response
3. **Response format phải đúng**: `{"RspCode":"00","Message":"Confirm Success"}`
4. **Ngrok URL thay đổi mỗi lần restart** - nhớ cập nhật lại

## Sẵn sàng test!

Sau khi hoàn thành các bước trên, bạn có thể:
- ✅ Test payment với VNPay
- ✅ Xem IPN callback hoạt động
- ✅ Kiểm tra transaction được cập nhật tự động

