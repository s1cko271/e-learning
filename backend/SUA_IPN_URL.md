# ⚠️ Sửa IPN URL - Thêm endpoint vào cuối

## URL hiện tại (CHƯA ĐÚNG):

```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev
```

## URL cần nhập (ĐÚNG):

```
https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
```

## Các bước sửa:

1. **Click vào field "IPN Url *"** trên VNPay Dashboard
2. **Xóa URL cũ** hoặc **thêm phần endpoint** vào cuối:
   - Thêm: `/api/v1/vnpay/ipn`
3. **URL đầy đủ phải là:**
   ```
   https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn
   ```

## Giải thích:

- **Ngrok URL**: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev`
  - Đây chỉ là domain, chưa có endpoint
  
- **Endpoint backend**: `/api/v1/vnpay/ipn`
  - Đây là đường dẫn đến controller xử lý IPN callback

- **URL đầy đủ**: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn`
  - VNPay sẽ gọi đến URL này khi có payment callback

## Sau khi sửa:

1. ✅ Kiểm tra URL đầy đủ: `https://unomnipotently-presynsacral-silvana.ngrok-free.dev/api/v1/vnpay/ipn`
2. ✅ Giao thức IPN: **GET** ✅
3. ✅ Kiểu mã hóa: **HMACSHA512** ✅
4. ✅ Click **"Hoàn thành"** để lưu

## Test sau khi sửa:

1. Click **"Test call IPN"** để kiểm tra
2. Xem logs backend có nhận được request không
3. Kiểm tra response có đúng format không

