# 🔄 Troubleshooting - IPN Loading xoay mãi

## Tình huống:
Sau khi click "Hoàn thành" hoặc "Test call IPN", trang cứ loading (xoay) mãi không dừng.

## Nguyên nhân có thể:

### 1. Backend chưa chạy ⚠️
- VNPay đang cố gọi đến IPN URL nhưng backend không phản hồi
- → Trang sẽ loading mãi

### 2. Backend đang chạy nhưng endpoint sai
- Endpoint `/api/v1/vnpay/ipn` không tồn tại hoặc trả về lỗi
- → VNPay không nhận được response đúng

### 3. Firewall/Network chặn
- Ngrok không thể forward request đến backend
- → Request bị timeout

## Cách xử lý:

### Bước 1: Kiểm tra backend có đang chạy

Mở terminal mới và chạy:

```bash
cd backend
mvnw spring-boot:run
```

Đảm bảo backend chạy trên cổng **8080** và thấy log:
```
Tomcat started on port(s): 8080 (http)
```

### Bước 2: Kiểm tra ngrok đang chạy

Trong terminal ngrok, đảm bảo vẫn thấy:
```
Forwarding: https://unomnipotently-presynsacral-silvana.ngrok-free.dev -> http://localhost:8080
```

### Bước 3: Test IPN URL thủ công

Mở browser và truy cập:
```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

**Kết quả mong đợi:**
- Nếu backend chạy: Sẽ thấy response hoặc error (không phải timeout)
- Nếu backend không chạy: Sẽ timeout hoặc connection refused

### Bước 4: Xem logs backend

Khi click "Test call IPN" hoặc "Hoàn thành", xem logs backend có nhận được request không:

```
========================================
VNPay IPN Callback Received
Params: {...}
========================================
```

## Nếu backend chưa chạy:

1. **Khởi động backend:**
   ```bash
   cd backend
   mvnw spring-boot:run
   ```

2. **Đợi backend khởi động xong** (thấy "Started CourseManagementSystemApplication")

3. **Thử lại trên VNPay Dashboard:**
   - Click "Test call IPN" lại
   - Hoặc refresh trang và click "Hoàn thành" lại

## Nếu backend đã chạy nhưng vẫn loading:

1. **Kiểm tra endpoint có đúng không:**
   - Truy cập: `http://localhost:8080/api/v1/vnpay/ipn`
   - Phải thấy response (không phải 404)

2. **Kiểm tra ngrok:**
   - Truy cập: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn`
   - Phải forward được đến backend

3. **Xem logs backend:**
   - Có nhận được request từ VNPay không?
   - Có lỗi gì không?

## Lưu ý:

- ⚠️ **Backend PHẢI chạy** trước khi test IPN
- ⚠️ **Ngrok PHẢI chạy** để VNPay có thể gọi đến
- ⚠️ **Cả 2 phải chạy đồng thời** khi test

## Checklist:

- [ ] Backend đang chạy trên cổng 8080
- [ ] Ngrok đang chạy và forward đến localhost:8080
- [ ] Endpoint `/api/v1/vnpay/ipn` có thể truy cập được
- [ ] Logs backend sẵn sàng để xem request

## Sau khi backend chạy:

1. Refresh trang VNPay Dashboard
2. Click "Hoàn thành" lại
3. Hoặc click "Test call IPN" để test

