# ✅ Kiểm tra cấu hình VNPay với ngrok

## Cấu hình hiện tại trong application.properties:

```properties
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html ✅
vnpay.return-url=http://localhost:3000/payment/vnpay-return ✅
vnpay.ipn-url=http://localhost:8080/api/v1/vnpay/ipn (không dùng)
vnpay.tmn-code=PISGV29M ✅
vnpay.hash-secret=DRC0V9AAYA651P2SID7SVYRY46HND1H4 ✅
```

## Phân tích:

### ✅ Đúng - Không cần thay đổi:

1. **vnpay.url**: URL VNPay sandbox - ĐÚNG ✅
2. **vnpay.return-url**: `http://localhost:3000/payment/vnpay-return` - ĐÚNG ✅
   - ReturnURL là browser redirect, KHÔNG cần ngrok
   - Browser tự redirect về localhost:3000
3. **vnpay.tmn-code**: `PISGV29M` - ĐÚNG ✅
4. **vnpay.hash-secret**: `DRC0V9AAYA651P2SID7SVYRY46HND1H4` - ĐÚNG ✅

### ⚠️ Lưu ý về IPN URL:

**vnpay.ipn-url trong application.properties KHÔNG được sử dụng trong code!**

- IPN URL được cấu hình trên **VNPay Dashboard**, không phải trong code
- Code chỉ cung cấp endpoint `/api/v1/vnpay/ipn`, VNPay sẽ gọi đến URL bạn đã cấu hình trên dashboard
- Ngrok URL chỉ cần nhập trên VNPay Dashboard: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn`

## Tóm tắt:

| Cấu hình | Giá trị | Cần ngrok? | Trạng thái |
|----------|---------|------------|------------|
| **vnpay.url** | https://sandbox.vnpayment.vn/paymentv2/vpcpay.html | ❌ | ✅ Đúng |
| **vnpay.return-url** | http://localhost:3000/payment/vnpay-return | ❌ | ✅ Đúng |
| **IPN URL** | Cấu hình trên VNPay Dashboard | ✅ | ✅ Cần nhập: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn` |
| **vnpay.tmn-code** | PISGV29M | ❌ | ✅ Đúng |
| **vnpay.hash-secret** | DRC0V9AAYA651P2SID7SVYRY46HND1H4 | ❌ | ✅ Đúng |

## Kết luận:

✅ **Cấu hình trong application.properties ĐÃ ĐÚNG!**

- ReturnURL dùng localhost:3000 (không cần ngrok) ✅
- IPN URL được cấu hình trên VNPay Dashboard với ngrok URL ✅
- TmnCode và HashSecret đã đúng ✅

## Việc cần làm:

1. ✅ **Đã xong**: Cấu hình trong application.properties
2. ⏳ **Cần làm**: Nhập IPN URL trên VNPay Dashboard:
   ```
   https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
   ```
3. ⏳ **Cần làm**: Đảm bảo backend đang chạy trên cổng 8080
4. ⏳ **Cần làm**: Giữ ngrok chạy khi test

## Sẵn sàng test!

Sau khi nhập IPN URL trên VNPay Dashboard, bạn có thể test payment ngay!

