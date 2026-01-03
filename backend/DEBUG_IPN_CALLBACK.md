# 🔍 Debug IPN Callback - Kiểm tra VNPay có gọi đến không

## Tình trạng hiện tại:

✅ **Backend đang chạy** trên cổng 8080  
✅ **Ngrok đang chạy** và forward đến localhost:8080  
❓ **Chưa thấy logs IPN callback** từ VNPay

## Các bước kiểm tra:

### 1. Kiểm tra ngrok web interface

Mở browser và truy cập:
```
http://127.0.0.1:4040
```

Xem tab **"Requests"** để kiểm tra:
- Có request nào từ VNPay đến IPN URL không?
- Request có thành công (200) hay bị lỗi (4xx, 5xx)?

### 2. Test IPN URL thủ công

Mở browser và truy cập:
```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

**Kết quả mong đợi:**
- Nếu endpoint hoạt động: Sẽ thấy response hoặc error (không phải timeout)
- Nếu endpoint không hoạt động: Sẽ timeout hoặc connection refused

### 3. Kiểm tra endpoint có đúng không

Test trực tiếp trên localhost:
```
http://localhost:8080/api/v1/vnpay/ipn
```

**Kết quả:**
- Nếu thấy response hoặc error → Endpoint hoạt động ✅
- Nếu 404 → Endpoint không tồn tại ❌

### 4. Xem logs backend khi VNPay gọi

Khi VNPay gọi IPN, bạn sẽ thấy logs:

```
========================================
VNPay IPN Callback Received
Params: {vnp_Amount=..., vnp_BankCode=..., ...}
========================================
IPN processed successfully for transaction: ...
========================================
```

## Nếu VNPay chưa gọi:

### Nguyên nhân có thể:

1. **VNPay đang đợi response từ lần test trước**
   - → Refresh trang VNPay Dashboard
   - → Đợi thêm vài giây

2. **IPN URL chưa được lưu đúng**
   - → Kiểm tra lại IPN URL trên VNPay Dashboard
   - → Đảm bảo URL đầy đủ: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn`

3. **VNPay đang retry**
   - → VNPay có thể retry sau 5 phút nếu lần đầu thất bại
   - → Xem logs backend sau vài phút

4. **Network/Firewall chặn**
   - → Kiểm tra ngrok có thể truy cập từ internet không
   - → Test ngrok URL từ browser khác

## Cách test nhanh:

### Test 1: Test endpoint trực tiếp
```bash
curl http://localhost:8080/api/v1/vnpay/ipn
```

### Test 2: Test qua ngrok
```bash
curl https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

### Test 3: Xem ngrok requests
Truy cập: http://127.0.0.1:4040 và xem tab "Requests"

## Lưu ý:

- ⚠️ **Lỗi "NoResourceFoundException"** là bình thường, không ảnh hưởng
- ⚠️ **VNPay có thể đợi vài giây** trước khi gọi IPN
- ⚠️ **Nếu loading quá lâu**, refresh trang VNPay Dashboard và thử lại

## Sau khi kiểm tra:

1. Nếu endpoint hoạt động → VNPay sẽ gọi được
2. Nếu endpoint không hoạt động → Kiểm tra code và cấu hình
3. Nếu VNPay đã gọi nhưng không thấy logs → Kiểm tra security config

